package com.littlehorizons.erp.test;

import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.Parent;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.test.fake.InMemoryParentDao;
import com.littlehorizons.erp.test.fake.InMemoryStudentDao;

/**
 * Tests the student registration rules with injected in-memory DAOs.
 */
public final class StudentControllerTest {

    private StudentControllerTest() {
    }

    public static void register(TestSuite suite) {
        suite.add("Registration requires a guardian", () -> {
            Fixture fixture = new Fixture();
            IllegalArgumentException error = Assert.assertThrows(
                    IllegalArgumentException.class,
                    () -> fixture.controller.register(validStudent("1020304050"), 0),
                    "registering without a guardian must fail");
            Assert.isTrue(error.getMessage().contains("acudiente"),
                    "the message should mention the guardian");
            Assert.equals(0, fixture.students.count(), "no student should be stored");
        });
        suite.add("Registration rejects an unknown guardian", () -> {
            Fixture fixture = new Fixture();
            Assert.assertThrows(IllegalArgumentException.class,
                    () -> fixture.controller.register(validStudent("1020304051"), 99),
                    "unknown guardians must be rejected");
            Assert.equals(0, fixture.students.count(), "no student should be stored");
        });
        suite.add("Registration rejects a duplicated document", () -> {
            Fixture fixture = new Fixture();
            int guardian = fixture.guardian();
            fixture.controller.register(validStudent("1020304052"), guardian);
            Assert.assertThrows(IllegalArgumentException.class,
                    () -> fixture.controller.register(validStudent("1020304052"), guardian),
                    "documents must be unique");
            Assert.equals(1, fixture.students.count(), "only the first student is stored");
        });
        suite.add("Registration stores the student and its guardian", () -> {
            Fixture fixture = new Fixture();
            int guardian = fixture.guardian();
            int id = fixture.controller.register(validStudent("1020304053"), guardian);
            Assert.isTrue(id > 0, "a new identifier must be returned");
            Assert.notNull(fixture.controller.findById(id), "the student must be stored");
            Assert.equals(1, fixture.students.guardians().size(),
                    "the guardian link must be stored");
        });
        suite.add("Removing an unknown student fails clearly", () -> {
            Fixture fixture = new Fixture();
            Assert.assertThrows(IllegalArgumentException.class,
                    () -> fixture.controller.remove(12345),
                    "unknown students cannot be removed");
        });
        suite.add("Retiring a stored student preserves its history", () -> {
            Fixture fixture = new Fixture();
            int id = fixture.controller.register(validStudent("1020304054"), fixture.guardian());
            fixture.controller.remove(id);
            Assert.equals("Retirado", fixture.controller.findById(id).getEnrollmentState(),
                    "the student should be retained with retired status");
        });
    }

    static Student validStudent(String documentNumber) {
        Student student = new Student();
        student.setDocumentType("TI");
        student.setDocumentNumber(documentNumber);
        student.setFirstName("Ana");
        student.setFirstLastName("Pérez");
        student.setBirthDate("2015-05-10");
        student.setGender("F");
        student.setEntryDate("2026-02-01");
        student.setEnrollmentState("Activo");
        return student;
    }

    private static final class Fixture {

        private final InMemoryStudentDao students = new InMemoryStudentDao();
        private final InMemoryParentDao parents = new InMemoryParentDao();
        private final StudentController controller =
                new StudentController(students, parents);

        private int guardian() {
            Parent parent = new Parent();
            parent.setDocumentType("CC");
            parent.setDocumentNumber("79555111");
            parent.setFirstName("Laura");
            parent.setFirstLastName("Gómez");
            return parents.insert(parent);
        }
    }
}
