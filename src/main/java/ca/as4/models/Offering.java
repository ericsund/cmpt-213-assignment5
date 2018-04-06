package ca.as4.models;

import java.util.ArrayList;

public class Offering implements Comparable<Offering>
{
    private long courseOfferingId;
    private String location;
    private String instructors;
    private int year;
    private int semesterCode;
    private String term;
    private int[] enrollments;
    private boolean[] components;
    private String componentCode;
    private int enrollmentTotal;
    private int enrollmentCap;
    private ArrayList<Section> sections = new ArrayList<>();

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

    public void setEnrollments(int[] enrollments) {
        this.enrollments = enrollments;
    }

    public void setComponents(boolean[] components) {
        this.components = components;
    }

    public void setSections(ArrayList<Section> sections) {
        this.sections = sections;
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

    public int[] getEnrollments() {
        return enrollments;
    }

    public boolean[] getComponents() {
        return components;
    }

    public ArrayList<Section> getSections() {
        return sections;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public String getComponentCode() {
        return componentCode;
    }

    public int getEnrollmentTotal() {
        return enrollmentTotal;
    }

    public int getEnrollmentCap() {
        return enrollmentCap;
    }

    public void setEnrollmentTotal(int enrollmentTotal) {
        this.enrollmentTotal = enrollmentTotal;
    }

    public void setEnrollmentCap(int enrollmentCap) {
        this.enrollmentCap = enrollmentCap;
    }

    @Override
    public int compareTo(Offering other)
    {
        return this.semesterCode - other.getSemesterCode();
    }
}
