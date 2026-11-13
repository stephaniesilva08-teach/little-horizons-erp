package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.dao.ReportDao;
import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.dao.JdbcResourceDao;
import com.littlehorizons.erp.dao.JdbcAttendanceDao;
import com.littlehorizons.erp.dao.JdbcReportDao;
import com.littlehorizons.erp.model.AttendanceSummary;
import com.littlehorizons.erp.model.CourseAverage;
import com.littlehorizons.erp.model.Debtor;
import com.littlehorizons.erp.model.GradeSummary;
import com.littlehorizons.erp.model.OverdueLoan;
import com.littlehorizons.erp.model.StudentGradeReport;
import com.littlehorizons.erp.util.Validator;

import java.util.List;

/**
 * Business rules for management reports.
 */
public class ReportController {

    private final ReportDao reportDao;
    private final AttendanceDao attendanceDao;
    private final ResourceDao resourceDao;

    public ReportController() {
        this(new JdbcReportDao(), new JdbcAttendanceDao(), new JdbcResourceDao());
    }

    public ReportController(ReportDao reportDao, AttendanceDao attendanceDao, ResourceDao resourceDao) {
        this.reportDao = reportDao;
        this.attendanceDao = attendanceDao;
        this.resourceDao = resourceDao;
    }

    public List<CourseAverage> courseAverages(int academicYear) {
        requireYear(academicYear);
        return reportDao.courseAverages(academicYear);
    }

    public List<StudentGradeReport> studentGradeReport(int idStudent, int academicYear) {
        if (idStudent <= 0) {
            throw new IllegalArgumentException("El estudiante es obligatorio.");
        }
        requireYear(academicYear);
        return reportDao.studentGradeReport(idStudent, academicYear);
    }

    public List<GradeSummary> gradeSummary(int academicYear) {
        requireYear(academicYear);
        return reportDao.gradeSummary(academicYear);
    }

    public List<Debtor> debtors() {
        return reportDao.debtors();
    }

    public List<OverdueLoan> overdueLoans() {
        return reportDao.overdueLoans();
    }

    public List<AttendanceSummary> attendanceSummary(int academicYear) {
        requireYear(academicYear);
        return attendanceDao.summaryByStudent(academicYear);
    }

    public List<String> enrollmentSummary(int academicYear) {
        requireYear(academicYear);
        return reportDao.enrollmentSummary(academicYear);
    }

    private void requireYear(int year) {
        if (!Validator.isInRange(year, 2000, 2100)) {
            throw new IllegalArgumentException("El año académico es inválido.");
        }
    }
}
