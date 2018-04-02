package ca.as4.models;

import java.util.ArrayList;

public class Course
{
    private long courseId;
    private String catalogNumber;
    private ArrayList<Offering> offerings = new ArrayList<>();

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

    public void setOfferings(Offering offering) {
        this.offerings.add(offering);
    }

    public ArrayList<Offering> getOfferings() {
        return offerings;
    }
}