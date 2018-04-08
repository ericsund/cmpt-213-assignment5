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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class DataInputController {

    // -----------------------------------------------------------------------------------------------------------------
    // todo move to watcher controller
    private AtomicLong nextWatcherId = new AtomicLong();
    private Watcher list = new Watcher();
    // -----------------------------------------------------------------------------------------------------------------

    private boolean needToReSort = true;

    private DisplayOrganizedData display = new DisplayOrganizedData();
    private Department reSort = new Department();
    private SortController sorter = new SortController();
    private ArrayList<String[]> csvData = new ArrayList<>();
    private ArrayList<Data> allData = new ArrayList<>();

    private ArrayList<ArrayList<Data>> allSortedClasses = new ArrayList<>();
    private ArrayList<Department> departments = new ArrayList<>();

    private int numLists = 0;
    private AtomicLong nextDepartmentId = new AtomicLong();
    private AtomicLong nextCourseId = new AtomicLong();
    private AtomicLong nextCourseOfferingId = new AtomicLong();

    private String[] topCSVRow = {"SEMESTER", "SUBJECT", "CATALOGNUMBER",
                                  "LOCATION", "ENROLMENTCAPACITY", "ENROLMENTTOTAL",
                                  "INSTRUCTORS", "COMPONENTCODE"};

    // enabling Spring servlet
    // copied from: https://stackoverflow.com/questions/36596069/spring-boot-mvc-whitelabel-error-page#36609620
    @Configuration
    @EnableWebMvc
    public class ApplicationWebMvcConfig extends WebMvcConfigurerAdapter {

        @Override
        public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
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

    // -----------------------------------------------------------------------------------------------------------------
    // todo move to watcher controller

    @GetMapping("/api/watchers")
    public ArrayList<Watcher> getWatchers()
    {
        fetchData();
        structureData();
        checkReSort();

//        debug
//        ArrayList<Watcher> watchers = list.getObservers();
//        Watcher w = watchers.get(watchers.size()-1);
//        System.out.println("We have " + watchers.size() + " observers!");
//        System.out.println("Watcher data for its dept: " + w.getDepartment().getName());
//        System.out.println("Watcher data for its cat number: " + w.getCourse().getCatalogNumber());
//
//        if (w instanceof Watcher) {
//            System.out.println("We have a watcher!");
//        }

        return list.getObservers();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/watchers")
    public void createWatcher(@RequestBody Watcher newWatcher)
    {
        fetchData();
        structureData();
        checkReSort();

        // model should always have an updated copy of departments
        newWatcher.setDepartments(departments);

        newWatcher.setId(nextWatcherId.incrementAndGet());

        newWatcher.setDepartment(newWatcher.getDeptId());
        // department id implicitly set via curl
        newWatcher.setDeptId(newWatcher.getDepartment().getDeptId());
        newWatcher.setName(newWatcher.getDeptId());

        newWatcher.setCourse(newWatcher.getCourseId());
        // course id implicitly set via curl
        newWatcher.setCourseId(newWatcher.getCourse().getCourseId());
        newWatcher.setCatalogNumber(newWatcher.getDeptId(), newWatcher.getCourseId());

        list.addObserver(newWatcher);
    }

    @GetMapping("/api/watchers/{id}")
    public ArrayList<String> getSpecificWatcherEvents(@PathVariable("id") long id)
    {
        return list.getSpecificWatcherEvents(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/api/watchers/{id}", method = RequestMethod.DELETE)
    public void deleteSpecificWatcher(@PathVariable("id") long id)
    {
        list.removeWatcher(id);
    }

    // -----------------------------------------------------------------------------------------------------------------

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
        fetchData(); // fetch data if we haven't already
        checkReSort();

        display.printDump(allSortedClasses);
//        display.displayClassData(allSortedClasses);
    }

    @GetMapping("/api/departments")
    public ArrayList<Department> getDepartments()
    {
        fetchData(); // fetch data if we haven't already
        structureData(); // structure data if we haven't already
        checkReSort();
        return departments;
    }

    @GetMapping("/api/departments/{id}/courses")
    public ArrayList<Course> getCourses(@PathVariable("id") long id)
    {
        fetchData(); // fetch data if we haven't already
        structureData(); // structure data if we haven't already
        checkReSort();

        // quit if id is out of range
        if (!(id <= departments.size() && id > 0))
        {
            throw new NotFound("Department for id " + id + " is out of range.");
        }

        // search for department and return its courses
        for (Department department : departments)
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

        return grabOfferings(courses, courseID, IDOffset);
    }

    @GetMapping("/api/departments/{deptID}/courses/{courseID}/offerings/{sectionID}")
    public ArrayList<Section> getSections(@PathVariable("deptID") long deptID,
                                          @PathVariable("courseID") long courseID,
                                          @PathVariable("sectionID") long sectionID)
    {
        ArrayList<Offering> offerings = getOfferings(deptID, courseID);
        return grabSection(offerings, sectionID);
    }

    private ArrayList<Section> grabSection(ArrayList<Offering> offerings, long id)
    {
        if (!(id <= offerings.size() && id > 0))
        {
            throw new NotFound("Offerings for " + id + " is out of range.");
        }

        ArrayList<Section> sections = new ArrayList<>();
        for (Offering offering : offerings)
        {
            if (offering.getCourseOfferingId() == id)
            {
                sections = offering.getSections();
                if (sections.size() == 0)
                {
                    throw new NotFound("The id: " + id + " has no sections.");
                }
                else
                {
                    break;
                }
            }
        }
        return sections;
    }

    // todo refactor this to work with hash tables?

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/addoffering")
    public void addoffering(@RequestBody Data newData)
    {
        fetchData(); // fetch data if we haven't already
        structureData(); // structure data if we haven't already
        checkReSort();

        boolean existingDepartment = false;
        boolean existingCourse = false;

        display.perClassCalculations(newData, newData.getEnrollments(),
                newData.getComponents());

        display.displayFormatter(newData.getEnrollments(), newData.getComponents(),
                newData, newData.getAllOfferings());

        int foundDept = 0;
        int foundCourse = 0;

        // find the department
        for (int i = 0; i < departments.size(); i++)
        {
            Department department = departments.get(i);
            if (department.getName().equals(newData.getSubjectName()))
            {
                foundDept = i;
                existingDepartment = true;
            }

        }

        // create a new offering for a new course in a new department
        if (!(existingDepartment))
        {
            Department newDept = new Department();
            newDept.setDeptId(nextDepartmentId.incrementAndGet());
            newDept.setName(newData.getSubjectName());

            Course newCourse = new Course();
            newCourse.setCourseId(nextCourseId.incrementAndGet());
            newCourse.setCatalogNumber(newData.getCatalogNumber());

            Offering newOffering = buildOffering(newData);

            newCourse.addOffering(newOffering); // add new offering to new course
            list.insert(newOffering, newDept.getDeptId(), newCourse.getCourseId()); // add to watchers list

            newDept.addCourse(newCourse); // add new course to new department
            Collections.sort(newDept.getCourses()); // resort courses with new addition
            departments.add(newDept); // add new department to master list
            needToReSort = true;
        }

        // find the course in the existing department
        if (existingDepartment)
        {
            ArrayList<Course> courses = departments.get(foundDept).getCourses();
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

        // create new offering for an existing course in existing department
        if (existingDepartment && existingCourse)
        {
            Department tempDept = departments.get(foundDept);
            departments.remove(foundDept);

            Course tempCourse = tempDept.getSpecificCourse(foundCourse);
            tempDept.removeSpecificCourse(foundCourse);

            Offering newOffering = buildOffering(newData);
            list.insert(newOffering, tempDept.getDeptId(), tempCourse.getCourseId()); // add to watchers list

            tempCourse.addOffering(newOffering); // add new offering to existing course
            Collections.sort(tempCourse.getOfferings()); // resort offerings with new addition
            tempDept.addCourse(foundCourse, tempCourse); // put existing course back where we found it
            departments.add(tempDept);
            needToReSort = true;
        }

        // create new offering for a new course in existing department
        if (existingDepartment && !(existingCourse))
        {
            Department tempDept = departments.get(foundDept);
            departments.remove(foundDept);

            Course newCourse = new Course();

            newCourse.setCatalogNumber(newData.getCatalogNumber());
            Offering newOffering = buildOffering(newData);
            list.insert(newOffering, tempDept.getDeptId(), newCourse.getCourseId()); // add to watchers list

            newCourse.addOffering(newOffering); // add new offering to new course
            tempDept.addCourse(newCourse); // add new course to existing department
            Collections.sort(tempDept.getCourses()); // resort courses with new addition
            departments.add(tempDept);
            needToReSort = true;
        }
    }

    @GetMapping("/api/stats/students-per-semester")
    @ResponseStatus(HttpStatus.OK)
    public ArrayList<GraphData> graphData(@RequestParam(value = "deptId", required = true) long deptID)
    {
        fetchData(); // fetch data if we haven't already
        structureData(); // structure data if we haven't already
        checkReSort();

        if (!(deptID <= departments.size() && deptID > 0))
        {
            throw new NotFound("Offerings for " + deptID + " is out of range.");
        }

        Department currentDepartment = departments.get((int)deptID-1);
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

    private ArrayList<Offering> grabOfferings(ArrayList<Course> courses, long id, long IDOffset)
    {
        if (!(id < courses.size() + IDOffset && id >= IDOffset))
        {
            throw new NotFound("Course for " + id + " is out of range.");
        }

        ArrayList<Offering> offerings = new ArrayList<>();

        // search for department and return its courses
        for (Course course : courses)
        {
            if (course.getCourseId() == id)
            {
                if (course.getOfferings().size() == 0)
                {
                    throw new NotFound("The course: " + id + " has no offerings.");
                }
                else
                {
                    offerings = course.getOfferings();
                    break;
                }
            }
        }

        return offerings;
    }

    private void checkReSort() {
        if (needToReSort)
        {
            Collections.sort(departments);
            reSort.recalculateAllIDs(departments);
            needToReSort = false;
        }
    }

    private String getLastDepartmentName()
    {
        String lastDepartmentName;

        if (departments.size() > 0)
        {
            int lastDepartmentIndex = departments.size() - 1;
            lastDepartmentName = departments.get(lastDepartmentIndex).getName();
        }
        else
        {
            lastDepartmentName = "";
        }

        return lastDepartmentName;
    }

    // retrieve and sort csv data if we haven't already
    private void fetchData()
    {
        if (allSortedClasses.isEmpty())
        {
            retrieveCSVData();
            populateDataModel();

            ArrayList<ArrayList<Data>> organizeClasses = new ArrayList<>();
            allSortedClasses = sorter.sortDataByClassName(organizeClasses, allData);
        }
    }

    // structure organized data if we haven't already
    private void structureData()
    {
        if (departments.isEmpty()) {
            // todo a faster way to do this is simultaneously with sortDataByClassName, but it's easier to see this way
            for (int i = 0; i < allSortedClasses.size(); i++)
            {
                ArrayList<Data> currentDataSet = allSortedClasses.get(i);

                Collections.sort(currentDataSet, new Comparator<Data>() {
                    @Override
                    public int compare(Data o1, Data o2) {
                        String t1 = o1.getSemester() + o1.getSubjectName() + o1.getCatalogNumber() +
                                    o1.getLocation() + o1.getComponent();

                        String t2 = o2.getSemester() + o2.getSubjectName() + o2.getCatalogNumber() +
                                o1.getLocation() + o1.getComponent();

                        return t1.compareTo(t2);
                    }
                });


                if (!(currentDataSet.isEmpty())) {
                    Data currentDepartment = currentDataSet.get(0);

                    String comparisonStr = currentDataSet.get(0).getSemester() + currentDataSet.get(0).getLocation();
                    ArrayList<Data> group = new ArrayList<>();

                    // current department different from last: create offerings for new course in new department
                    if (!(currentDepartment.getSubjectName().equals(getLastDepartmentName())))
                    {
                        Department newDepartment = new Department();
                        newDepartment.setDeptId(nextDepartmentId.incrementAndGet());
                        newDepartment.setName(currentDepartment.getSubjectName());
                        TreeMap<Integer, GraphData> currentTable = newDepartment.getGraphTreeMap();
                        ArrayList<Integer> semesters = newDepartment.getSemesters();

                        Course newCourse = new Course();
                        newCourse.setCourseId(nextCourseId.incrementAndGet());

                        newCourse.setCatalogNumber(currentDepartment.getCatalogNumber());
                        buildGroupedClasses(currentDataSet, comparisonStr, group, newCourse,
                                currentTable, semesters);

                        newDepartment.setGraphTreeMap(currentTable);
                        newDepartment.setSemesters(semesters);

                        newDepartment.addCourse(newCourse);
                        departments.add(newDepartment);
                    }

                    // current dept same from last: create offerings for current course OR new course in current dept
                    else
                    {
                        String currentCatalogNumber = allSortedClasses.get(i).get(0).getCatalogNumber();
                        String previousCatalogNumber = allSortedClasses.get(i - 1).get(0).getCatalogNumber();

                        // catalog number different: create offerings for new course in current department
                        if (!(currentCatalogNumber.equals(previousCatalogNumber)))
                        {
                            Department tempDepartment = departments.get(departments.size() - 1);
                            departments.remove(departments.size() - 1);
                            TreeMap<Integer, GraphData> currentTable = tempDepartment.getGraphTreeMap();
                            ArrayList<Integer> semesters = tempDepartment.getSemesters();

                            Course newCourse = new Course();
                            newCourse.setCourseId(nextCourseId.incrementAndGet());
                            newCourse.setCatalogNumber(currentDepartment.getCatalogNumber());

                            nextCourseOfferingId.getAndSet(0);
                            buildGroupedClasses(currentDataSet, comparisonStr, group, newCourse,
                                    currentTable, semesters);

                            tempDepartment.setGraphTreeMap(currentTable);
                            tempDepartment.setSemesters(semesters);

                            tempDepartment.addCourse(newCourse);
                            departments.add(tempDepartment);
                        }

                        // component codes same: create offerings for current course in current dept
                        else
                        {
                            Department tempDepartment = departments.get(departments.size() - 1);
                            departments.remove(departments.size() - 1);
                            Course tempCourse = tempDepartment.getLastCourse();
                            tempDepartment.removeLastCourse();
                            TreeMap<Integer, GraphData> currentTable = tempDepartment.getGraphTreeMap();
                            ArrayList<Integer> semesters = tempDepartment.getSemesters();

                            nextCourseOfferingId.getAndSet(0);
                            buildGroupedClasses(currentDataSet, comparisonStr, group, tempCourse,
                                    currentTable, semesters);

                            tempDepartment.setGraphTreeMap(currentTable);
                            tempDepartment.setSemesters(semesters);

                            tempDepartment.addCourse(tempCourse);
                            departments.add(tempDepartment);
                        }
                    }
                }
            }
        }
    }

    private void buildGroupedClasses(ArrayList<Data> currentDataSet, String comparisonStr, ArrayList<Data> group,
                                     Course newCourse, TreeMap<Integer, GraphData> currentTable, ArrayList<Integer> semesters) {
        if (currentDataSet.size() == 1)
        {
            Data data = currentDataSet.get(0);

            display.perClassCalculations(data, data.getEnrollments(),
                    data.getComponents());

            display.displayFormatter(data.getEnrollments(), data.getComponents(),
                    data, data.getAllOfferings());

            Offering newOffering = buildOffering(data);
            newCourse.addOffering(newOffering);

            updateTreeTable(currentTable, data, semesters);
            return;
        }

        for (Data currentOffering : currentDataSet)
        {
            display.perClassCalculations(currentOffering, currentOffering.getEnrollments(),
                    currentOffering.getComponents());

            String currentStr = currentOffering.getSemester() + currentOffering.getLocation();
            comparisonStr = getStringAndBuildNewOffering(comparisonStr, group, newCourse, currentOffering, currentStr);
            group.add(currentOffering);

            updateTreeTable(currentTable, currentOffering, semesters);
        }
    }

    private void updateTreeTable(TreeMap<Integer, GraphData> currentTable, Data currentOffering, ArrayList<Integer> semesters) {
        if (currentOffering.getComponent().equals("LEC"))
        {
            GraphData tempGraph = new GraphData(currentOffering.getSemester(), currentOffering.getEnrollmentTotal());

            if (!semesters.contains(currentOffering.getSemester()))
            {
                semesters.add(currentOffering.getSemester());
            }

            if (currentTable.containsKey(currentOffering.getSemester()))
            {
                currentTable.get(currentOffering.getSemester()).incrementEnrollmentTotal(currentOffering.getEnrollmentTotal());
            }
            else
            {
                currentTable.put(currentOffering.getSemester(), tempGraph);
            }
        }
    }

    private String getStringAndBuildNewOffering(String comparisonStr, ArrayList<Data> group, Course newCourse, Data currentOffering, String currentStr) {
        if (!currentStr.equals(comparisonStr))
        {
            Offering newOffering = buildOffering(group);
            newCourse.addOffering(newOffering);

            comparisonStr = currentOffering.getSemester() + currentOffering.getLocation();
            group.clear();
        }
        return comparisonStr;
    }

    private Offering buildOffering(Data newData)
    {
        int[] enrollments = newData.getEnrollments();
        boolean[] components = newData.getComponents();

        Offering newOffering = new Offering();
        newOffering.setComponentCode(newData.getComponent());

        newOffering.setCourseOfferingId(nextCourseOfferingId.incrementAndGet());
        newOffering.setLocation(newData.getLocation());
        newOffering.setEnrollmentCap(newData.getEnrollmentCap());
        newOffering.setEnrollmentTotal(newData.getEnrollmentTotal());

        if (newData.getInstructors().size() > 0)
        {
            newOffering.setInstructors(newData.getInstructors().toString()
                    .replace("[", "")
                    .replace("]", "")
                    .replace("  ", " ")
                    .trim());
        }
        else
        {
            newOffering.setInstructors(newData.getInstructor()
                    .replace("[", "")
                    .replace("]", "")
                    .replace("  ", " ")
                    .trim());
        }

        newOffering.setSemesterCode(newData.getSemester());
        newOffering.setEnrollments(enrollments);
        newOffering.setComponents(components);

        setYearAndTermOffering(newData.getSemester(), newOffering);
        setSection(newData, newOffering);

        return newOffering;
    }

    private Offering buildOffering(ArrayList<Data> group)
    {
        int[] enrollments = group.get(0).getEnrollments();
        boolean[] components = group.get(0).getComponents();

        if (group.size() > 1)
        {
            int increment = 15;
            for (int i = 1; i < group.size(); i++)
            {
                for (int j = 0; j < 15; j++)
                {
                    enrollments[j] += group.get(i).getEnrollments()[j];
                    enrollments[j+increment] += group.get(i).getEnrollments()[j+increment];

                    if (components[j] || group.get(i).getComponents()[j])
                    {
                        components[j] = true;
                    }
                }
            }
        }

        Data temp = buildTempDataFile(group, enrollments, components);
        Offering newOffering = new Offering();
        newOffering.setComponentCode(temp.getComponent());
        newOffering.setEnrollmentCap(temp.getEnrollmentCap());
        newOffering.setEnrollmentTotal(temp.getEnrollmentTotal());

        newOffering.setCourseOfferingId(nextCourseOfferingId.incrementAndGet());
        newOffering.setLocation(group.get(group.size()-1).getLocation());
        newOffering.setInstructors(group.get(group.size()-1).getInstructors().toString()
                .replace("[", "")
                .replace("]", "")
                .replace("  ", " ")
                .trim());

        newOffering.setSemesterCode(group.get(0).getSemester());
        newOffering.setEnrollments(enrollments);
        newOffering.setComponents(components);

        setYearAndTermOffering(group.get(0).getSemester(), newOffering);
        setSection(temp, newOffering);

        return newOffering;
    }

    private Data buildTempDataFile(ArrayList<Data> group, int[] enrollments, boolean[] components)
    {
        Data temp = new Data();
        temp.setComponent(group.get(0).getComponent());
        temp.setLocation(group.get(group.size()-1).getLocation());
        temp.setCatalogNumber(group.get(0).getCatalogNumber());
        temp.setSubjectName(group.get(0).getSubjectName());
        temp.setSemester(group.get(0).getSemester());
        temp.setInstructors(group.get(group.size()-1).getInstructors());
        temp.setEnrollments(enrollments);
        temp.setComponents(components);

        display.displayFormatter(temp.getEnrollments(), temp.getComponents(),
                temp, temp.getAllOfferings());

        return temp;
    }

    private void setYearAndTermOffering(int semester, Offering newOffering)
    {
        String semesterCode = semester + "";
        char term = semesterCode.charAt(3);
        int year = Integer.parseInt(semesterCode.substring(1, 3)) + 2000;
        String termToInsert;

        switch (term)
        {
            case '1' :
                termToInsert = "Spring";
                break;

            case '4' :
                termToInsert = "Summer";
                break;

            case '7' :
                termToInsert = "Fall";
                break;

            default :
                termToInsert = "";
        }

        newOffering.setTerm(termToInsert);
        newOffering.setYear(year);
    }

    private void setSection(Data currentOffering, Offering newOffering)
    {
        ArrayList<Section> sections = new ArrayList<>();

        for (long i = 0; i < currentOffering.getAllOfferings().size(); i++)
        {
            String currentStr = currentOffering.getAllOfferings().get((int)i);
            String type = currentStr.substring(7, 10);
            String enrollmentInfo = currentStr.substring(23, currentStr.length());
            String[] numericalData = enrollmentInfo.split("/");

            int enrollmentTotal = Integer.parseInt(numericalData[0]);
            int enrollmentCap = Integer.parseInt(numericalData[1]);

            Section section = new Section(i, type, enrollmentTotal, enrollmentCap);
            sections.add(section);
        }
        newOffering.setSections(sections);
    }

    private void retrieveCSVData()
    {
        String CSVFile = "data/course_data_2018.csv";
        String splitCSVBy = ",";
        String currentLine;

        // open csv file
        File file = new File(CSVFile);

        if (!(file.exists()))
        {
            System.out.println("Error: File does not exist.");
            System.exit(1);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            while ((currentLine = br.readLine()) != null)
            {
                // grab all entries on each line, add to ArrayList
                String[] line = currentLine.split(splitCSVBy);
                csvData.add(line);
            }
        }
        catch (IOException e)
        {
            System.out.println("Error: Unable to open the CSV file.");
            System.exit(2);
        }
        catch (Exception e)
        {
            System.out.println("Error: An unknown exception occurred while processing the CSV file.");
            System.exit(3);
        }
    }

    private void populateDataModel()
    {
        for (String[] line : csvData)
        {
            if (notTopRow(line[0]))
            {
                convertDataAndInsert(line);
            }
        }
    }

    private boolean notTopRow(String lineEntry)
    {
        for (String compareString : topCSVRow)
        {
            if (compareString.equals(lineEntry))
            {
                return false;
            }
        }
        return true;
    }

    private void convertDataAndInsert(String[] line)
    {
        // create new blank data object
        Data classData = new Data();

        // grab the entries for a line
        int semester = Integer.parseInt(line[0]);
        String subject = fixStrings(line[1]);
        String catalogNumber = fixStrings(line[2]);
        String location = fixStrings(line[3]);
        int enrollmentCapacity = Integer.parseInt(line[4]);
        int enrollmentTotal = Integer.parseInt(line[5]);
        ArrayList<String> instructors = new ArrayList<>();
        String componentCode;
        int sizeOfAClass = 8;


        if (line.length >= sizeOfAClass && line[7].length() > 3)
        {
            int currentIndex = 6;
            while (currentIndex < line.length-1)
            {
                if (line[currentIndex].contains("\""))
                {
                    String changedStr;
                    changedStr = line[currentIndex].replace("\"", "");
                    changedStr = fixStrings(changedStr);

                    instructors.add(changedStr);
                }
                else
                {
                    String changedStr;
                    changedStr = fixStrings(line[currentIndex]);
                    instructors.add(changedStr);
                }

                currentIndex++;
            }
            componentCode = line[currentIndex];
        }
        else
        {
            if (line[6].contains("\""))
            {
                String changedStr = line[6].replace("\"", "");
                changedStr = fixStrings(changedStr);
                instructors.add(changedStr);
            }
            else
            {
                String changedStr;
                changedStr = fixStrings(line[6]);
                instructors.add(changedStr);
            }
            componentCode = line[7];
        }

        classData.setSemester(semester);
        classData.setSubjectName(subject);
        classData.setCatalogNumber(catalogNumber);
        classData.setLocation(location);
        classData.setEnrollmentCap(enrollmentCapacity);
        classData.setEnrollmentTotal(enrollmentTotal);
        classData.setInstructors(instructors);
        classData.setComponent(componentCode);

        boolean isComponentCodeAClass = (!classData.getComponent().equals("TUT")
                || !classData.getComponent().equals("LAB")
                || !classData.getComponent().equals("OPL")
                || !classData.getComponent().equals("WKS"));

        if (!allData.contains(classData) && isComponentCodeAClass)
        {
            numLists++;
        }

        allData.add(classData);
    }

    private String fixStrings(String changedStr)
    {
        String stringToFix = changedStr;
        stringToFix = stringToFix.replace("(", "<")
                .replace(")", ">");

        for (int i = 0; i < stringToFix.length()-1; i++)
        {
            if (stringToFix.charAt(i) == ' ' && stringToFix.charAt(i+1) == ' ')
            {
                stringToFix = changedStr.substring(0, i);
            }
        }

        return stringToFix;
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
