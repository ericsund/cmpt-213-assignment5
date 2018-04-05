package ca.as4.models;

import java.util.ArrayList;

public class Data {
    private int semester;
    private String subjectName;
    private String catalogNumber;
    private String location;
    private int enrollmentCap;
    private int enrollmentTotal;
    private ArrayList<String> instructors = new ArrayList<>();
    private String component;
    private String instructor;
    private ArrayList<String> allOfferings = new ArrayList<>();

    private int[] enrollments = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private boolean[] components = new boolean[] {false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            false};

    public Data() { }

    public Data(int semester, String subjectName, String catalogNumber, String location, int enrollmentCap, String component, int enrollmentTotal, String instructor) {
        this.semester = semester;
        this.subjectName = subjectName;
        this.catalogNumber = catalogNumber;
        this.location = location;
        this.enrollmentCap = enrollmentCap;
        this.enrollmentTotal = enrollmentTotal;
        this.component = component;
        this.instructor = instructor;
    }

    public Data(int semester, String subjectName, String catalogNumber, String location, int enrollmentCap,
                int enrollmentTotal, ArrayList<String> instructors, String component)
    {
        this.semester = semester;
        this.subjectName = subjectName;
        this.catalogNumber = catalogNumber;
        this.location = location;
        this.enrollmentCap = enrollmentCap;
        this.enrollmentTotal = enrollmentTotal;
        this.instructors = instructors;
        this.component = component;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getEnrollmentCap() {
        return enrollmentCap;
    }

    public void setEnrollmentCap(int enrollmentCap) {
        this.enrollmentCap = enrollmentCap;
    }

    public int getEnrollmentTotal() {
        return enrollmentTotal;
    }

    public void setEnrollmentTotal(int enrollmentTotal) {
        this.enrollmentTotal = enrollmentTotal;
    }

    public ArrayList<String> getInstructors() {
        return instructors;
    }

    public void setInstructors(ArrayList<String> instructors) {
        this.instructors = instructors;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public int[] getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(int[] enrollments) {
        this.enrollments = enrollments;
    }

    public boolean[] getComponents() {
        return components;
    }

    public void setComponents(boolean[] components) {
        this.components = components;
    }

    public ArrayList<String> getAllOfferings() {
        return allOfferings;
    }

    public void setAllOfferings(ArrayList<String> allOfferings) {
        this.allOfferings = allOfferings;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }
}
