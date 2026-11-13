package com.littlehorizons.erp.test;

/**
 * Entry point of the automated test suite.
 *
 * <p>Run it with the plain JDK, without Maven or any test framework:</p>
 * <pre>
 * javac --release 17 -encoding UTF-8 -cp "out:lib/mysql-connector-j-8.4.0.jar" \
 *       -d out-test $(find src test -name "*.java")
 * java -cp "out-test:lib/mysql-connector-j-8.4.0.jar" com.littlehorizons.erp.test.AllTests
 * </pre>
 */
public final class AllTests {

    private AllTests() {
    }

    public static void main(String[] args) {
        TestSuite suite = new TestSuite();
        ValidatorTest.register(suite);
        TextInvoiceGeneratorTest.register(suite);
        StudentControllerTest.register(suite);
        AcademicControllerTest.register(suite);
        ExtracurricularControllerTest.register(suite);
        JdbcFinanceDaoTest.register(suite);
        PaymentAllocatorTest.register(suite);
        ModelIdentityTest.register(suite);
        ConnectionPoolTest.register(suite);
        boolean success = suite.run();
        System.exit(success ? 0 : 1);
    }
}
