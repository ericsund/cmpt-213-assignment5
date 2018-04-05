package ca.as4.models;

public class Graph
{
    private int semester;
    private int enrollmentTotal = 0;
    private int enrollmentCap = 0;

    public Graph() { }

    public Graph(int semester, int enrollmentTotal, int enrollmentCap) {
        this.semester = semester;
        this.enrollmentTotal = enrollmentTotal;
        this.enrollmentCap = enrollmentCap;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getEnrollmentTotal() {
        return enrollmentTotal;
    }

    public void setEnrollmentTotal(int enrollmentTotal) {
        this.enrollmentTotal = enrollmentTotal;
    }

    public int getEnrollmentCap() {
        return enrollmentCap;
    }

    public void setEnrollmentCap(int enrollmentCap) {
        this.enrollmentCap = enrollmentCap;
    }
}
