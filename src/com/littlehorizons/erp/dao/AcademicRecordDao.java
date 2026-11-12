package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.SubjectAverage;
import java.math.BigDecimal;
import java.util.List;

/**
 * Persistence contract for academic records.
 */
public interface AcademicRecordDao {

    int insert(AcademicRecord record);

    void update(AcademicRecord record);

    boolean delete(int idRecord);

    AcademicRecord findById(int idRecord);

    List<AcademicRecord> findAll();

    List<AcademicRecord> findByStudent(int idStudent, int academicYear);

    List<AcademicRecord> findHistoryByStudent(int idStudent);

    List<SubjectAverage> findSubjectAverages(int idStudent, int academicYear);

    BigDecimal averageByStudent(int idStudent, int academicYear, String period);

    BigDecimal overallAverageByStudent(int idStudent, int academicYear);
}
