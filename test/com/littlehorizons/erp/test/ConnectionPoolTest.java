package com.littlehorizons.erp.test;

import com.littlehorizons.erp.util.ConnectionPool;
import com.littlehorizons.erp.util.ConnectionProvider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the pool lifecycle: reuse, bounds, timeouts and shutdown.
 */
public final class ConnectionPoolTest {

    private ConnectionPoolTest() {
    }

    public static void register(TestSuite suite) {
        suite.add("Released connections are reused", () -> {
            FakeProvider provider = new FakeProvider();
            ConnectionPool pool = new ConnectionPool(provider, 2, Duration.ofSeconds(1));
            Connection first = pool.getConnection();
            first.close();
            Connection second = pool.getConnection();
            Assert.equals(1, provider.created, "the physical connection must be reused");
            Assert.equals(1, pool.openConnections(), "one open connection");
            Assert.equals(0, pool.idleConnections(), "the reused connection is in use");
            second.close();
            Assert.equals(1, pool.idleConnections(), "the connection returns to the pool");
            pool.close();
        });
        suite.add("The pool never exceeds its size", () -> {
            FakeProvider provider = new FakeProvider();
            ConnectionPool pool = new ConnectionPool(provider, 1, Duration.ofMillis(150));
            Connection held = pool.getConnection();
            SQLException error = Assert.assertThrows(SQLException.class,
                    pool::getConnection,
                    "a full pool must wait and then time out");
            Assert.isTrue(error.getMessage().contains("Timed out"),
                    "the timeout message should be explicit");
            held.close();
            Connection recovered = pool.getConnection();
            Assert.notNull(recovered, "the connection is available after release");
            Assert.equals(1, provider.created, "no extra connection is created");
            recovered.close();
            pool.close();
        });
        suite.add("Closing a connection after shutdown releases it", () -> {
            FakeProvider provider = new FakeProvider();
            ConnectionPool pool = new ConnectionPool(provider, 2, Duration.ofSeconds(1));
            Connection connection = pool.getConnection();
            pool.close();
            connection.close();
            Assert.equals(0, pool.openConnections(), "the pool counts no open connections");
            Assert.isTrue(provider.connections.get(0).closed, "the physical connection is closed");
        });
        suite.add("A closed pool refuses new requests", () -> {
            FakeProvider provider = new FakeProvider();
            ConnectionPool pool = new ConnectionPool(provider, 2, Duration.ofSeconds(1));
            pool.close();
            Assert.assertThrows(SQLException.class, pool::getConnection,
                    "closed pools cannot hand out connections");
            Assert.equals(0, provider.created, "no connection should be created");
        });
        suite.add("Broken idle connections are replaced", () -> {
            FakeProvider provider = new FakeProvider();
            ConnectionPool pool = new ConnectionPool(provider, 2, Duration.ofSeconds(1));
            Connection first = pool.getConnection();
            first.close();
            provider.connections.get(0).closed = true;
            Connection second = pool.getConnection();
            Assert.notNull(second, "a fresh connection is handed out");
            Assert.equals(2, provider.created, "the broken connection is discarded");
            second.close();
            pool.close();
        });
    }

    private static final class FakeProvider implements ConnectionProvider {

        private final List<FakeConnection> connections = new ArrayList<>();
        private int created;

        @Override
        public Connection getConnection() {
            created++;
            FakeConnection fake = new FakeConnection();
            connections.add(fake);
            return fake.proxy();
        }
    }

    private static final class FakeConnection implements InvocationHandler {

        private boolean closed;

        private Connection proxy() {
            return (Connection) Proxy.newProxyInstance(
                    ConnectionPoolTest.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "close":
                    closed = true;
                    return null;
                case "isClosed":
                    return closed;
                case "isValid":
                    return !closed;
                case "getAutoCommit":
                    return true;
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "FakeConnection";
                default:
                    return defaultValue(method.getReturnType());
            }
        }

        private static Object defaultValue(Class<?> type) {
            if (!type.isPrimitive() || type == void.class) {
                return null;
            }
            if (type == boolean.class) {
                return false;
            }
            if (type == char.class) {
                return '\0';
            }
            if (type == byte.class) {
                return (byte) 0;
            }
            if (type == short.class) {
                return (short) 0;
            }
            if (type == int.class) {
                return 0;
            }
            if (type == long.class) {
                return 0L;
            }
            if (type == float.class) {
                return 0f;
            }
            return 0d;
        }
    }
}
