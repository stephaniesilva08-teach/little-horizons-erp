package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ParentController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.Parent;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.ConsoleUtils;

import java.util.List;

/**
 * Console view for student management.
 */
public class StudentView {

    private final StudentController controller;
    private final ParentController parentController;

    public StudentView() {
        this.controller = new StudentController();
        this.parentController = new ParentController();
    }

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("GESTIÓN DE ESTUDIANTES");
            System.out.println("  1. Listar todos los estudiantes");
            System.out.println("  2. Buscar estudiante");
            System.out.println("  3. Registrar estudiante");
            System.out.println("  4. Actualizar estudiante");
            System.out.println("  5. Retirar estudiante");
            System.out.println("  6. Asociar acudiente");
            System.out.println("  0. Volver al menú principal");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 6);
            switch (option) {
                case 1 -> listAll();
                case 2 -> search();
                case 3 -> register();
                case 4 -> update();
                case 5 -> delete();
                case 6 -> assignGuardian();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listAll() {
        ConsoleUtils.printSubHeader("LISTADO DE ESTUDIANTES");
        List<Student> students = ViewUtils.query("list_students", controller::listAll);
        if (students.isEmpty()) {
            ConsoleUtils.printInfo("No hay estudiantes registrados.");
        } else {
            System.out.printf("%-4s %-12s %-30s %-6s %-10s %-10s%n",
                    "ID", "DOCUMENTO", "NOMBRE", "GÉNERO", "ESTADO", "INGRESO");
            ViewUtils.printSeparator();
            students.forEach(s -> System.out.printf("%-4d %-12s %-30s %-6s %-10s %-10s%n",
                    s.getIdStudent(), s.getDocumentNumber(), truncate(s.getFullName(), 30),
                    s.getGender(), s.getEnrollmentState(), s.getEntryDate()));
        }
        ConsoleUtils.pause();
    }

    private void search() {
        ConsoleUtils.printSubHeader("BUSCAR ESTUDIANTE");
        String text = ConsoleUtils.readNonEmpty("Documento o nombre: ");
        ViewUtils.execute(() -> {
            List<Student> students = controller.search(text);
            if (students.isEmpty()) {
                ConsoleUtils.printInfo("No se encontraron coincidencias.");
            } else {
                students.forEach(s -> System.out.println("  [" + s.getIdStudent() + "] "
                        + s.getFullName() + " - " + s.getDocumentNumber()
                        + " - " + s.getEnrollmentState()));
            }
        }, null);
        ConsoleUtils.pause();
    }

    private void register() {
        ConsoleUtils.printSubHeader("REGISTRAR ESTUDIANTE");
        ConsoleUtils.printInfo("El acudiente debe existir previamente (menú Acudientes).");
        Student student = readStudent(null);
        int idParent = ConsoleUtils.readInt("ID del acudiente responsable: ");
        ViewUtils.executeWithId(() -> controller.register(student, idParent),
                "Estudiante registrado con");
        ConsoleUtils.pause();
    }

    private void update() {
        ConsoleUtils.printSubHeader("ACTUALIZAR ESTUDIANTE");
        int id = ConsoleUtils.readInt("ID del estudiante: ");
        Student student = ViewUtils.query("find_student", id, () -> controller.findById(id));
        if (student == null) {
            ConsoleUtils.printError("El estudiante no existe.");
            ConsoleUtils.pause();
            return;
        }
        readStudent(student);
        ViewUtils.execute(() -> controller.update(student),
                "Estudiante actualizado correctamente.", id);
        ConsoleUtils.pause();
    }

    private void delete() {
        ConsoleUtils.printSubHeader("ELIMINAR ESTUDIANTE");
        int id = ConsoleUtils.readInt("ID del estudiante: ");
        if (ViewUtils.query("find_student", id, () -> controller.findById(id)) == null) {
            ConsoleUtils.printError("El estudiante no existe.");
        } else if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.remove(id),
                    "Estudiante retirado; se conserva su historial.", id);
        }
        ConsoleUtils.pause();
    }

    private void assignGuardian() {
        ConsoleUtils.printSubHeader("ASOCIAR ACUDIENTE");
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        int idParent = ConsoleUtils.readInt("ID del acudiente: ");
        boolean main = ConsoleUtils.readYesNo("¿Es acudiente principal?");
        boolean emergency = ConsoleUtils.readYesNo("¿Es contacto de emergencia?");
        ViewUtils.execute(() -> controller.assignGuardian(idStudent, idParent,
                main, emergency), "Acudiente asociado correctamente.", idStudent);
        ConsoleUtils.pause();
    }

    private Student readStudent(Student current) {
        Student s = current == null ? new Student() : current;
        s.setDocumentType(ConsoleUtils.readEnum("Tipo de documento",
                "TI", "CC", "CE", "Pasaporte"));
        s.setDocumentNumber(ViewUtils.readWithDefault("Número de documento",
                s.getDocumentNumber()));
        s.setFirstName(ViewUtils.readWithDefault("Primer nombre", s.getFirstName()));
        s.setSecondName(ViewUtils.readWithDefault("Segundo nombre (opcional)",
                s.getSecondName()));
        s.setFirstLastName(ViewUtils.readWithDefault("Primer apellido",
                s.getFirstLastName()));
        s.setSecondLastName(ViewUtils.readWithDefault("Segundo apellido (opcional)",
                s.getSecondLastName()));
        s.setBirthDate(ViewUtils.readDateWithDefault("Fecha de nacimiento (YYYY-MM-DD)",
                s.getBirthDate()));
        s.setGender(ConsoleUtils.readEnum("Género", "M", "F", "Otro"));
        s.setAddress(ViewUtils.readWithDefault("Dirección", s.getAddress()));
        s.setPhone(ViewUtils.readWithDefault("Teléfono", s.getPhone()));
        s.setEmail(ViewUtils.readWithDefault("Correo electrónico", s.getEmail()));
        s.setBloodType(ViewUtils.readWithDefault("Tipo de sangre", s.getBloodType()));
        s.setAllergies(ViewUtils.readWithDefault("Alergias", s.getAllergies()));
        s.setMedicalConditions(ViewUtils.readWithDefault("Condiciones médicas",
                s.getMedicalConditions()));
        s.setEntryDate(ViewUtils.readDateWithDefault("Fecha de ingreso (YYYY-MM-DD)",
                s.getEntryDate()));
        s.setEnrollmentState(ConsoleUtils.readEnum("Estado", "Activo", "Graduado",
                "Retirado", "Suspendido"));
        return s;
    }

    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 1) + ".";
    }
}
