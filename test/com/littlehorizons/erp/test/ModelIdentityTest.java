package com.littlehorizons.erp.test;

import com.littlehorizons.erp.model.Parent;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Teacher;

import java.util.HashSet;
import java.util.Set;

/** Tests stable identity and display-name formatting for mutable entities. */
public final class ModelIdentityTest {

    private ModelIdentityTest() {
    }

    public static void register(TestSuite suite) {
        suite.add("Optional name parts are omitted", () -> {
            Student student = new Student();
            student.setFirstName("  Ana ");
            student.setSecondName(" ");
            student.setFirstLastName("Pérez");
            student.setSecondLastName(null);
            Assert.equals("Ana Pérez", student.getFullName(),
                    "blank optional name components must be omitted");

            Parent parent = new Parent();
            parent.setFirstName("Luis");
            parent.setFirstLastName("Gómez");
            Assert.equals("Luis Gómez", parent.getFullName(),
                    "missing parent names must not render as null");

            Teacher teacher = new Teacher();
            teacher.setFirstName("Eva");
            teacher.setFirstLastName("Díaz");
            Assert.equals("Eva Díaz", teacher.getFullName(),
                    "missing teacher names must not produce extra spaces");
        });

        suite.add("Persisted students use ID equality and matching hash codes", () -> {
            Student first = new Student(42);
            first.setDocumentNumber("100");
            Student same = new Student(42);
            same.setDocumentNumber("200");
            Assert.isTrue(first.equals(same), "the same persisted ID must mean same entity");
            Assert.equals(first.hashCode(), same.hashCode(),
                    "equal entities must have equal hash codes");

            Set<Student> students = new HashSet<>();
            students.add(first);
            first.setDocumentNumber("changed");
            Assert.isTrue(students.contains(first),
                    "changing a non-identity field must not break set lookup");
        });

        suite.add("Transient entities retain object identity", () -> {
            Student first = new Student();
            Student second = new Student();
            Assert.isFalse(first.equals(second),
                    "two unsaved entities must not compare equal by default ID zero");
        });
    }
}
