package ca.as4.models;

import ca.as4.controllers.SortController;
import ca.as4.models.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

/*
Helper functions for the DataInputController
*/
public class DataInputHelper
{

    //All the variables needed
    public AtomicLong nextWatcherId = new AtomicLong();
    public Watcher list = new Watcher();

    public boolean needToReSort = true;

    public DisplayOrganizedData display = new DisplayOrganizedData();
    private Department reSort = new Department();
    private SortController sorter = new SortController();
    private ArrayList<String[]> csvData = new ArrayList<>();
    private ArrayList<Data> allData = new ArrayList<>();

    public ArrayList<ArrayList<Data>> allSortedClasses = new ArrayList<>();
    public ArrayList<Department> departments = new ArrayList<>();

    private int numLists = 0;
    private AtomicLong nextDepartmentId = new AtomicLong();
    private AtomicLong nextCourseId = new AtomicLong();
    private AtomicLong nextCourseOfferingId = new AtomicLong();

    private String[] topCSVRow = {"SEMESTER", "SUBJECT", "CATALOGNUMBER",
            "LOCATION", "ENROLMENTCAPACITY", "ENROLMENTTOTAL",
            "INSTRUCTORS", "COMPONENTCODE"};


    //Returns an arraylist of offerings per course
    public ArrayList<Offering> getOfferingsHelper(ArrayList<Course> courses, long id, long IDOffset)
    {
        if (!(id < courses.size() + IDOffset && id >= IDOffset))
        {
            return null;
        }

        ArrayList<Offering> offerings = new ArrayList<>();

        // search for department and return its courses
        for (Course course : courses)
        {
            if (course.getCourseId() == id)
            {
                if (course.getOfferings().size() == 0)
                {
                    return null;
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

    //Returns an arraylist of sections per offering of a course
    public ArrayList<Section> getSection(ArrayList<Offering> offerings, long id)
    {
        if (!(id <= offerings.size() && id > 0))
        {
            return null;
        }

        ArrayList<Section> sections = new ArrayList<>();
        for (Offering offering : offerings)
        {
            if (offering.getCourseOfferingId() == id)
            {
                sections = offering.getSections();
                if (sections.size() == 0)
                {
                    return null;
                }
                else
                {
                    break;
                }
            }
        }
        return sections;
    }

    //Function to resort the data if it need be
    public void checkReSort() {
        if (needToReSort)
        {
            Collections.sort(departments);
            reSort.recalculateAllIDs(departments);
            needToReSort = false;
        }
    }

    //Get the name of the last department for data organization purposes
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

    // helper function to create a new offering for a new course in a new department
    public void newOfferingNewCourseNewDept(Data newData)
    {
        Department newDept = new Department();
        newDept.setDeptId(nextDepartmentId.incrementAndGet());
        newDept.setName(newData.getSubjectName());

        Course newCourse = new Course();
        newCourse.setCourseId(nextCourseId.incrementAndGet());
        newCourse.setCatalogNumber(newData.getCatalogNumber());

        Offering newOffering = buildOffering(newData);

        newCourse.addOffering(newOffering); // add new offering to new course
        list.addOffering(newOffering, newDept.getDeptId(), newCourse.getCourseId()); // add to watchers list

        newDept.addCourse(newCourse); // add new course to new department
        Collections.sort(newDept.getCourses()); // resort courses with new addition

        // update the graph
        TreeMap<Integer, GraphData> currentMap = newDept.getGraphTreeMap();
        ArrayList<Integer> semesters = newDept.getSemesters();
        updateTreeTable(currentMap, newData, semesters);
        newDept.setSemesters(semesters);
        newDept.setGraphTreeMap(currentMap);

        departments.add(newDept); // add new department to master list
    }

    // helper function to create new offering for an existing course in existing department
    public void newOfferingOldCourseOldDept(int foundDept, int foundCourse, Data newData)
    {
        Department tempDept = departments.get(foundDept);
        departments.remove(foundDept);

        Course tempCourse = tempDept.getSpecificCourse(foundCourse);
        tempDept.removeSpecificCourse(foundCourse);

        Offering newOffering = buildOffering(newData);
        list.addOffering(newOffering, tempDept.getDeptId(), tempCourse.getCourseId()); // add to watchers list

        tempCourse.addOffering(newOffering); // add new offering to existing course
        Collections.sort(tempCourse.getOfferings()); // resort offerings with new addition
        tempDept.addCourse(foundCourse, tempCourse); // put existing course back where we found it

        // update the graph
        TreeMap<Integer, GraphData> currentMap = tempDept.getGraphTreeMap();
        ArrayList<Integer> semesters = tempDept.getSemesters();
        updateTreeTable(currentMap, newData, semesters);
        tempDept.setSemesters(semesters);
        tempDept.setGraphTreeMap(currentMap);

        departments.add(tempDept);
    }

    // Helper function to create new offering for a new course in existing department
    public void newOfferingNewCourseOldDept(int foundDept, Data newData)
    {
        Department tempDept = departments.get(foundDept);
        departments.remove(foundDept);

        Course newCourse = new Course();

        newCourse.setCatalogNumber(newData.getCatalogNumber());
        Offering newOffering = buildOffering(newData);
        list.addOffering(newOffering, tempDept.getDeptId(), newCourse.getCourseId()); // add to watchers list

        newCourse.addOffering(newOffering); // add new offering to new course
        tempDept.addCourse(newCourse); // add new course to existing department
        Collections.sort(tempDept.getCourses()); // resort courses with new addition

        // update the graph
        TreeMap<Integer, GraphData> currentMap = tempDept.getGraphTreeMap();
        ArrayList<Integer> semesters = tempDept.getSemesters();
        updateTreeTable(currentMap, newData, semesters);
        tempDept.setSemesters(semesters);
        tempDept.setGraphTreeMap(currentMap);

        departments.add(tempDept);
    }

    // retrieve and sort csv data if we haven't already
    public void fetchData()
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
    public void structureData()
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

    //for each department, build the offerings, sections, and graph treeMaps
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

    //Perform updates to the treeTable if there are ever any changes that need to be done
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

    //Add the offering to the current class before moving on the next one
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

    //Build offering if only a single class is added
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

    //build offering if a class with multiple offerings is inserted
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

    //Build a temp data file to use for building the offering
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

    //Calculate the enrollment information per data
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

    //read in the csv data
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

    //convert csv read in data to a data file and insert into arraylist
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
}
