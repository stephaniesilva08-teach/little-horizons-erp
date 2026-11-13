package com.littlehorizons.erp.test;

import com.littlehorizons.erp.dao.JdbcFinanceDao;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.util.ConnectionProvider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Tests transaction boundaries for financial sequence reservations. */
public final class JdbcFinanceDaoTest {

    private JdbcFinanceDaoTest() {
    }

    public static void register(TestSuite suite) {
        suite.add("Payment, invoice and both sequences commit together", () -> {
            JdbcHarness harness = new JdbcHarness();
            JdbcFinanceDao dao = new JdbcFinanceDao(harness.provider());
            Payment payment = payment();
            String[] number = new String[1];
            int id = dao.registerPaymentWithInvoice(payment, invoiceNumber -> {
                number[0] = invoiceNumber;
                Invoice invoice = new Invoice();
                invoice.setInvoiceNumber(invoiceNumber);
                invoice.setIssueDate(LocalDate.now().toString());
                invoice.setCufe("test-cufe");
                invoice.setTxtFileName("test.txt");
                invoice.setTxtPath("/tmp/test.txt");
                return invoice;
            });

            String year = Integer.toString(LocalDate.now().getYear());
            Assert.equals(101, id, "the inserted payment ID must be returned");
            Assert.equals("REC-" + year + "-00001", payment.getReceiptNumber(),
                    "the receipt number must be assigned inside the transaction");
            Assert.equals("FAC-" + year + "-00001", number[0],
                    "the invoice number must start at one for an empty sequence");
            Assert.isTrue(harness.events.indexOf("setAutoCommit:false")
                            < harness.events.indexOf("sequence:recibo"),
                    "sequence reservation must happen after transaction start");
            Assert.isTrue(harness.events.indexOf("sequence:factura")
                            < harness.events.indexOf("payment-insert"),
                    "the invoice sequence must be reserved before persistence");
            Assert.isTrue(harness.committed, "the transaction must commit on success");
            Assert.isFalse(harness.rolledBack, "the successful path must not roll back");
        });

        suite.add("Failed invoice generation rolls back sequence reservations", () -> {
            JdbcHarness harness = new JdbcHarness();
            JdbcFinanceDao dao = new JdbcFinanceDao(harness.provider());
            Assert.assertThrows(IllegalStateException.class,
                    () -> dao.registerPaymentWithInvoice(payment(), invoiceNumber -> {
                        throw new IllegalStateException("simulated invoice failure");
                    }), "a failed invoice factory must abort the transaction");
            Assert.isTrue(harness.rolledBack, "the failed path must roll back");
            Assert.isFalse(harness.committed, "the failed path must not commit");
            Assert.isTrue(harness.sequences.isEmpty(),
                    "rollback must restore both sequence values");
        });
    }

    private static Payment payment() {
        Payment payment = new Payment();
        payment.setIdStudent(1);
        payment.setIdConcept(1);
        payment.setAmountPaid(new BigDecimal("100.00"));
        payment.setPaymentDate("2026-11-13");
        payment.setPaymentMethod("Efectivo");
        payment.setAcademicYear(2026);
        payment.setAcademicPeriod("I");
        return payment;
    }

    private static final class JdbcHarness implements InvocationHandler {
        private final Map<String, Integer> sequences = new HashMap<>();
        private final List<String> events = new ArrayList<>();
        private Map<String, Integer> snapshot = new HashMap<>();
        private boolean committed;
        private boolean rolledBack;
        private boolean transaction;
        private int paymentId = 101;
        private int invoiceId = 201;

        ConnectionProvider provider() {
            return () -> proxy(Connection.class, this);
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            return switch (method.getName()) {
                case "setAutoCommit" -> {
                    transaction = !((boolean) args[0]);
                    events.add("setAutoCommit:" + args[0]);
                    if (transaction) snapshot = new HashMap<>(sequences);
                    yield null;
                }
                case "prepareStatement" -> statement((String) args[0]);
                case "commit" -> {
                    committed = true;
                    events.add("commit");
                    yield null;
                }
                case "rollback" -> {
                    rolledBack = true;
                    sequences.clear();
                    sequences.putAll(snapshot);
                    events.add("rollback");
                    yield null;
                }
                case "close" -> null;
                case "isClosed" -> false;
                case "toString" -> "JdbcHarnessConnection";
                default -> defaultValue(method.getReturnType());
            };
        }

        private PreparedStatement statement(String sql) {
            Map<Integer, Object> parameters = new HashMap<>();
            InvocationHandler handler = (statement, method, args) -> {
                String methodName = method.getName();
                if (methodName.startsWith("set") && args != null && args.length >= 2) {
                    parameters.put((Integer) args[0], args[1]);
                    return null;
                }
                return switch (methodName) {
                    case "executeUpdate" -> executeUpdate(sql, parameters);
                    case "executeQuery" -> sequenceResult(sql, parameters);
                    case "getGeneratedKeys" -> generatedKeys(sql);
                    case "close" -> null;
                    case "toString" -> sql;
                    default -> defaultValue(method.getReturnType());
                };
            };
            return proxy(PreparedStatement.class, handler);
        }

        private int executeUpdate(String sql, Map<Integer, Object> parameters) {
            if (sql.startsWith("INSERT INTO secuencias_financieras")) {
                String name = (String) parameters.get(1);
                sequences.merge(name, 1, Integer::sum);
                events.add("sequence:" + name);
            } else if (sql.startsWith("INSERT INTO pagos")) {
                events.add("payment-insert");
            } else if (sql.startsWith("INSERT INTO facturas")) {
                events.add("invoice-insert");
            }
            return 1;
        }

        private ResultSet sequenceResult(String sql, Map<Integer, Object> parameters) {
            String name = (String) parameters.get(1);
            return resultSet(sequences.getOrDefault(name, 0));
        }

        private ResultSet generatedKeys(String sql) {
            int key = sql.startsWith("INSERT INTO pagos") ? paymentId : invoiceId;
            if (sql.startsWith("INSERT INTO pagos")) paymentId++;
            else invoiceId++;
            return resultSet(key);
        }

        private ResultSet resultSet(int value) {
            boolean[] consumed = {false};
            return proxy(ResultSet.class, (result, method, args) -> switch (method.getName()) {
                case "next" -> {
                    if (consumed[0]) yield false;
                    consumed[0] = true;
                    yield true;
                }
                case "getInt" -> value;
                case "close" -> null;
                case "wasNull" -> false;
                default -> defaultValue(method.getReturnType());
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        return null;
    }
}
