package ca.as4.models;

/*
Graph model for modeling the graphs data per department
*/
public class GraphData
{
    private int semesterCode;
    private int totalCoursesTaken = 0;

    public GraphData() { }

    public GraphData(int semesterCode, int totalCoursesTaken) {
        this.semesterCode = semesterCode;
        this.totalCoursesTaken = totalCoursesTaken;
    }

    public int getSemesterCode() {
        return semesterCode;
    }

    public void setSemesterCode(int semesterCode) {
        this.semesterCode = semesterCode;
    }

    public int getTotalCoursesTaken() {
        return totalCoursesTaken;
    }

    public void setTotalCoursesTaken(int totalCoursesTaken) {
        this.totalCoursesTaken = totalCoursesTaken;
    }

    public void incrementEnrollmentTotal(int enrollmentTotal)
    {
        this.totalCoursesTaken += enrollmentTotal;
    }
}
