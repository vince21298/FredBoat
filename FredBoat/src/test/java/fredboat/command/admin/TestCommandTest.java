package fredboat.command.admin;

import fredboat.Config;
import fredboat.ProvideJDASingleton;
import fredboat.database.DatabaseConfig;
import fredboat.database.DatabaseManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by napster on 16.04.17.
 */
class TestCommandTest extends ProvideJDASingleton {


    @AfterAll
    public static void saveStats() {
        saveClassStats(TestCommandTest.class.getSimpleName());
    }


    /**
     * Run a small db test
     */
    @Test
    void onInvoke() throws IOException {
        Assumptions.assumeFalse(isTravisEnvironment(), () -> "Aborting test: Travis CI detected");
        Assumptions.assumeTrue(initialized);
        String[] args = {"test", "10", "10"};

        //test the connection if one was specified
        DatabaseConfig dbConfig = DatabaseConfig.loadDefault();
        ExecutorService executor = Executors.newCachedThreadPool();
        String appName = "FredBoat_TESTING";
        if (dbConfig.jdbcUrl != null && !"".equals(dbConfig.jdbcUrl)) {
            //start the database
            DatabaseManager dbm = new DatabaseManager(dbConfig, null, Config.CONFIG.getHikariPoolSize(),
                    appName, executor);
            try {
                dbm.startup();
                Assertions.assertTrue(new TestCommand().invoke(dbm, testChannel, testSelfMember, args));
            } finally {
                dbm.shutdown();
            }
        }

        //test the internal SQLite db
        args[1] = args[2] = "2";
        dbConfig.jdbcUrl = "jdbc:sqlite:fredboat.db";
        dbConfig.useSshTunnel = false;
        DatabaseManager dbm = new DatabaseManager(dbConfig, "org.hibernate.dialect.SQLiteDialect", 1,
                appName, executor);
        try {
            dbm.startup();
            Assertions.assertTrue(new TestCommand().invoke(dbm, testChannel, testSelfMember, args));
        } finally {
            dbm.shutdown();
        }
        bumpPassedTests();
    }
}