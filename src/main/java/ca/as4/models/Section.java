package ca.as4.models;

public class Section
{
    private long sectionID;
    private String type;
    private int enrollmentTotal;
    private int enrollmentCap;

    public Section() {}

    public Section(long sectionID, String type, int enrollmentTotal, int enrollmentCap) {
        this.sectionID = sectionID;
        this.type = type;
        this.enrollmentTotal = enrollmentTotal;
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
