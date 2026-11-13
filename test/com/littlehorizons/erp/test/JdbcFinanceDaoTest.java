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
            harness.charges.add(new ChargeRow(501, new BigDecimal("60.00"),
                    new BigDecimal("0.00")));
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
            Assert.equals("101:501:60.00", harness.allocations.get(0),
                    "the payment allocation must be persisted with the payment");
            Assert.equals(new BigDecimal("40.00"), payment.getUnappliedAmount(),
                    "the amount above the charge must remain as credit");
            Assert.equals("Pagado", harness.chargeStates.get(501),
                    "a fully allocated charge must be marked paid");
            Assert.isTrue(harness.events.indexOf("payment-allocation")
                            < harness.events.indexOf("invoice-insert"),
                    "payment allocation must be stored before invoice commit");
        });

        suite.add("Failed invoice generation rolls back sequence reservations", () -> {
            JdbcHarness harness = new JdbcHarness();
            harness.charges.add(new ChargeRow(501, new BigDecimal("60.00"),
                    new BigDecimal("0.00")));
            JdbcFinanceDao dao = new JdbcFinanceDao(harness.provider());
            Assert.assertThrows(IllegalStateException.class,
                    () -> dao.registerPaymentWithInvoice(payment(), invoiceNumber -> {
                        throw new IllegalStateException("simulated invoice failure");
                    }), "a failed invoice factory must abort the transaction");
            Assert.isTrue(harness.rolledBack, "the failed path must roll back");
            Assert.isFalse(harness.committed, "the failed path must not commit");
            Assert.isTrue(harness.sequences.isEmpty(),
                    "rollback must restore both sequence values");
            Assert.isTrue(harness.allocations.isEmpty(),
                    "rollback must remove payment allocations too");
        });

        suite.add("A future charge consumes an existing unapplied credit", () -> {
            JdbcHarness harness = new JdbcHarness();
            harness.paymentCredits.put(99, new BigDecimal("20.00"));
            JdbcFinanceDao dao = new JdbcFinanceDao(harness.provider());
            com.littlehorizons.erp.model.Charge charge = new com.littlehorizons.erp.model.Charge();
            charge.setIdStudent(1);
            charge.setIdConcept(1);
            charge.setAcademicYear(2026);
            charge.setAcademicPeriod("I");
            charge.setAmount(new BigDecimal("50.00"));
            charge.setDueDate("2026-12-01");
            charge.setState("Pendiente");

            int id = dao.insertCharge(charge);

            Assert.equals(301, id, "the new charge ID must be returned");
            Assert.equals("99:301:20.00", harness.allocations.get(0),
                    "an older credit should be applied to the new charge");
            Assert.equals(new BigDecimal("0.00"), harness.paymentCredits.get(99),
                    "the consumed credit balance should be reduced to zero");
            Assert.equals("Pendiente", harness.chargeStates.get(301),
                    "the unpaid remainder should keep the charge pending");
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
        private Map<Integer, BigDecimal> creditSnapshot = new HashMap<>();
        private Map<Integer, String> chargeStateSnapshot = new HashMap<>();
        private List<String> allocationSnapshot = new ArrayList<>();
        private final List<ChargeRow> charges = new ArrayList<>();
        private final Map<Integer, BigDecimal> paymentCredits = new HashMap<>();
        private final Map<Integer, String> chargeStates = new HashMap<>();
        private final List<String> allocations = new ArrayList<>();
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
                    if (transaction) {
                        snapshot = new HashMap<>(sequences);
                        creditSnapshot = new HashMap<>(paymentCredits);
                        chargeStateSnapshot = new HashMap<>(chargeStates);
                        allocationSnapshot = new ArrayList<>(allocations);
                    }
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
                    paymentCredits.clear();
                    paymentCredits.putAll(creditSnapshot);
                    chargeStates.clear();
                    chargeStates.putAll(chargeStateSnapshot);
                    allocations.clear();
                    allocations.addAll(allocationSnapshot);
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
            List<Map<Integer, Object>> batches = new ArrayList<>();
            InvocationHandler handler = (statement, method, args) -> {
                String methodName = method.getName();
                if (methodName.startsWith("set") && args != null && args.length >= 2) {
                    parameters.put((Integer) args[0], args[1]);
                    return null;
                }
                return switch (methodName) {
                    case "executeUpdate" -> executeUpdate(sql, parameters);
                    case "addBatch" -> {
                        batches.add(new HashMap<>(parameters));
                        yield null;
                    }
                    case "executeBatch" -> {
                        int[] results = new int[batches.size()];
                        for (int i = 0; i < batches.size(); i++) {
                            results[i] = executeUpdate(sql, batches.get(i));
                        }
                        batches.clear();
                        yield results;
                    }
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
            } else if (sql.startsWith("INSERT INTO pagos_cargos")) {
                allocations.add(parameters.get(1) + ":" + parameters.get(2) + ":"
                        + parameters.get(3));
                events.add("payment-allocation");
            } else if (sql.startsWith("INSERT INTO pagos (")) {
                BigDecimal insertedPaymentAmount = (BigDecimal) parameters.get(4);
                paymentCredits.put(paymentId, insertedPaymentAmount);
                events.add("payment-insert");
            } else if (sql.startsWith("INSERT INTO cargos_estudiante")) {
                charges.add(new ChargeRow(301, (BigDecimal) parameters.get(5),
                        new BigDecimal("0.00")));
            } else if (sql.startsWith("UPDATE pagos SET monto_no_aplicado")) {
                paymentCredits.put((Integer) parameters.get(2), (BigDecimal) parameters.get(1));
            } else if (sql.startsWith("UPDATE cargos_estudiante SET estado")) {
                chargeStates.put((Integer) parameters.get(2), (String) parameters.get(1));
            } else if (sql.startsWith("INSERT INTO facturas")) {
                events.add("invoice-insert");
            }
            return 1;
        }

        private ResultSet sequenceResult(String sql, Map<Integer, Object> parameters) {
            if (sql.contains("FROM secuencias_financieras")) {
                String name = (String) parameters.get(1);
                return resultSet(sequences.getOrDefault(name, 0));
            }
            if (sql.contains("FROM estudiantes")) {
                return resultSet(1);
            }
            if (sql.contains("FROM cargos_estudiante c LEFT JOIN pagos_cargos")) {
                return chargeResultSet();
            }
            if (sql.startsWith("SELECT id_pago, monto_no_aplicado FROM pagos")) {
                return paymentCreditResultSet();
            }
            return emptyResultSet();
        }

        private ResultSet paymentCreditResultSet() {
            List<Map<String, Object>> rows = new ArrayList<>();
            paymentCredits.forEach((id, amount) -> {
                Map<String, Object> row = new HashMap<>();
                row.put("id_pago", id);
                row.put("monto_no_aplicado", amount);
                rows.add(row);
            });
            return rowResultSet(rows);
        }

        private ResultSet chargeResultSet() {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (ChargeRow charge : charges) {
                Map<String, Object> row = new HashMap<>();
                row.put("id_cargo", charge.id());
                row.put("monto", charge.amount());
                row.put("monto_aplicado", charge.applied());
                rows.add(row);
            }
            return rowResultSet(rows);
        }

        private ResultSet rowResultSet(List<Map<String, Object>> rows) {
            int[] index = {-1};
            return proxy(ResultSet.class, (result, method, args) -> switch (method.getName()) {
                case "next" -> ++index[0] < rows.size();
                case "getInt" -> ((Number) value(rows, index[0], args[0])).intValue();
                case "getBigDecimal" -> (BigDecimal) value(rows, index[0], args[0]);
                case "getString" -> (String) value(rows, index[0], args[0]);
                case "close" -> null;
                case "wasNull" -> false;
                default -> defaultValue(method.getReturnType());
            });
        }

        private Object value(List<Map<String, Object>> rows, int index, Object column) {
            if (column instanceof Integer integer) {
                return rows.get(index).values().toArray()[integer - 1];
            }
            return rows.get(index).get(column.toString());
        }

        private ResultSet emptyResultSet() {
            return proxy(ResultSet.class, (result, method, args) -> switch (method.getName()) {
                case "next" -> false;
                case "close" -> null;
                default -> defaultValue(method.getReturnType());
            });
        }

        private ResultSet generatedKeys(String sql) {
            int key;
            if (sql.startsWith("INSERT INTO pagos (")) key = paymentId;
            else if (sql.startsWith("INSERT INTO cargos_estudiante")) key = 301;
            else key = invoiceId;
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

    private record ChargeRow(int id, BigDecimal amount, BigDecimal applied) { }

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
