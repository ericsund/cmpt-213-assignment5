package ca.as4.models;

/*
Section model to store the total enrollment information per class type
*/
public class Section
{
    private long sectionID;
    private String type;
    private int totalCoursesTaken = 0;
    private int enrollmentCap = 0;

    public Section() {}

    public Section(long sectionID, String type, int totalCoursesTaken, int enrollmentCap) {
        this.sectionID = sectionID;
        this.type = type;
        this.totalCoursesTaken = totalCoursesTaken;
        this.enrollmentCap = enrollmentCap;
    }

    public long getSectionID() {
        return sectionID;
    }

    public void setSectionID(long sectionID) {
        this.sectionID = sectionID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTotalCoursesTaken() {
        return totalCoursesTaken;
    }

    public void setTotalCoursesTaken(int totalCoursesTaken) {
        this.totalCoursesTaken = totalCoursesTaken;
    }

    public int getEnrollmentCap() {
        return enrollmentCap;
    }

    public void setEnrollmentCap(int enrollmentCap) {
        this.enrollmentCap = enrollmentCap;
    }
}
