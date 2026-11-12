package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.AttendanceAlert;
import com.littlehorizons.erp.model.AttendanceSummary;
import java.util.List;

/**
 * Persistence contract for attendance records.
 */
public interface AttendanceDao {

    int insert(Attendance attendance);

    void update(Attendance attendance);

    boolean delete(int idAttendance);

    Attendance findById(int idAttendance);

    List<Attendance> findByStudent(int idStudent);

    List<AttendanceSummary> summaryByStudent(int academicYear);

    List<AttendanceAlert> findRecurringAbsences(int minimumAbsences);
}
