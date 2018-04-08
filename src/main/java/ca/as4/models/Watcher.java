package ca.as4.models;

import java.text.SimpleDateFormat;
import java.util.*;

public class Watcher implements Iterable<Offering>
{
    private ArrayList<Department> departments = new ArrayList<>();

    private ArrayList<String> events = new ArrayList<>();

    private ArrayList<Watcher> observers = new ArrayList<>();
    private ArrayList<Offering> list = new ArrayList<>();

    private long id;

    private Department department;
    private long deptId;
    private String name;

    private Course course;
    private long courseId;
    private String catalogNumber;

    public Watcher() { }

    public Watcher(long id,
                   Department department, long deptId, String name,
                   Course course, long courseId, String catalogNumber)
    {
        this.id = id;
        this.department = department;
        this.deptId = deptId;
        this.name = name;
        this.course = course;
        this.courseId = courseId;
        this.catalogNumber = catalogNumber;
        this.department = departments.get((int)deptId-1);
    }

    public ArrayList<Watcher> getObservers() {
        return observers;
    }

    public void addObserver(Watcher observer) {

        String observerName = observer.getName();
        String catalogNumber = observer.getCatalogNumber();

        if (observers.size() > 0)
        {
            for (Watcher currentObserver : observers)
            {
                if (!(currentObserver.getName().equals(observerName) &&
                        currentObserver.getCatalogNumber().equals(catalogNumber)))
                {
                    observers.add(observer);
                }
            }
        }

        else
        {
            observers.add(observer);
        }
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDepartments(ArrayList<Department> departments) {
        this.departments = departments;
    }

    public void setDepartment(long deptId) {
        this.department = departments.get((int)deptId-1);
    }

    public void setDeptId(long deptId) {
        this.deptId = deptId;
    }

    public void setName(long deptId) {
        this.name = departments.get((int)deptId-1).getName();
    }

    public void setCourse(long courseId) {
        this.course = this.departments.get((int)deptId-1).getSpecificCourse((int)courseId - 1);
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public void setCatalogNumber(long deptId, long courseId) {
        this.catalogNumber = this.departments.get((int)deptId-1).getSpecificCourse((int)courseId-1).getCatalogNumber();
    }

    public long getId() {
        return id;
    }

    public Department getDepartment() {
        return department;
    }

    public long getDeptId() {
        return deptId;
    }

    public String getName() {
        return name;
    }

    public Course getCourse() {
        return course;
    }

    public long getCourseId() {
        return courseId;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    private void stateChanged(ArrayList<Offering> list, Watcher observer, long deptId, long courseId) {
        if (observer.getDeptId() == deptId &&
                observer.getCourseId() == courseId)
        {
            // update the screen
            Date dNow = new Date();
            SimpleDateFormat ft = new SimpleDateFormat ("E MM.dd hh:mm:ss zzz yyyy");
            String currentTime = ft.format(dNow);

            Offering o = list.get(list.size()-1);

            String newEvent = ": Added section " + o.getComponentCode() + " with enrollment (" +
                    o.getEnrollmentTotal() + " / " + o.getEnrollmentCap()
                    + ") to offering " + o.getTerm() + " " + o.getYear();

            String updateMessage = currentTime + newEvent;

            events.add(updateMessage);
        }
    }

    public ArrayList<String> getEvents() {
        return events;
    }

    public void insert(Offering offering, long deptId, long courseId) {
        list.add(offering);
        notifyObservers(list, deptId, courseId);
    }

    @Override
    public Iterator<Offering> iterator() { return Collections.unmodifiableList(list).iterator(); }

    // (Should put this list at top with other fields!)

    private void notifyObservers(ArrayList<Offering> list, long deptId, long courseId) {
        for (Watcher observer : observers) {
            observer.stateChanged(list, observer, deptId, courseId);
        }
    }

    public int getNumberOfObservers() {
        return observers.size();
    }

    private ArrayList<Offering> getOfferings() {
        // this will always be non-zero when called in stateChanged()
        return list;
    }

    public ArrayList<String> getSpecificWatcherEvents(long id) {
        return observers.get((int)id - 1).getEvents();
    }

    public void removeWatcher(long id) {
        if (observers.size() != 0) {
            observers.remove((int)id - 1);
        }
    }
}

