package com.littlehorizons.erp.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Small bounded pool of JDBC connections.
 *
 * <p>Connections handed out by {@link #getConnection()} are proxies: calling
 * {@code close()} on them returns the physical connection to the pool instead
 * of closing it. Idle connections are validated before being reused, so a
 * connection dropped by the server is discarded and replaced transparently.</p>
 *
 * <p>The pool is thread safe and never grows beyond its configured size.</p>
 */
public final class ConnectionPool implements ConnectionProvider, AutoCloseable {

    private static final int DEFAULT_MAX_SIZE = 5;
    private static final Duration DEFAULT_ACQUIRE_TIMEOUT = Duration.ofSeconds(5);
    private static final int VALIDATION_TIMEOUT_SECONDS = 2;

    private final ConnectionProvider source;
    private final int maxSize;
    private final long acquireTimeoutMillis;
    private final BlockingQueue<Connection> idle;
    private final Object stateLock = new Object();
    private int openConnections;
    private volatile boolean closed;

    public ConnectionPool(ConnectionProvider source) {
        this(source, DEFAULT_MAX_SIZE, DEFAULT_ACQUIRE_TIMEOUT);
    }

    public ConnectionPool(ConnectionProvider source, int maxSize, Duration acquireTimeout) {
        if (source == null) {
            throw new IllegalArgumentException("A connection source is required.");
        }
        if (maxSize < 1) {
            throw new IllegalArgumentException("Pool size must be at least 1.");
        }
        if (acquireTimeout == null || acquireTimeout.isNegative()) {
            throw new IllegalArgumentException("Acquire timeout must be zero or positive.");
        }
        this.source = source;
        this.maxSize = maxSize;
        this.acquireTimeoutMillis = acquireTimeout.toMillis();
        this.idle = new LinkedBlockingQueue<>(maxSize);
    }

    @Override
    public Connection getConnection() throws SQLException {
        long deadline = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(acquireTimeoutMillis);
        while (true) {
            if (closed) {
                throw new SQLException("The connection pool is closed.");
            }
            Connection pooled = idle.poll();
            if (pooled != null) {
                if (isUsable(pooled)) {
                    return checkout(pooled);
                }
                discard(pooled);
                continue;
            }
            if (reserveSlot()) {
                try {
                    Connection physical = source.getConnection();
                    if (physical == null) {
                        throw new SQLException("The connection source returned null.");
                    }
                    synchronized (stateLock) {
                        if (closed) {
                            closeQuietly(physical);
                            throw new SQLException("The connection pool is closed.");
                        }
                        return wrap(physical);
                    }
                } catch (SQLException | RuntimeException e) {
                    releaseReservedSlot();
                    throw e;
                }
            }
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                throw new SQLException(
                        "Timed out waiting for a database connection after "
                        + acquireTimeoutMillis + " ms.");
            }
            try {
                pooled = idle.poll(remaining, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted while waiting for a connection.", e);
            }
            if (pooled == null) {
                throw new SQLException(
                        "Timed out waiting for a database connection after "
                        + acquireTimeoutMillis + " ms.");
            }
            if (isUsable(pooled)) {
                return checkout(pooled);
            }
            discard(pooled);
        }
    }

    /**
     * Number of connections currently created, either idle or in use.
     */
    public int openConnections() {
        synchronized (stateLock) {
            return openConnections;
        }
    }

    /**
     * Number of connections waiting to be reused.
     */
    public int idleConnections() {
        return idle.size();
    }

    @Override
    public void close() {
        List<Connection> toClose = new ArrayList<>();
        synchronized (stateLock) {
            closed = true;
            idle.drainTo(toClose);
            openConnections -= toClose.size();
        }
        for (Connection connection : toClose) {
            closeQuietly(connection);
        }
    }

    private void release(Connection physical) {
        try {
            if (!physical.getAutoCommit()) {
                physical.rollback();
                physical.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Fall through and discard the broken connection.
            closeQuietly(physical);
            releaseReservedSlot();
            return;
        }
        boolean usable = isUsable(physical);
        synchronized (stateLock) {
            if (!closed && usable && idle.offer(physical)) {
                return;
            }
            openConnections--;
        }
        closeQuietly(physical);
    }

    private void discard(Connection physical) {
        closeQuietly(physical);
        releaseReservedSlot();
    }

    private boolean reserveSlot() throws SQLException {
        synchronized (stateLock) {
            if (closed) {
                throw new SQLException("The connection pool is closed.");
            }
            if (openConnections >= maxSize) {
                return false;
            }
            openConnections++;
            return true;
        }
    }

    private void releaseReservedSlot() {
        synchronized (stateLock) {
            openConnections--;
        }
    }

    private Connection checkout(Connection physical) throws SQLException {
        synchronized (stateLock) {
            if (closed) {
                openConnections--;
                closeQuietly(physical);
                throw new SQLException("The connection pool is closed.");
            }
            return wrap(physical);
        }
    }

    private boolean isUsable(Connection connection) {
        try {
            return !connection.isClosed()
                    && connection.isValid(VALIDATION_TIMEOUT_SECONDS);
        } catch (SQLException e) {
            return false;
        }
    }

    private static void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
            // Nothing else can be done for a connection that cannot be closed.
        }
    }

    private Connection wrap(Connection physical) {
        return (Connection) Proxy.newProxyInstance(
                ConnectionPool.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new PooledConnectionHandler(physical));
    }

    private final class PooledConnectionHandler implements InvocationHandler {

        private final Connection physical;
        private boolean released;

        private PooledConnectionHandler(Connection physical) {
            this.physical = physical;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "close":
                    if (!released) {
                        released = true;
                        release(physical);
                    }
                    return null;
                case "isClosed":
                    return released || physical.isClosed();
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "PooledConnection[" + physical + "]";
                default:
                    if (released) {
                        throw new SQLException("The pooled connection is already closed.");
                    }
                    try {
                        return method.invoke(physical, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
            }
        }
    }
}
