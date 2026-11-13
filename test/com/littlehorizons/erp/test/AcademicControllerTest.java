package com.littlehorizons.erp.test;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.dao.AssignmentDao;
import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.dao.GradeDao;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.SubjectEnrollment;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests that grades and attendance require a valid subject enrollment and,
 * when applicable, a teacher assignment for the student's section.
 */
public final class AcademicControllerTest {

    private AcademicControllerTest() {
    }

    public static void register(TestSuite suite) {
        suite.add("Grade registration requires an active enrollment", () -> {
            AtomicBoolean inserted = new AtomicBoolean();
            AcademicController controller = controller(enrollmentDao(null), true, inserted, new AtomicBoolean());
            IllegalArgumentException error = Assert.assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.registerGrade(validGrade("I")),
                    "a grade without enrollment must fail");
            Assert.isTrue(error.getMessage().contains("no está inscrito"),
                    "the message should mention the missing enrollment");
            Assert.isFalse(inserted.get(), "the grade must not be stored");
        });
        suite.add("Grade registration requires a teacher assignment", () -> {
            AtomicBoolean inserted = new AtomicBoolean();
            AcademicController controller = controller(enrollmentDao(enrollment("A")), false, inserted,
                    new AtomicBoolean());
            IllegalArgumentException error = Assert.assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.registerGrade(validGrade("I")),
                    "a grade without teacher assignment must fail");
            Assert.isTrue(error.getMessage().contains("profesor"),
                    "the message should mention the teacher assignment");
            Assert.isFalse(inserted.get(), "the grade must not be stored");
        });
        suite.add("Grade registration stores a valid record", () -> {
            AtomicBoolean inserted = new AtomicBoolean();
            AcademicController controller = controller(enrollmentDao(enrollment("A")), true, inserted,
                    new AtomicBoolean());
            int id = controller.registerGrade(validGrade("I"));
            Assert.equals(7, id, "the record identifier must be returned");
            Assert.isTrue(inserted.get(), "the grade must be stored");
        });
        suite.add("Final grade accepts an enrollment from any period", () -> {
            AtomicBoolean inserted = new AtomicBoolean();
            String[] requestedPeriod = new String[1];
            EnrollmentDao enrollments = proxy(EnrollmentDao.class, (proxy, method, args) -> {
                if (method.getName().equals("findActive")) {
                    requestedPeriod[0] = (String) args[3];
                    return enrollment("A");
                }
                throw new UnsupportedOperationException(method.getName());
            });
            AcademicController controller = controller(enrollments, true, inserted,
                    new AtomicBoolean());
            controller.registerGrade(validGrade("Final"));
            Assert.equals(null, requestedPeriod[0],
                    "Final grades must look for any period in the year");
            Assert.isTrue(inserted.get(), "the final grade must be stored");
        });
        suite.add("Attendance registration requires an active enrollment", () -> {
            AtomicBoolean inserted = new AtomicBoolean();
            AcademicController controller = controller(enrollmentDao(null), true, new AtomicBoolean(), inserted);
            IllegalArgumentException error = Assert.assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.registerAttendance(validAttendance()),
                    "attendance without enrollment must fail");
            Assert.isTrue(error.getMessage().contains("no está inscrito"),
                    "the message should mention the missing enrollment");
            Assert.isFalse(inserted.get(), "the attendance must not be stored");
        });
        suite.add("Attendance registration requires a teacher assignment", () -> {
            AtomicBoolean inserted = new AtomicBoolean();
            AcademicController controller = controller(enrollmentDao(enrollment("A")), false,
                    new AtomicBoolean(), inserted);
            IllegalArgumentException error = Assert.assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.registerAttendance(validAttendance()),
                    "attendance with an unassigned teacher must fail");
            Assert.isTrue(error.getMessage().contains("profesor"),
                    "the message should mention the teacher assignment");
            Assert.isFalse(inserted.get(), "the attendance must not be stored");
        });
        suite.add("Attendance registration stores a valid record", () -> {
            AtomicBoolean inserted = new AtomicBoolean();
            AcademicController controller = controller(enrollmentDao(enrollment("A")), true,
                    new AtomicBoolean(), inserted);
            int id = controller.registerAttendance(validAttendance());
            Assert.equals(9, id, "the attendance identifier must be returned");
            Assert.isTrue(inserted.get(), "the attendance must be stored");
        });
    }

    private static AcademicController controller(EnrollmentDao enrollmentDao,
                                                 boolean assigned,
                                                 AtomicBoolean gradeInserted,
                                                 AtomicBoolean attendanceInserted) {
        return new AcademicController(recordDao(gradeInserted),
                attendanceDao(attendanceInserted), assignmentDao(assigned),
                enrollmentDao, unused(GradeDao.class));
    }

    private static EnrollmentDao enrollmentDao(SubjectEnrollment enrollment) {
        return proxy(EnrollmentDao.class, (proxy, method, args) -> {
            if (method.getName().equals("findActive")) {
                return enrollment;
            }
            throw new UnsupportedOperationException(method.getName());
        });
    }

    private static AssignmentDao assignmentDao(boolean assigned) {
        return proxy(AssignmentDao.class, (proxy, method, args) -> {
            if (method.getName().equals("hasAssignment")) {
                return assigned;
            }
            throw new UnsupportedOperationException(method.getName());
        });
    }

    private static AcademicRecordDao recordDao(AtomicBoolean inserted) {
        return proxy(AcademicRecordDao.class, (proxy, method, args) -> {
            if (method.getName().equals("insert")) {
                inserted.set(true);
                return 7;
            }
            throw new UnsupportedOperationException(method.getName());
        });
    }

    private static AttendanceDao attendanceDao(AtomicBoolean inserted) {
        return proxy(AttendanceDao.class, (proxy, method, args) -> {
            if (method.getName().equals("insert")) {
                inserted.set(true);
                return 9;
            }
            throw new UnsupportedOperationException(method.getName());
        });
    }

    private static SubjectEnrollment enrollment(String section) {
        SubjectEnrollment enrollment = new SubjectEnrollment();
        enrollment.setIdEnrollment(1);
        enrollment.setIdStudent(10);
        enrollment.setIdSubject(20);
        enrollment.setAcademicYear(2026);
        enrollment.setAcademicPeriod("I");
        enrollment.setSection(section);
        enrollment.setState("Activa");
        return enrollment;
    }

    private static AcademicRecord validGrade(String period) {
        AcademicRecord record = new AcademicRecord();
        record.setIdStudent(10);
        record.setIdSubject(20);
        record.setIdTeacher(30);
        record.setAcademicYear(2026);
        record.setAcademicPeriod(period);
        record.setGrade(new BigDecimal("4.5"));
        record.setRecordDate("2026-05-10");
        return record;
    }

    private static Attendance validAttendance() {
        Attendance attendance = new Attendance();
        attendance.setIdStudent(10);
        attendance.setIdSubject(20);
        attendance.setAttendanceDate("2026-05-10");
        attendance.setState("Presente");
        attendance.setRegisteredBy(30);
        return attendance;
    }

    private static <T> T unused(Class<T> type) {
        return proxy(type, (proxy, method, args) -> {
            throw new UnsupportedOperationException(method.getName());
        });
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(),
                new Class<?>[]{type}, handler));
    }
}
