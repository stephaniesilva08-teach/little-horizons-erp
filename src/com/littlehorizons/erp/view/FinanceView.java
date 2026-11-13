package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.FinanceController;
import com.littlehorizons.erp.controller.ParentController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.Charge;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Parent;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentConcept;
import com.littlehorizons.erp.util.ConsoleUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Console view for the financial module (payments and .txt invoices).
 */
public class FinanceView {

    private final FinanceController controller = new FinanceController();
    private final StudentController studentController = new StudentController();
    private final ParentController parentController = new ParentController();

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("MÓDULO FINANCIERO");
            System.out.println("  1. Conceptos de pago");
            System.out.println("  2. Valores personalizados por estudiante");
            System.out.println("  3. Cargos y obligaciones");
            System.out.println("  4. Registrar pago y generar factura .txt");
            System.out.println("  5. Consultar pagos");
            System.out.println("  6. Facturas generadas");
            System.out.println("  0. Volver al menú principal");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 6);
            switch (option) {
                case 1 -> conceptsMenu();
                case 2 -> studentConceptsMenu();
                case 3 -> chargesMenu();
                case 4 -> registerPayment();
                case 5 -> paymentsMenu();
                case 6 -> listInvoices();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    // -------------------------------------------------------------- concepts

    private void conceptsMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("CONCEPTOS DE PAGO");
            System.out.println("  1. Listar conceptos activos");
            System.out.println("  2. Listar todos los conceptos");
            System.out.println("  3. Crear concepto");
            System.out.println("  4. Actualizar concepto");
            System.out.println("  5. Eliminar concepto");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> printConcepts(ViewUtils.query("list_active_payment_concepts",
                        controller::listActiveConcepts));
                case 2 -> printConcepts(ViewUtils.query("list_payment_concepts",
                        controller::listAllConcepts));
                case 3 -> createConcept();
                case 4 -> updateConcept();
                case 5 -> deleteConcept();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void printConcepts(List<PaymentConcept> concepts) {
        ConsoleUtils.printSubHeader("CONCEPTOS DE PAGO");
        if (concepts.isEmpty()) {
            ConsoleUtils.printInfo("No hay conceptos registrados.");
        } else {
            System.out.printf("%-4s %-12s %-26s %-14s %-10s %-8s%n",
                    "ID", "CÓDIGO", "CONCEPTO", "MONTO BASE", "RECURRENTE", "ACTIVO");
            ViewUtils.printSeparator();
            concepts.forEach(c -> System.out.printf("%-4d %-12s %-26s %-14s %-10s %-8s%n",
                    c.getIdConcept(), c.getConceptCode(), truncate(c.getName(), 26),
                    ViewUtils.money(c.getBaseAmount()), c.isRecurring() ? "Sí" : "No",
                    c.isActive() ? "Sí" : "No"));
        }
        ConsoleUtils.pause();
    }

    private void createConcept() {
        ConsoleUtils.printSubHeader("CREAR CONCEPTO DE PAGO");
        PaymentConcept concept = readConcept(new PaymentConcept());
        ViewUtils.executeWithId(() -> controller.createConcept(concept),
                "Concepto creado con");
        ConsoleUtils.pause();
    }

    private void updateConcept() {
        int id = ConsoleUtils.readInt("ID del concepto: ");
        PaymentConcept concept = ViewUtils.query("find_payment_concept", id,
                () -> controller.findConcept(id));
        if (concept == null) {
            ConsoleUtils.printError("El concepto no existe.");
            ConsoleUtils.pause();
            return;
        }
        readConcept(concept);
        ViewUtils.execute(() -> controller.updateConcept(concept),
                "Concepto actualizado correctamente.", id);
        ConsoleUtils.pause();
    }

    private PaymentConcept readConcept(PaymentConcept c) {
        c.setConceptCode(ViewUtils.readWithDefault("Código", c.getConceptCode()));
        c.setName(ViewUtils.readWithDefault("Nombre", c.getName()));
        c.setDescription(ViewUtils.readWithDefault("Descripción", c.getDescription()));
        c.setBaseAmount(ViewUtils.readAmountWithDefault("Monto base", c.getBaseAmount()));
        c.setRecurring(ConsoleUtils.readYesNo("¿Es recurrente?"));
        c.setActive(ConsoleUtils.readYesNo("¿Está activo?"));
        return c;
    }

    private void deleteConcept() {
        int id = ConsoleUtils.readInt("ID del concepto: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeConcept(id),
                    "Concepto eliminado correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    // ------------------------------------------------------ student concepts

    private void studentConceptsMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("VALORES PERSONALIZADOS");
            System.out.println("  1. Listar valores de un estudiante");
            System.out.println("  2. Registrar o actualizar valor personalizado");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 2);
            switch (option) {
                case 1 -> listStudentConcepts();
                case 2 -> setStudentConcept();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listStudentConcepts() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("VALORES PERSONALIZADOS");
            List<StudentConcept> list = controller.listStudentConcepts(idStudent);
            if (list.isEmpty()) {
                ConsoleUtils.printInfo("El estudiante no tiene valores personalizados.");
                return;
            }
            list.forEach(sc -> System.out.println("  Concepto " + sc.getIdConcept()
                    + ": " + ViewUtils.money(sc.getCustomAmount())
                    + " - " + sc.getReason()));
        }, null);
        ConsoleUtils.pause();
    }

    private void setStudentConcept() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        listConcepts();
        int idConcept = ConsoleUtils.readInt("ID del concepto: ");
        BigDecimal amount = ConsoleUtils.readDecimal("Monto personalizado: ", 0,
                999999999);
        String reason = ConsoleUtils.readOptional("Motivo: ");
        ViewUtils.execute(() -> controller.setStudentConcept(idStudent, idConcept,
                amount, reason), "Valor personalizado registrado correctamente.", idStudent);
        ConsoleUtils.pause();
    }

    // --------------------------------------------------------------- charges

    private void chargesMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("CARGOS Y OBLIGACIONES");
            System.out.println("  1. Listar todos los cargos");
            System.out.println("  2. Listar cargos de un estudiante");
            System.out.println("  3. Crear cargo");
            System.out.println("  4. Actualizar estado de cargo");
            System.out.println("  5. Eliminar cargo");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> printCharges(ViewUtils.query("list_charges", controller::listCharges));
                case 2 -> chargesByStudent();
                case 3 -> createCharge();
                case 4 -> updateChargeState();
                case 5 -> deleteCharge();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void chargesByStudent() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        ViewUtils.execute(() -> printCharges(
                controller.listChargesByStudent(idStudent)), null);
        ConsoleUtils.pause();
    }

    private void printCharges(List<Charge> charges) {
        ConsoleUtils.printSubHeader("CARGOS");
        if (charges.isEmpty()) {
            ConsoleUtils.printInfo("No hay cargos para mostrar.");
        } else {
            System.out.printf("%-4s %-26s %-22s %-12s %-10s %-10s%n",
                    "ID", "ESTUDIANTE", "CONCEPTO", "VENCE", "MONTO", "ESTADO");
            ViewUtils.printSeparator();
            charges.forEach(c -> System.out.printf("%-4d %-26s %-22s %-12s %-10s %-10s%n",
                    c.getIdCharge(), truncate(c.getStudentName(), 26),
                    truncate(c.getConceptName(), 22), c.getDueDate(),
                    ViewUtils.money(c.getAmount()), c.getState()));
        }
    }

    private void createCharge() {
        ConsoleUtils.printSubHeader("CREAR CARGO");
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        listConcepts();
        int idConcept = ConsoleUtils.readInt("ID del concepto: ");
        PaymentConcept concept = ViewUtils.query("find_payment_concept", idConcept,
                () -> controller.findConcept(idConcept));
        if (concept == null) {
            ConsoleUtils.printError("El concepto no existe.");
            ConsoleUtils.pause();
            return;
        }
        BigDecimal suggested = controller.resolveAmount(idStudent, concept);
        Charge charge = new Charge();
        charge.setIdStudent(idStudent);
        charge.setIdConcept(idConcept);
        charge.setAcademicYear(ConsoleUtils.readInt("Año académico: ", 2000, 2100));
        charge.setAcademicPeriod(ConsoleUtils.readEnum("Período", "I", "II", "III",
                "IV", "Anual"));
        charge.setAmount(ViewUtils.readAmountWithDefault("Monto", suggested));
        charge.setDueDate(ConsoleUtils.readDate("Fecha de vencimiento (YYYY-MM-DD): "));
        charge.setObservations(ConsoleUtils.readOptional("Observaciones: "));
        ViewUtils.executeWithId(() -> controller.createCharge(charge),
                "Cargo creado con");
        ConsoleUtils.pause();
    }

    private void updateChargeState() {
        int id = ConsoleUtils.readInt("ID del cargo: ");
        String state = ConsoleUtils.readEnum("Nuevo estado", "Pendiente", "Anulado");
        ViewUtils.execute(() -> controller.updateChargeState(id, state),
                "Estado del cargo actualizado.", id);
        ConsoleUtils.pause();
    }

    private void deleteCharge() {
        int id = ConsoleUtils.readInt("ID del cargo: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeCharge(id),
                    "Cargo eliminado correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    // -------------------------------------------------------------- payments

    private void registerPayment() {
        ConsoleUtils.printSubHeader("REGISTRAR PAGO");
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        Student student = studentController.findById(idStudent);
        if (student == null) {
            ConsoleUtils.printError("El estudiante no existe.");
            ConsoleUtils.pause();
            return;
        }
        int idParent = ConsoleUtils.readInt("ID del acudiente que paga (0 si no aplica): ");
        Parent guardian = idParent > 0 ? parentController.findById(idParent) : null;
        if (idParent > 0 && guardian == null) {
            ConsoleUtils.printError("El acudiente no existe.");
            ConsoleUtils.pause();
            return;
        }
        listConcepts();
        int idConcept = ConsoleUtils.readInt("ID del concepto: ");
        PaymentConcept concept = ViewUtils.query("find_payment_concept", idConcept,
                () -> controller.findConcept(idConcept));
        if (concept == null) {
            ConsoleUtils.printError("El concepto no existe.");
            ConsoleUtils.pause();
            return;
        }
        BigDecimal suggested = controller.resolveAmount(idStudent, concept);
        Payment payment = new Payment();
        payment.setIdStudent(idStudent);
        payment.setIdParent(idParent);
        payment.setIdConcept(idConcept);
        payment.setAmountPaid(ViewUtils.readAmountWithDefault("Monto pagado", suggested));
        payment.setPaymentDate(ConsoleUtils.readDate("Fecha de pago (YYYY-MM-DD): "));
        payment.setPaymentMethod(ConsoleUtils.readEnum("Método de pago", "Efectivo",
                "Transferencia", "Tarjeta Débito", "Tarjeta Crédito", "Cheque"));
        payment.setAcademicYear(ConsoleUtils.readInt("Año académico: ", 2000, 2100));
        payment.setAcademicPeriod(ConsoleUtils.readEnum("Período", "I", "II", "III",
                "IV", "Anual"));
        payment.setNotes(ConsoleUtils.readOptional("Notas: "));
        final Invoice[] invoice = new Invoice[1];
        ViewUtils.execute(() -> invoice[0] = controller.registerPayment(payment,
                student, guardian), "Pago registrado correctamente.");
        if (invoice[0] != null) {
            ConsoleUtils.printInfo("Factura: " + invoice[0].getInvoiceNumber());
            ConsoleUtils.printInfo("Archivo: " + invoice[0].getTxtPath());
            if (payment.getUnappliedAmount().signum() > 0) {
                ConsoleUtils.printInfo("Crédito pendiente de aplicar: "
                        + ViewUtils.money(payment.getUnappliedAmount()));
            }
        }
        ConsoleUtils.pause();
    }

    private void paymentsMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("PAGOS");
            System.out.println("  1. Listar todos los pagos");
            System.out.println("  2. Listar pagos de un estudiante");
            System.out.println("  3. Listar pagos de un acudiente");
            System.out.println("  4. Consultar factura de un pago");
            System.out.println("  5. Generar factura faltante");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> printPayments(ViewUtils.query("list_payments", controller::listPayments));
                case 2 -> paymentsByStudent();
                case 3 -> paymentsByGuardian();
                case 4 -> invoiceForPayment();
                case 5 -> generateMissingInvoice();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void paymentsByStudent() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        ViewUtils.execute(() -> printPayments(
                controller.listPaymentsByStudent(idStudent)), null);
        ConsoleUtils.pause();
    }

    private void paymentsByGuardian() {
        int idParent = ConsoleUtils.readInt("ID del acudiente: ");
        ViewUtils.execute(() -> printPayments(
                controller.listPaymentsByGuardian(idParent)), null);
        ConsoleUtils.pause();
    }

    private void printPayments(List<Payment> payments) {
        ConsoleUtils.printSubHeader("PAGOS");
        if (payments.isEmpty()) {
            ConsoleUtils.printInfo("No hay pagos para mostrar.");
            return;
        }
        System.out.printf("%-4s %-26s %-22s %-12s %-10s %-10s %-16s%n",
                "ID", "ESTUDIANTE", "CONCEPTO", "FECHA", "MONTO", "CRÉDITO", "RECIBO");
        ViewUtils.printSeparator();
        payments.forEach(p -> System.out.printf("%-4d %-26s %-22s %-12s %-10s %-10s %-16s%n",
                p.getIdPayment(), truncate(p.getStudentName(), 26),
                truncate(p.getConceptName(), 22), p.getPaymentDate(),
                ViewUtils.money(p.getAmountPaid()), ViewUtils.money(p.getUnappliedAmount()),
                p.getReceiptNumber()));
    }

    private void invoiceForPayment() {
        int idPayment = ConsoleUtils.readInt("ID del pago: ");
        ViewUtils.execute(() -> {
            Invoice invoice = ViewUtils.query("find_invoice_by_payment", idPayment,
                    () -> controller.findInvoiceByPayment(idPayment));
            if (invoice == null) {
                ConsoleUtils.printInfo("El pago no tiene factura asociada.");
                return;
            }
            ConsoleUtils.printSubHeader("FACTURA " + invoice.getInvoiceNumber());
            System.out.println("  Pago: " + invoice.getIdPayment());
            System.out.println("  Emisión: " + invoice.getIssueDate());
            System.out.println("  CUFE: " + invoice.getCufe());
            System.out.println("  Archivo: " + invoice.getTxtFileName());
            System.out.println("  Ruta: " + invoice.getTxtPath());
        }, null);
        ConsoleUtils.pause();
    }

    private void generateMissingInvoice() {
        int idPayment = ConsoleUtils.readInt("ID del pago: ");
        Payment payment = ViewUtils.query("find_payment", idPayment,
                () -> controller.findPayment(idPayment));
        if (payment == null) {
            ConsoleUtils.printError("El pago no existe.");
            ConsoleUtils.pause();
            return;
        }
        Student student = studentController.findById(payment.getIdStudent());
        Parent guardian = payment.getIdParent() > 0
                ? parentController.findById(payment.getIdParent()) : null;
        final Invoice[] invoice = new Invoice[1];
        ViewUtils.execute(() -> invoice[0] = controller.generateInvoiceForPayment(
                payment, student, guardian), "Factura generada correctamente.");
        if (invoice[0] != null) {
            ConsoleUtils.printInfo("Archivo: " + invoice[0].getTxtPath());
        }
        ConsoleUtils.pause();
    }

    private void listInvoices() {
        ConsoleUtils.printSubHeader("FACTURAS GENERADAS");
        List<Invoice> invoices = ViewUtils.query("list_invoices", controller::listInvoices);
        if (invoices.isEmpty()) {
            ConsoleUtils.printInfo("No hay facturas registradas.");
        } else {
            System.out.printf("%-4s %-18s %-6s %-12s %-28s%n",
                    "ID", "NÚMERO", "PAGO", "EMISIÓN", "ARCHIVO");
            ViewUtils.printSeparator();
            invoices.forEach(i -> System.out.printf("%-4d %-18s %-6d %-12s %-28s%n",
                    i.getIdInvoice(), i.getInvoiceNumber(), i.getIdPayment(),
                    i.getIssueDate(), truncate(i.getTxtFileName(), 28)));
        }
        ConsoleUtils.pause();
    }

    private void listConcepts() {
        System.out.println("  Conceptos activos:");
        for (PaymentConcept c : ViewUtils.query("list_active_payment_concepts",
                controller::listActiveConcepts)) {
            System.out.println("    " + c.getIdConcept() + " - " + c.getConceptCode()
                    + " " + c.getName() + " (" + ViewUtils.money(c.getBaseAmount()) + ")");
        }
    }

    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 1) + ".";
    }
}
