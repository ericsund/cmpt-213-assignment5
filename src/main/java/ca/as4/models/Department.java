package ca.as4.models;

import java.util.ArrayList;

/*
A class to create Department objects
*/
public class Department implements Comparable<Department> {
    private long deptId;
    private String name;
    private ArrayList<Course> courses = new ArrayList<>();

    public Department() { }

    public Department(long deptId, String name) {
        this.deptId = deptId;
        this.name = name;
    }

    public void setDeptId(long deptId) {
        this.deptId = deptId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDeptId() {
        return deptId;
    }

    public String getName() {
        return name;
    }

    public void addCourse(Course course) {
        this.courses.add(course);
    }

    // overloaded this to add a course to a specific spot
    public void addCourse(int i, Course course) {
        this.courses.add(i, course);
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public Course getSpecificCourse(int i) {
        if (courses.size() == 0) {
            return null;
        }

        else {
            return courses.get(i);
        }
    }

    public void removeSpecificCourse(int i) {
        if (courses.size() == 0) {
            return;
        }

        else {
            courses.remove(i);
        }
    }

    public Course getLastCourse() {
        if (courses.size() == 0) {
            return null;
        }

        else {
            return courses.get(courses.size() - 1);
        }
    }

    public void removeLastCourse() {
        if (courses.size() > 0)
        {
            courses.remove(courses.size() - 1);
        }
    }

    @Override
    public int compareTo(Department other)
    {
        return name.compareTo(other.name);
    }
}
