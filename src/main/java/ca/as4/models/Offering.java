package ca.as4.models;

public class Offering
{
    private long courseOfferingId;
    private String location;
    private String instructors;
    private int year;
    private int semesterCode;
    private String term;

    public Offering() { }

    public Offering(long courseOfferingId, String location, String instructors, int year, int semesterCode, String term) {
        this.courseOfferingId = courseOfferingId;
        this.location = location;
        this.instructors = instructors;
        this.year = year;
        this.semesterCode = semesterCode;
        this.term = term;
    }

    public void setCourseOfferingId(long courseOfferingId) {
        this.courseOfferingId = courseOfferingId;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setInstructors(String instructors) {
        this.instructors = instructors;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setSemesterCode(int semesterCode) {
        this.semesterCode = semesterCode;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public long getCourseOfferingId() {
        return courseOfferingId;
    }

    public String getLocation() {
        return location;
    }

    public String getInstructors() {
        return instructors;
    }

    public int getYear() {
        return year;
    }

    public int getSemesterCode() {
        return semesterCode;
    }

    public String getTerm() {
        return term;
    }
}
