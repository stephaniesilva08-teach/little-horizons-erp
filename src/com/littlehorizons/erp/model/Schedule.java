package com.littlehorizons.erp.model;

/** Weekly schedule slot for a teacher assignment. */
public class Schedule {

    private int idSchedule;
    private int idAssignment;
    private String dayOfWeek;
    private String startTime;
    private String endTime;

    public Schedule() {
    }

    public Schedule(int idSchedule, int idAssignment, String dayOfWeek,
                    String startTime, String endTime) {
        this.idSchedule = idSchedule;
        this.idAssignment = idAssignment;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getIdSchedule() { return idSchedule; }
    public void setIdSchedule(int idSchedule) { this.idSchedule = idSchedule; }
    public int getIdAssignment() { return idAssignment; }
    public void setIdAssignment(int idAssignment) { this.idAssignment = idAssignment; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    @Override
    public String toString() {
        return dayOfWeek + " " + startTime + "-" + endTime;
    }
}
