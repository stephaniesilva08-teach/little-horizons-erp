package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.CourseAverage;
import com.littlehorizons.erp.model.Debtor;
import com.littlehorizons.erp.model.GradeSummary;
import com.littlehorizons.erp.model.OverdueLoan;
import com.littlehorizons.erp.model.StudentGradeReport;
import java.util.List;

/**
 * Persistence contract for reports.
 */
public interface ReportDao {

    List<CourseAverage> courseAverages(int academicYear);

    List<StudentGradeReport> studentGradeReport(int idStudent, int academicYear);

    List<GradeSummary> gradeSummary(int academicYear);

    List<Debtor> debtors();

    List<OverdueLoan> overdueLoans();

    List<String> enrollmentSummary(int academicYear);
}
