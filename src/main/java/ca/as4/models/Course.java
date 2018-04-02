package ca.as4.models;

public class Course
{
    private long courseId;
    private String catalogNumber;

    public Course() {}

    public Course(long courseId, String catalogNumber) {
        this.courseId = courseId;
        this.catalogNumber = catalogNumber;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public long getCourseId() {
        return courseId;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }
}
