package ca.as4.models;

import java.util.ArrayList;
import java.util.Collections;

public class DisplayOrganizedData {
    public DisplayOrganizedData() { }

    public void printDump(ArrayList<Data>[] organizedData) {

        System.out.println();
        for (ArrayList<Data> currentList : organizedData) {
            if (currentList.size() > 0) {

                // create an ArrayList to hold our instructors
                ArrayList<String> instructors = new ArrayList<>();
                boolean lastNotDisplayed = false;

                /*
                hold enrollment totals
                order of items in this array are:

                EnrollmentTotal: LAB, OPL, TUT, WKS, FLD, SEC, STD, OLC, STL, RQL, SEM, PRA, INS, CNV, LEC
                EnrollmentCapacity: LAB, OPL, TUT, WKS, FLD, SEC, STD, OLC, STL, RQL, SEM, PRA, INS, CNV, LEC

                */
                int[] enrollments = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

                /*
                hold our component status (know which one we printed)
                order of items in this array are:

                Component Status: LAB, OPL, TUT, WKS, FLD, SEC, STD, OLC, STL, RQL, SEM, PRA, INS, CNV, LEC
                */

                boolean[] components = new boolean[] {false, false, false, false, false, false, false,
                                                      false, false, false, false, false, false, false,
                                                      false};

                // print course group
                System.out.println(currentList.get(0).getSubject() + " " +
                        currentList.get(0).getCatalogNumber());

                if (currentList.size() == 1)
                {
                    calculateCourseOfferings(currentList, 0, instructors, enrollments, components);
                    printCourseOfferings(instructors, enrollments, components, currentList.get(0).getSemester(),
                            currentList.get(0).getLocation(), currentList);
                }

                for (int j = 0; j < currentList.size() - 1; j++)
                {

                    // get current semester, current location
                    int currentSem = currentList.get(j).getSemester();
                    String currentLoc = currentList.get(j).getLocation();

                    // check if the current semester is the same as the next one
                    // keep checking unless we reach the end of the ArrayList

                    if (currentSem == currentList.get(j + 1).getSemester()
                            && currentLoc.equals(currentList.get(j + 1).getLocation()))
                    {

                        if (j == currentList.size() - 2)
                        {
                            calculateCourseOfferings(currentList, j, instructors, enrollments, components);
                            calculateCourseOfferings(currentList, j + 1, instructors, enrollments, components);
                        }
                        else
                        {
                            calculateCourseOfferings(currentList, j, instructors, enrollments, components);
                        }
                    }

                    else
                    {
                        calculateCourseOfferings(currentList, j, instructors, enrollments, components);

                        if (j == currentList.size()-2)
                        {
                            lastNotDisplayed = true;
                        }

                    }

                    if (currentSem != currentList.get(j + 1).getSemester()
                            || !currentLoc.equals(currentList.get(j + 1).getLocation())
                            || j == currentList.size()-2)
                    {
                        printCourseOfferings(instructors, enrollments, components, currentSem, currentLoc, currentList);
                    }

                    if (lastNotDisplayed)
                    {
                        calculateCourseOfferings(currentList, j+1, instructors, enrollments, components);
                        printCourseOfferings(instructors, enrollments, components, currentList.get(j + 1).getSemester()
                                , currentList.get(j + 1).getLocation(), currentList);
                    }
                }
            }

            // place newline after current course group
//            System.out.println();
        }
    }

    private void calculateCourseOfferings(ArrayList<Data> currentList, int j, ArrayList<String> instructors,
                                          int[] enrollments, boolean[] components)
    {

        // grab all the instructors in a class' semester's location
        // add those to our list who are not already in it
        ArrayList<String> currentInstructors =  currentList.get(j).getInstructors();
        currentInstructors.removeAll(Collections.singleton("<null>"));
        currentInstructors.removeAll(Collections.singleton("(null)"));
        for (String currentInstructor : currentInstructors) {
            if (!(instructors.contains(currentInstructor))) {
                instructors.add(currentInstructor);
            }
        }

        perClassCalculations(currentList, j, enrollments, components);
    }

    private void printCourseOfferings(ArrayList<String> instructors, int[] enrollments, boolean[] components,
                                      int currentSem, String currentLoc, ArrayList<Data> currentList) {
        System.out.println("\t" + currentSem + " in " + currentLoc + " by " +
                instructors.toString()
                        .replace("[", "")
                        .replace("]", "")
                        .replace("  ", " ")
                        .trim());

        ArrayList<String> allOfferrings = new ArrayList<>();
        displayFormatter(enrollments, components, currentList, allOfferrings);
        Collections.sort(allOfferrings);

        for (String currentString : allOfferrings) {
            System.out.println(currentString);
        }

        allOfferrings.clear();
        instructors.clear();
    }

    private void setEnrollmentArr(int[] enrollments, boolean[] components, int i, int j,
                                  int indexList, ArrayList<Data> currentList, boolean reset)
    {
        if (reset)
        {
            enrollments[i] = 0;
            enrollments[j] = 0;
            components[i] = false;
        }
        else
        {
            enrollments[i] += currentList.get(indexList).getEnrollmentTotal();
            enrollments[j] += currentList.get(indexList).getEnrollmentCapacity();
            components[i] = true;
        }
    }

    private void perClassCalculations(ArrayList<Data> currentList, int j, int[] enrollments, boolean[] components) {
        switch (currentList.get(j).getComponentCode()) {

            case "LAB":
                // add up all lab data for a class' semester's location
                setEnrollmentArr(enrollments, components, 0, 15, j, currentList, false);
                break;

            case "OPL":
                // add up all pol data for a class' semester's location
                setEnrollmentArr(enrollments, components, 1, 16, j, currentList, false);
                break;

            case "TUT":
                // add up all tut data for a class' semester's location
                setEnrollmentArr(enrollments, components, 2, 17, j, currentList, false);
                break;

            case "WKS":
                // add up all opl data for a class' semester's location
                setEnrollmentArr(enrollments, components, 3, 18, j, currentList, false);
                break;

            case "FLD":
                setEnrollmentArr(enrollments, components, 4, 19, j, currentList, false);
                break;

            case "SEC":
                setEnrollmentArr(enrollments, components, 5, 20, j, currentList, false);
                break;

            case "STD":
                setEnrollmentArr(enrollments, components, 6, 21, j, currentList, false);
                break;

            case "OLC":
                setEnrollmentArr(enrollments, components, 7, 22, j, currentList, false);
                break;

            case "STL":
                setEnrollmentArr(enrollments, components, 8, 23, j, currentList, false);
                break;

            case "RQL":
                setEnrollmentArr(enrollments, components, 9, 24, j, currentList, false);
                break;

            case "SEM":
                setEnrollmentArr(enrollments, components, 10, 25, j, currentList, false);
                break;

            case "PRA":
                setEnrollmentArr(enrollments, components, 11, 26, j, currentList, false);
                break;

            case "INS":
                setEnrollmentArr(enrollments, components, 12, 27, j, currentList, false);
                break;

            case "CNV":
                setEnrollmentArr(enrollments, components, 13, 28, j, currentList, false);
                break;

            default:
                setEnrollmentArr(enrollments, components, 14, 29, j, currentList, false);
        }
    }

    private void displayFormatter(int[] enrollments, boolean[] components, ArrayList<Data> currentList, ArrayList<String> allOfferrings) {
        if (components[0]) {
            allOfferrings.add("\t\t" + "Type=LAB, Enrollment=" + enrollments[0] +
                    "/" + enrollments[15]);

            setEnrollmentArr(enrollments, components, 0, 15, 0, currentList, true);
        }

        if (components[1]) {
            allOfferrings.add("\t\t" + "Type=OPL, Enrollment=" + enrollments[1] +
                    "/" + enrollments[16]);

            setEnrollmentArr(enrollments, components, 1, 16, 0, currentList, true);
        }

        if (components[2]) {
            allOfferrings.add("\t\t" + "Type=TUT, Enrollment=" + enrollments[2] +
                    "/" + enrollments[17]);

            setEnrollmentArr(enrollments, components, 2, 17, 0, currentList, true);
        }

        if (components[3]) {
            allOfferrings.add("\t\t" + "Type=WKS, Enrollment=" + enrollments[3] +
                    "/" + enrollments[18]);

            setEnrollmentArr(enrollments, components, 3, 18, 0, currentList, true);
        }

        if (components[4]) {
            allOfferrings.add("\t\t" + "Type=FLD, Enrollment=" + enrollments[4] +
                    "/" + enrollments[19]);

            setEnrollmentArr(enrollments, components, 4, 19, 0, currentList, true);
        }
        if (components[5]) {
            allOfferrings.add("\t\t" + "Type=SEC, Enrollment=" + enrollments[5] +
                    "/" + enrollments[20]);

            setEnrollmentArr(enrollments, components, 5, 20, 0, currentList, true);
        }

        if (components[6]) {
            allOfferrings.add("\t\t" + "Type=STD, Enrollment=" + enrollments[6] +
                    "/" + enrollments[21]);

            setEnrollmentArr(enrollments, components, 6, 21, 0, currentList, true);
        }

        if (components[7]) {
            allOfferrings.add("\t\t" + "Type=OLC, Enrollment=" + enrollments[7] +
                    "/" + enrollments[22]);

            setEnrollmentArr(enrollments, components, 7, 22, 0, currentList, true);
        }

        if (components[8]) {
            allOfferrings.add("\t\t" + "Type=STL, Enrollment=" + enrollments[8] +
                    "/" + enrollments[23]);

            setEnrollmentArr(enrollments, components, 8, 23, 0, currentList, true);
        }

        if (components[9]) {
            allOfferrings.add("\t\t" + "Type=RQL, Enrollment=" + enrollments[9] +
                    "/" + enrollments[24]);

            setEnrollmentArr(enrollments, components, 9, 24, 0, currentList, true);
        }

        if (components[10]) {
            allOfferrings.add("\t\t" + "Type=SEM, Enrollment=" + enrollments[10] +
                    "/" + enrollments[25]);

            setEnrollmentArr(enrollments, components, 10, 25, 0, currentList, true);
        }

        if (components[11]) {
            allOfferrings.add("\t\t" + "Type=PRA, Enrollment=" + enrollments[11] +
                    "/" + enrollments[26]);

            setEnrollmentArr(enrollments, components, 11, 26, 0, currentList, true);
        }

        if (components[12]) {
            allOfferrings.add("\t\t" + "Type=PRA, Enrollment=" + enrollments[12] +
                    "/" + enrollments[27]);

            setEnrollmentArr(enrollments, components, 11, 27, 0, currentList, true);
        }

        if (components[13])
        {
            allOfferrings.add("\t\t" + "Type=CNV, Enrollment=" + enrollments[13] +
                    "/" + enrollments[28]);

            setEnrollmentArr(enrollments, components, 13, 28, 0, currentList, true);
        }

        if (components[14]) {
            allOfferrings.add("\t\t" + "Type=LEC, Enrollment=" + enrollments[14] +
                    "/" + enrollments[29]);

            setEnrollmentArr(enrollments, components, 14, 29, 0, currentList, true);
        }
    }

    // Debug: Display the class data in their grouped order
    private String displayAllInstructors(Data classFileWithInstructors)
    {
        ArrayList<String> instructors = classFileWithInstructors.getInstructors();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < instructors.size(); i++)
        {
            if (i != instructors.size()-1)
            {
                sb.append(instructors.get(i));
                sb.append(",");
            }
            else
            {
                sb.append(instructors.get(i));
            }
        }

        return sb.toString();
    }

    // Debug: Display all instructors
    public void displayClassData(ArrayList<Data>[] allSortedClasses)
    {
        System.out.println();

        int countNumData = 0;
        for (ArrayList<Data> currentList : allSortedClasses)
        {
            if (currentList.size() > 0)
            {
                for (Data currentData : currentList)
                {
                    System.out.print(countNumData + ". " + currentData.getSemester() + ", " + currentData.getSubject() + ", "
                            + currentData.getCatalogNumber() + ", " + currentData.getLocation() + ", "
                            + currentData.getEnrollmentCapacity() + ", " + currentData.getEnrollmentTotal() + ", ");

                    System.out.print(displayAllInstructors(currentData) + ", ");

                    System.out.println(currentData.getComponentCode());
                    countNumData++;
                }
                System.out.println();
            }

            if (countNumData >= 3000)
            {
                break;
            }
        }
    }

}
