package ca.as4.controllers;

import ca.as4.models.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.*;

@RestController
public class DataInputController {

    private DataInputHelper b = new DataInputHelper();

    // enabling Spring servlet
    // copied from: https://stackoverflow.com/questions/36596069/spring-boot-mvc-whitelabel-error-page#36609620
    @Configuration
    @EnableWebMvc
    public class ApplicationWebMvcConfig extends WebMvcConfigurerAdapter {

        @Override
        public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
            b.fetchData();
            b.structureData();
            b.checkReSort();

            configurer.enable();
        }

        @Bean
        public InternalResourceViewResolver viewResolver() {
            InternalResourceViewResolver resolver = new InternalResourceViewResolver();
            resolver.setPrefix("/WEB-INF/views/jsp/");
            resolver.setSuffix(".jsp");
            return resolver;
        }
    }

    @GetMapping("/api/watchers")
    public ArrayList<Watcher> getWatchers()
    {
        b.checkReSort();

        return b.list.getWatchers();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/watchers")
    public void createWatcher(@RequestBody Watcher newWatcher)
    {

        b.checkReSort();

        // watcher model should always have an updated copy of departments
        newWatcher.setDepartments(b.departments);

        newWatcher.setId(b.nextWatcherId.incrementAndGet());

        newWatcher.setDepartment(newWatcher.getDeptId());
        newWatcher.setDeptId(newWatcher.getDepartment().getDeptId());
        newWatcher.setName(newWatcher.getDeptId());

        newWatcher.setCourse(newWatcher.getCourseId());
        newWatcher.setCourseId(newWatcher.getCourse().getCourseId());
        newWatcher.setCatalogNumber(newWatcher.getDeptId(), newWatcher.getCourseId());

        b.list.addWatcher(newWatcher);
    }

    @GetMapping("/api/watchers/{id}")
    public ArrayList<String> getSpecificWatcherEvents(@PathVariable("id") long id)
    {
        b.checkReSort();

        if (b.list.getWatchers().size() == 0) {
            throw new BadRequest("Trying to get from an empty set of watchers!");
        }

        if (id - 1 > b.list.getWatchers().size() || id - 1 < 0) {
            throw new BadRequest("The ID " + id + " is out of range.");
        }

        return b.list.getSpecificWatcherEvents(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/api/watchers/{id}", method = RequestMethod.DELETE)
    public void deleteSpecificWatcher(@PathVariable("id") long id)
    {
        b.checkReSort();

        if (b.list.getWatchers().size() == 0) {
            throw new BadRequest("Trying to remove from an empty set of watchers!");
        }

        if (id - 1 > b.list.getWatchers().size() || id - 1 < 0) {
            throw new BadRequest("The ID " + id + " is out of range.");
        }

        b.list.removeWatcher(id);
    }

    @GetMapping("/api/about")
    public AboutResponse getAbout()
    {
        AboutResponse response = new AboutResponse();
        response.setAppName("a snazzy SFU course planner");
        response.setAuthorName("Eric Sund and Sukhdeep Parmar");
        return response;
    }

    @GetMapping("/api/dump-model")
    public void dumpModel()
    {
        b.checkReSort();

        b.display.printDump(b.allSortedClasses);
//        display.displayClassData(allSortedClasses);
    }

    @GetMapping("/api/departments")
    public ArrayList<Department> getDepartments()
    {
        b.checkReSort();
        return b.departments;
    }

    @GetMapping("/api/departments/{id}/courses")
    public ArrayList<Course> getCourses(@PathVariable("id") long id)
    {
        b.checkReSort();

        if (b.departments.size() == 0) {
            throw new BadRequest("Trying to get from an empty set of departments!");
        }

        if (id - 1 > b.departments.size() || id - 1 < 0) {
            throw new BadRequest("The ID " + id + " is out of range.");
        }

        // search for department and return its courses
        for (Department department : b.departments)
        {
            if (department.getDeptId() == id)
            {
                if (department.getCourses().size() == 0)
                {
                    throw new NotFound("The for id " + id + " has no courses.");
                }
                return department.getCourses();
            }
        }

        // searched all departments but didn't find the id
        throw new NotFound("The id " + id + " was not found.");

    }

    @GetMapping("/api/departments/{deptID}/courses/{courseID}/offerings")
    public ArrayList<Offering> getOfferings(@PathVariable("deptID") long deptID, @PathVariable("courseID") long courseID)
    {
        ArrayList<Course> courses = getCourses(deptID);
        long IDOffset = 0;

        if (courses.size() > 0)
        {
            IDOffset = courses.get(0).getCourseId();
        }

        ArrayList<Offering> offerings = b.getOfferingsHelper(courses, courseID, IDOffset);

        if (offerings != null)
        {
            return offerings;
        }
        else
        {
            throw new DataInputController.NotFound("Course for " + courseID + " is out of range or has no offerings.");
        }
    }

    @GetMapping("/api/departments/{deptID}/courses/{courseID}/offerings/{sectionID}")
    public ArrayList<Section> getSections(@PathVariable("deptID") long deptID,
                                          @PathVariable("courseID") long courseID,
                                          @PathVariable("sectionID") long sectionID)
    {
        ArrayList<Offering> offerings = getOfferings(deptID, courseID);
        ArrayList<Section> sections = b.getSection(offerings, sectionID);

        if (sections != null)
        {
            return sections;
        }

        else
        {
            throw new DataInputController.NotFound("Offerings for " + sectionID + " is empty or " + courseID +
                    " is empty.");
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/addoffering")
    public void addOffering(@RequestBody Data newData)
    {
        b.checkReSort();

        boolean existingDepartment = false;
        boolean existingCourse = false;

        b.display.perClassCalculations(newData, newData.getEnrollments(),
                newData.getComponents());

        b.display.displayFormatter(newData.getEnrollments(), newData.getComponents(),
                newData, newData.getAllOfferings());

        int foundDept = 0;
        int foundCourse = 0;

        /*
        There are three cases when creating an offering:

        1.  New offering; new course; new dept
        2.  New offering; old course; old dept
        3.  New offering; new course; old dept;
        */

        // Case 1: find the department
        for (int i = 0; i < b.departments.size(); i++)
        {
            Department department = b.departments.get(i);
            if (department.getName().equals(newData.getSubjectName()))
            {
                foundDept = i;
                existingDepartment = true;
            }

        }

        // Set case 1: create a new offering for a new course in a new department
        if (!(existingDepartment))
        {
            b.newOfferingNewCourseNewDept(newData);
            b.needToReSort = true;
        }

        // Case 2: find the course in the existing department
        if (existingDepartment)
        {
            ArrayList<Course> courses = b.departments.get(foundDept).getCourses();
            for (int i = 0; i < courses.size(); i++)
            {
                Course course = courses.get(i);
                if (course.getCatalogNumber().equals(newData.getCatalogNumber()))
                {
                    foundCourse = i;
                    existingCourse = true;
                }
            }
        }

        // Set case 2: create new offering for an existing course in existing department
        if (existingDepartment && existingCourse)
        {
            b.newOfferingOldCourseOldDept(foundDept, foundCourse, newData);
            b.needToReSort = true;
        }

        // Otherwise set case 3: create new offering for a new course in existing department
        if (existingDepartment && !(existingCourse))
        {
            b.newOfferingNewCourseOldDept(foundDept, newData);
            b.needToReSort = true;
        }
    }

    @GetMapping("/api/stats/students-per-semester")
    @ResponseStatus(HttpStatus.OK)
    public ArrayList<GraphData> graphData(@RequestParam(value = "deptId", required = true) long deptID)
    {
        b.fetchData(); // fetch data if we haven't already
        b.structureData(); // structure data if we haven't already
        b.checkReSort();

        if (!(deptID <= b.departments.size() && deptID > 0))
        {
            throw new NotFound("Offerings for " + deptID + " is out of range.");
        }

        Department currentDepartment = b.departments.get((int)deptID-1);
        TreeMap<Integer, GraphData> currentMap = currentDepartment.getGraphTreeMap();
        ArrayList<Integer> semesters = currentDepartment.getSemesters();
        Collections.sort(semesters);

        GraphData[] graph = new GraphData[currentMap.size()];
        ArrayList<GraphData> graphData = new ArrayList<>();

        for (int i = 0; i < semesters.size(); i++)
        {
            graph[i] = currentMap.get(semesters.get(i));
            graphData.add(graph[i]);
        }

        return graphData;
    }

    // Exceptions
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private class BadRequest extends RuntimeException
    {
        private BadRequest(String message)
        {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private class NotFound extends RuntimeException {
        private NotFound(String message) {
            super(message);
        }
    }
}
