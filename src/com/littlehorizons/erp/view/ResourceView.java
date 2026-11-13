package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.util.ConsoleUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * Console view for the resource module.
 */
public class ResourceView {

    private final ResourceController controller = new ResourceController();

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("GESTIÓN DE RECURSOS");
            System.out.println("  1. Categorías de recursos");
            System.out.println("  2. Inventario de recursos");
            System.out.println("  3. Préstamos");
            System.out.println("  0. Volver al menú principal");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 3);
            switch (option) {
                case 1 -> categoriesMenu();
                case 2 -> resourcesMenu();
                case 3 -> loansMenu();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void categoriesMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("CATEGORÍAS DE RECURSOS");
            System.out.println("  1. Listar categorías");
            System.out.println("  2. Crear categoría");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 2);
            switch (option) {
                case 1 -> listCategories();
                case 2 -> createCategory();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listCategories() {
        ConsoleUtils.printSubHeader("CATEGORÍAS");
        List<ResourceCategory> categories = ViewUtils.query(
                "list_resource_categories", controller::listCategories);
        if (categories.isEmpty()) {
            ConsoleUtils.printInfo("No hay categorías registradas.");
        } else {
            categories.forEach(c -> System.out.println("  [" + c.getIdCategory() + "] "
                    + c.getName() + " - " + c.getDescription()));
        }
        ConsoleUtils.pause();
    }

    private void createCategory() {
        ResourceCategory category = new ResourceCategory();
        category.setName(ConsoleUtils.readNonEmpty("Nombre de la categoría: "));
        category.setDescription(ConsoleUtils.readOptional("Descripción: "));
        ViewUtils.executeWithId(() -> controller.createCategory(category),
                "Categoría creada con");
        ConsoleUtils.pause();
    }

    private void resourcesMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("INVENTARIO DE RECURSOS");
            System.out.println("  1. Listar todos los recursos");
            System.out.println("  2. Listar recursos disponibles");
            System.out.println("  3. Registrar recurso");
            System.out.println("  4. Actualizar recurso");
            System.out.println("  5. Eliminar recurso");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> printResources(ViewUtils.query(
                        "list_resources", controller::listResources));
                case 2 -> printResources(ViewUtils.query(
                        "list_available_resources", controller::listAvailableResources));
                case 3 -> createResource();
                case 4 -> updateResource();
                case 5 -> deleteResource();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void printResources(List<Resource> resources) {
        ConsoleUtils.printSubHeader("RECURSOS");
        if (resources.isEmpty()) {
            ConsoleUtils.printInfo("No hay recursos para mostrar.");
        } else {
            System.out.printf("%-4s %-10s %-28s %-20s %-14s %-16s%n",
                    "ID", "CÓDIGO", "RECURSO", "CATEGORÍA", "UBICACIÓN", "ESTADO");
            ViewUtils.printSeparator();
            resources.forEach(r -> System.out.printf("%-4d %-10s %-28s %-20s %-14s %-16s%n",
                    r.getIdResource(), r.getResourceCode(), truncate(r.getName(), 28),
                    truncate(r.getCategoryName(), 20), truncate(r.getLocation(), 14),
                    r.getState()));
        }
        ConsoleUtils.pause();
    }

    private void createResource() {
        ConsoleUtils.printSubHeader("REGISTRAR RECURSO");
        listCategoryOptions();
        Resource resource = readResource(new Resource());
        ViewUtils.executeWithId(() -> controller.createResource(resource),
                "Recurso registrado con");
        ConsoleUtils.pause();
    }

    private void updateResource() {
        int id = ConsoleUtils.readInt("ID del recurso: ");
        Resource resource = ViewUtils.query("find_resource", id,
                () -> controller.findResource(id));
        if (resource == null) {
            ConsoleUtils.printError("El recurso no existe.");
            ConsoleUtils.pause();
            return;
        }
        listCategoryOptions();
        readResource(resource);
        ViewUtils.execute(() -> controller.updateResource(resource),
                "Recurso actualizado correctamente.", id);
        ConsoleUtils.pause();
    }

    private Resource readResource(Resource r) {
        r.setResourceCode(ViewUtils.readWithDefault("Código del recurso",
                r.getResourceCode()));
        r.setName(ViewUtils.readWithDefault("Nombre del recurso", r.getName()));
        r.setIdCategory(ConsoleUtils.readInt("ID de la categoría: "));
        r.setDescription(ViewUtils.readWithDefault("Descripción", r.getDescription()));
        if (r.getAcquisitionDate() == null) {
            r.setAcquisitionDate(ConsoleUtils.readDate(
                    "Fecha de adquisición (YYYY-MM-DD): "));
        } else {
            r.setAcquisitionDate(ViewUtils.readDateWithDefault(
                    "Fecha de adquisición", r.getAcquisitionDate()));
        }
        r.setLocation(ViewUtils.readWithDefault("Ubicación física", r.getLocation()));
        r.setState(ConsoleUtils.readEnum("Estado", "Disponible", "Prestado",
                "En Mantenimiento", "Dado de Baja"));
        return r;
    }

    private void deleteResource() {
        int id = ConsoleUtils.readInt("ID del recurso: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeResource(id),
                    "Recurso eliminado correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    private void loansMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("PRÉSTAMOS DE RECURSOS");
            System.out.println("  1. Listar préstamos a estudiantes");
            System.out.println("  2. Listar préstamos a profesores");
            System.out.println("  3. Prestar recurso a estudiante");
            System.out.println("  4. Prestar recurso a profesor");
            System.out.println("  5. Registrar devolución de estudiante");
            System.out.println("  6. Registrar devolución de profesor");
            System.out.println("  7. Préstamos de un estudiante");
            System.out.println("  8. Préstamos de un profesor");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 8);
            switch (option) {
                case 1 -> printLoans(ViewUtils.query(
                        "list_student_loans", controller::listStudentLoans));
                case 2 -> printLoans(ViewUtils.query(
                        "list_teacher_loans", controller::listTeacherLoans));
                case 3 -> lendToStudent();
                case 4 -> lendToTeacher();
                case 5 -> returnStudentLoan();
                case 6 -> returnTeacherLoan();
                case 7 -> loansByStudent();
                case 8 -> loansByTeacher();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void printLoans(List<Loan> loans) {
        ConsoleUtils.printSubHeader("PRÉSTAMOS");
        if (loans.isEmpty()) {
            ConsoleUtils.printInfo("No hay préstamos para mostrar.");
        } else {
            System.out.printf("%-4s %-26s %-24s %-12s %-12s %-10s%n",
                    "ID", "RECURSO", "BENEFICIARIO", "PRÉSTAMO", "DEVOLUCIÓN", "ESTADO");
            ViewUtils.printSeparator();
            loans.forEach(l -> System.out.printf("%-4d %-26s %-24s %-12s %-12s %-10s%n",
                    l.getIdLoan(), truncate(l.getResourceName(), 26),
                    truncate(l.getBorrowerName(), 24), l.getLoanDate(),
                    l.getExpectedReturnDate(), l.getState()));
        }
    }

    private void lendToStudent() {
        listAvailableOptions();
        Loan loan = new Loan();
        loan.setIdResource(ConsoleUtils.readInt("ID del recurso: "));
        loan.setIdStudent(ConsoleUtils.readInt("ID del estudiante: "));
        loan.setLoanDate(LocalDate.now().toString());
        loan.setExpectedReturnDate(ConsoleUtils.readDate(
                "Fecha esperada de devolución (YYYY-MM-DD): "));
        loan.setObservations(ConsoleUtils.readOptional("Observaciones: "));
        ViewUtils.executeWithId(() -> controller.lendToStudent(loan),
                "Préstamo registrado con");
        ConsoleUtils.pause();
    }

    private void lendToTeacher() {
        listAvailableOptions();
        Loan loan = new Loan();
        loan.setIdResource(ConsoleUtils.readInt("ID del recurso: "));
        loan.setIdTeacher(ConsoleUtils.readInt("ID del profesor: "));
        loan.setLoanDate(LocalDate.now().toString());
        loan.setExpectedReturnDate(ConsoleUtils.readDate(
                "Fecha esperada de devolución (YYYY-MM-DD): "));
        loan.setObservations(ConsoleUtils.readOptional("Observaciones: "));
        ViewUtils.executeWithId(() -> controller.lendToTeacher(loan),
                "Préstamo registrado con");
        ConsoleUtils.pause();
    }

    private void returnStudentLoan() {
        int id = ConsoleUtils.readInt("ID del préstamo: ");
        ViewUtils.execute(() -> controller.returnStudentLoan(id),
                "Devolución registrada correctamente.", id);
        ConsoleUtils.pause();
    }

    private void returnTeacherLoan() {
        int id = ConsoleUtils.readInt("ID del préstamo: ");
        ViewUtils.execute(() -> controller.returnTeacherLoan(id),
                "Devolución registrada correctamente.", id);
        ConsoleUtils.pause();
    }

    private void loansByStudent() {
        int id = ConsoleUtils.readInt("ID del estudiante: ");
        ViewUtils.execute(() -> printLoans(
                controller.listLoansByStudent(id)), null);
        ConsoleUtils.pause();
    }

    private void loansByTeacher() {
        int id = ConsoleUtils.readInt("ID del profesor: ");
        ViewUtils.execute(() -> printLoans(
                controller.listLoansByTeacher(id)), null);
        ConsoleUtils.pause();
    }

    private void listCategoryOptions() {
        System.out.println("  Categorías:");
        ViewUtils.query("list_resource_categories", controller::listCategories)
                .forEach(c -> System.out.println("    "
                + c.getIdCategory() + " - " + c.getName()));
    }

    private void listAvailableOptions() {
        System.out.println("  Recursos disponibles:");
        ViewUtils.query("list_available_resources", controller::listAvailableResources)
                .forEach(r -> System.out.println("    "
                + r.getIdResource() + " - " + r.getResourceCode()
                + " " + r.getName()));
    }

    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 1) + ".";
    }
}
