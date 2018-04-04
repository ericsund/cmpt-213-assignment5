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
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class DataInputController {
    private DisplayOrganizedData display = new DisplayOrganizedData();
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

        display.printDump(allSortedClasses);
    }

    @GetMapping("/api/departments")
    public ArrayList<Department> getDepartments()
    {
        fetchData(); // fetch data if we haven't already
        structureData(); // structure data if we haven't already
        return departments;
    }

    @GetMapping("/api/departments/{id}/courses")
    public ArrayList<Course> getCourses(@PathVariable("id") long id)
    {
        fetchData(); // fetch data if we haven't already
        structureData(); // structure data if we haven't already

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

    // todo write /api/departments{id}/courses{id} endpoint...

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

                if (!(currentDataSet.isEmpty())) {
                    Data currentDepartment = currentDataSet.get(0);

                    // current department different from last: create offerings for new course in new department
                    if (!(currentDepartment.getSubject().equals(getLastDepartmentName())))
                    {
                        Department newDepartment = new Department();
                        newDepartment.setDeptId(nextDepartmentId.incrementAndGet());
                        newDepartment.setName(currentDepartment.getSubject());

                        Course newCourse = new Course();
                        newCourse.setCourseId(nextCourseId.incrementAndGet());
                        newCourse.setCatalogNumber(currentDepartment.getCatalogNumber());

                        for (Data currentOffering : currentDataSet) {
                            Offering newOffering = new Offering();
                            newOffering.setCourseOfferingId(nextCourseOfferingId.incrementAndGet());
                            newOffering.setLocation(currentOffering.getLocation());
                            newOffering.setInstructors(currentOffering.getInstructors().toString());
//                                newOffering.setYear(currentOffering.getYear()); // need to write getYear()
                            newOffering.setSemesterCode(currentOffering.getSemester());
//                                newOffering.setTerm(currentOffering.getTerm()); // need to write getTerm()

                            newCourse.setOfferings(newOffering);
                        }

                        newDepartment.setCourses(newCourse);
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

                            Course newCourse = new Course();
                            newCourse.setCourseId(nextCourseId.incrementAndGet());
                            newCourse.setCatalogNumber(currentDepartment.getCatalogNumber());

                            for (Data currentOffering : currentDataSet) {
                                Offering newOffering = new Offering();
                                newOffering.setCourseOfferingId(nextCourseOfferingId.incrementAndGet());
                                newOffering.setLocation(currentOffering.getLocation());
                                newOffering.setInstructors(currentOffering.getInstructors().toString());
//                                newOffering.setYear(currentOffering.getYear()); // need to write getYear()
                                newOffering.setSemesterCode(currentOffering.getSemester());
//                                newOffering.setTerm(currentOffering.getTerm()); // need to write getTerm()


                                newCourse.setOfferings(newOffering);
                            }

                            tempDepartment.setCourses(newCourse);
                            departments.add(tempDepartment);
                        }

                        // component codes same: create offerings for current course in current dept
                        else
                        {
                            Department tempDepartment = departments.get(departments.size() - 1);
                            departments.remove(departments.size() - 1);
                            Course tempCourse = tempDepartment.getLastCourse();
                            tempDepartment.removeLastCourse();

                            for (Data currentOffering : currentDataSet)
                            {
                                Offering newOffering = new Offering();
                                newOffering.setCourseOfferingId(nextCourseOfferingId.incrementAndGet());
                                newOffering.setLocation(currentOffering.getLocation());
                                newOffering.setInstructors(currentOffering.getInstructors().toString());
//                                newOffering.setYear(currentOffering.getYear()); // need to write getYear()
                                newOffering.setSemesterCode(currentOffering.getSemester());
//                                newOffering.setTerm(currentOffering.getTerm()); // need to write getTerm()

                                tempCourse.setOfferings(newOffering);
                            }

                            tempDepartment.setCourses(tempCourse);
                            departments.add(tempDepartment);
                        }
                    }
                }
            }
        }
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
        classData.setSubject(subject);
        classData.setCatalogNumber(catalogNumber);
        classData.setLocation(location);
        classData.setEnrollmentCapacity(enrollmentCapacity);
        classData.setEnrollmentTotal(enrollmentTotal);
        classData.setInstructors(instructors);
        classData.setComponentCode(componentCode);

        boolean isComponentCodeAClass = (!classData.getComponentCode().equals("TUT")
                || !classData.getComponentCode().equals("LAB")
                || !classData.getComponentCode().equals("OPL")
                || !classData.getComponentCode().equals("WKS"));

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

//    private void buildArrayList(ArrayList<Data>[] organizeByClass)
//    {
//        for (int i = 0; i < numLists; i++)
//        {
//            organizeByClass[i] = new ArrayList<>();
//        }
//    }

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
