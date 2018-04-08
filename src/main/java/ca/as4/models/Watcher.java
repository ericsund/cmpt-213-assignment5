package ca.as4.models;

import java.text.SimpleDateFormat;
import java.util.*;

public class Watcher implements Iterable<Offering>
{
    private ArrayList<Department> departments = new ArrayList<>();

    private ArrayList<String> events = new ArrayList<>();

    private ArrayList<Watcher> watchers = new ArrayList<>();
    private ArrayList<Offering> offerings = new ArrayList<>();

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

    @Override
    public Iterator<Offering> iterator()
    {
        return Collections.unmodifiableList(offerings).iterator();
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setDepartments(ArrayList<Department> departments)
    {
        this.departments = departments;
    }

    public void setDepartment(long deptId)
    {
        this.department = departments.get((int)deptId-1);
    }

    public void setDeptId(long deptId)
    {
        this.deptId = deptId;
    }

    public void setName(long deptId)
    {
        this.name = departments.get((int)deptId-1).getName();
    }

    public void setCourse(long courseId)
    {
        this.course = this.departments.get((int)deptId-1).getSpecificCourse((int)courseId - 1);
    }

    public void setCourseId(long courseId)
    {
        this.courseId = courseId;
    }

    public void setCatalogNumber(long deptId, long courseId)
    {
        this.catalogNumber = this.departments.get((int)deptId-1).getSpecificCourse((int)courseId-1).getCatalogNumber();
    }

    public Department getDepartment()
    {
        return department;
    }

    public ArrayList<Watcher> getWatchers()
    {
        return watchers;
    }

    public long getDeptId()
    {
        return deptId;
    }

    public String getName()
    {
        return name;
    }

    public Course getCourse()
    {
        return course;
    }

    public long getCourseId()
    {
        return courseId;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public ArrayList<String> getEvents() {
        return events;
    }

    public ArrayList<String> getSpecificWatcherEvents(long id)
    {
        return watchers.get((int)id - 1).getEvents();
    }

    public void addOffering(Offering offering, long deptId, long courseId)
    {
        offerings.add(offering);
        notifyObservers(offerings, deptId, courseId);
    }

    public void addWatcher(Watcher observer)
    {
        String observerName = observer.getName();
        String catalogNumber = observer.getCatalogNumber();

        if (watchers.size() > 0)
        {
            for (Watcher currentObserver : watchers)
            {
                if (!(currentObserver.getName().equals(observerName) &&
                        currentObserver.getCatalogNumber().equals(catalogNumber)))
                {
                    watchers.add(observer);
                }
            }
        }

        else
        {
            watchers.add(observer);
        }
    }

    public void removeWatcher(long id)
    {
        if (watchers.size() != 0) {
            watchers.remove((int)id - 1);
        }
    }

    private void notifyObservers(ArrayList<Offering> list, long deptId, long courseId)
    {
        for (Watcher observer : watchers)
        {
            observer.stateChanged(list, observer, deptId, courseId);
        }
    }

    private void stateChanged(ArrayList<Offering> list, Watcher observer, long deptId, long courseId)
    {
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
}

