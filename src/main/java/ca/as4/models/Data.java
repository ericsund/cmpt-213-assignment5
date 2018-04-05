package ca.as4.models;

import java.util.ArrayList;
import java.util.Comparator;

public class Data {
    private int semester;
    private String subject;
    private String catalogNumber;
    private String location;
    private int enrollmentCapacity;
    private int enrollmentTotal;
    private ArrayList<String> instructors;
    private String componentCode;
    private ArrayList<String> allOfferrings = new ArrayList<>();

    private int[] enrollments = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private boolean[] components = new boolean[] {false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            false};

    public Data() { }

    public Data(int semester, String subject, String catalogNumber, String location, int enrollmentCapacity,
                int enrollmentTotal, ArrayList<String> instructors, String componentCode)
    {
        this.semester = semester;
        this.subject = subject;
        this.catalogNumber = catalogNumber;
        this.location = location;
        this.enrollmentCapacity = enrollmentCapacity;
        this.enrollmentTotal = enrollmentTotal;
        this.instructors = instructors;
        this.componentCode = componentCode;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    public int getEnrollmentCapacity() {
        return enrollmentCapacity;
    }

    public void setEnrollmentCapacity(int enrollmentCapacity) {
        this.enrollmentCapacity = enrollmentCapacity;
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

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
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

    public ArrayList<String> getAllOfferrings() {
        return allOfferrings;
    }

    public void setAllOfferrings(ArrayList<String> allOfferrings) {
        this.allOfferrings = allOfferrings;
    }
}
