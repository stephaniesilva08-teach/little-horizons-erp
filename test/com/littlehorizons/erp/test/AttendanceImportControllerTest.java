package com.littlehorizons.erp.test;

import com.littlehorizons.erp.controller.AttendanceImportController;
import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.AttendanceImportResult;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.DataAccessException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the bulk attendance CSV import without a database.
 */
public final class AttendanceImportControllerTest {

    private AttendanceImportControllerTest() {
    }

    public static void register(TestSuite suite) {
        suite.add("CSV import stores valid rows and counts invalid ones", () -> {
            Path file = Files.createTempFile("asistencias", ".csv");
            try {
                Files.writeString(file, String.join("\n",
                        AttendanceImportController.HEADER,
                        "1;5;2024-10-07;Presente",
                        "999;5;2024-10-07;Ausente",
                        "3;5;2024-10-07;Presente",
                        "4;99;2024-10-07;Tardanza",
                        "5;8;2024-10-07;presente",
                        "6;8;2024-10-07;Excusa Médica",
                        "7;8;2024-10-07;Presente"));
                AtomicInteger inserted = new AtomicInteger();
                AttendanceImportResult result = controller(file, inserted, false)
                        .importAttendances();

                Assert.equals(AttendanceImportResult.Status.COMPLETED,
                        result.status(), "the import must complete");
                Assert.equals(7, result.totalLines(), "seven data lines");
                Assert.equals(4, result.successful(), "four valid lines");
                Assert.equals(3, result.failed(), "three invalid lines");
                Assert.equals(4, inserted.get(), "only valid rows are stored");
            } finally {
                Files.deleteIfExists(file);
            }
        });

        suite.add("CSV import ignores empty lines and validates dates and field count", () -> {
            Path file = Files.createTempFile("asistencias", ".csv");
            try {
                Files.writeString(file, String.join("\n",
                        AttendanceImportController.HEADER,
                        "",
                        "1;5;2024-10-07;Presente",
                        "   ",
                        "2;5;2024-02-30;Presente",
                        "3;5;07/10/2024;Presente",
                        "4;5;2024-10-07;Presente;extra",
                        ""));
                AtomicInteger inserted = new AtomicInteger();
                AttendanceImportResult result = controller(file, inserted, false)
                        .importAttendances();

                Assert.equals(4, result.totalLines(),
                        "empty lines must not be counted");
                Assert.equals(1, result.successful(), "only one line is valid");
                Assert.equals(3, result.failed(), "three lines are invalid");
                Assert.equals(1, inserted.get(), "invalid rows are not stored");
            } finally {
                Files.deleteIfExists(file);
            }
        });

        suite.add("CSV import reports a missing file", () -> {
            Path file = Path.of("no-existe-" + System.nanoTime() + ".csv");
            AttendanceImportResult result = controller(file, new AtomicInteger(), false)
                    .importAttendances();
            Assert.equals(AttendanceImportResult.Status.FILE_NOT_FOUND,
                    result.status(), "missing files must be reported");
            Assert.equals(0, result.totalLines(), "nothing is processed");
        });

        suite.add("CSV import rejects an invalid header", () -> {
            Path file = Files.createTempFile("asistencias", ".csv");
            try {
                Files.writeString(file, String.join("\n",
                        "estudiante;materia;fecha;asistencia",
                        "1;5;2024-10-07;Presente"));
                AtomicInteger inserted = new AtomicInteger();
                AttendanceImportResult result = controller(file, inserted, false)
                        .importAttendances();
                Assert.equals(AttendanceImportResult.Status.INVALID_HEADER,
                        result.status(), "the header must match exactly");
                Assert.equals(0, inserted.get(), "nothing is stored");
            } finally {
                Files.deleteIfExists(file);
            }
        });

        suite.add("CSV import counts insertion failures as failed lines", () -> {
            Path file = Files.createTempFile("asistencias", ".csv");
            try {
                Files.writeString(file, String.join("\n",
                        AttendanceImportController.HEADER,
                        "1;5;2024-10-07;Presente"));
                AttendanceImportResult result = controller(file, new AtomicInteger(), true)
                        .importAttendances();
                Assert.equals(1, result.totalLines(), "one data line");
                Assert.equals(0, result.successful(), "the insert failed");
                Assert.equals(1, result.failed(), "the failure is counted");
            } finally {
                Files.deleteIfExists(file);
            }
        });
    }

    private static AttendanceImportController controller(Path file, AtomicInteger inserted,
                                                         boolean failInsert) {
        StudentDao students = proxy(StudentDao.class, (proxy, method, args) -> {
            if (method.getName().equals("exists")) {
                return Set.of(1, 3, 4, 5, 6, 7).contains((Integer) args[0]);
            }
            throw new UnsupportedOperationException(method.getName());
        });
        SubjectDao subjects = proxy(SubjectDao.class, (proxy, method, args) -> {
            if (method.getName().equals("findById")) {
                int id = (Integer) args[0];
                return id == 5 || id == 8 ? new Subject(id) : null;
            }
            throw new UnsupportedOperationException(method.getName());
        });
        AttendanceDao attendance = proxy(AttendanceDao.class, (proxy, method, args) -> {
            if (method.getName().equals("insert")) {
                if (failInsert) {
                    throw new DataAccessException("duplicate attendance", null);
                }
                inserted.incrementAndGet();
                ((Attendance) args[0]).setIdAttendance(inserted.get());
                return inserted.get();
            }
            throw new UnsupportedOperationException(method.getName());
        });
        return new AttendanceImportController(students, subjects, attendance, file);
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(),
                new Class<?>[]{type}, handler));
    }
}
