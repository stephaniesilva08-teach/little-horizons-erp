package com.littlehorizons.erp.util;

import java.time.Duration;

/**
 * Composition root for the persistence layer.
 *
 * <p>Builds the shared connection pool once and exposes it as the default
 * {@link ConnectionProvider} for every DAO. Tests bypass this class by
 * injecting their own provider.</p>
 */
public final class Database {

    private static final int POOL_SIZE = 5;
    private static final Duration ACQUIRE_TIMEOUT = Duration.ofSeconds(5);

    private Database() {
    }

    public static ConnectionProvider provider() {
        return Holder.POOL;
    }

    /**
     * Returns how many connections the shared pool has opened.
     */
    public static int openConnections() {
        return Holder.POOL.openConnections();
    }

    /**
     * Closes the idle connections of the shared pool. Called when the
     * application exits.
     */
    public static void shutdown() {
        Holder.POOL.close();
    }

    private static final class Holder {
        private static final ConnectionPool POOL =
                new ConnectionPool(ConnectionFactory.getInstance(), POOL_SIZE, ACQUIRE_TIMEOUT);
    }
}
