package ca.as4.models;

import java.util.ArrayList;

public class DisplayOrganizedData {
    public DisplayOrganizedData() { }

    public void printDump(ArrayList<Data>[] organizedData) {

        for (ArrayList<Data> currentList : organizedData) {
            if (currentList.size() > 0) {

                // create an ArrayList to hold our instructors
                ArrayList<String> instructors = new ArrayList<>();

                /*
                hold enrollment totals
                order of items in this array are:

                int enrollmentTotalLec, int enrollmentTotalOpl, int enrollmentTotalTut, int enrollmentTotalWks;

                int enrollmentCapacityLab, int enrollmentCapacityLec, int enrollmentCapacityOpl,
                int enrollmentCapacityTut, int enrollmentCapacityWks;

                */
                int[] enrollments = new int[] {0, 0, 0, 0, 0,
                                               0, 0, 0, 0, 0};

                /*
                hold our component status (know which one we printed)
                order of items in this array are:

                lec, lab, opl, tut, wks
                */

                boolean[] components = new boolean[] {false, false, false, false, false};
                int indexClass = 0;

                // print course group
                System.out.println(currentList.get(0).getSubject() + " " +
                        currentList.get(0).getCatalogNumber());

                if (currentList.size() == 1)
                {

                    ArrayList<String> currentInstructors =  currentList.get(0).getInstructors();
                    for (String currentInstructor : currentInstructors)
                    {
                        if (!(instructors.contains(currentInstructor)))
                        {
                            instructors.add(currentInstructor);
                        }
                    }

                    String listInstructors = instructors.toString();
                    listInstructors = listInstructors.replace("[", "");
                    listInstructors = listInstructors.replace("]", "");
                    listInstructors = listInstructors.trim();

                    if (listInstructors.contains("<null>"))
                    {
                        listInstructors = listInstructors.replace("<null>", "");
                    }

                    System.out.println("\t" + currentList.get(0).getSemester()+ " in "
                            + currentList.get(0).getLocation() + " by " + listInstructors);


                    System.out.println("\t\t" + "Type=" + currentList.get(0).getComponentCode()
                            + ", Enrollment=" + currentList.get(0).getEnrollmentTotal()
                            + "/" + currentList.get(0).getEnrollmentCapacity());

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
                            calculateCourseOfferings(currentList, j + 1, instructors, enrollments, components);
                        }
                        else
                        {
                            calculateCourseOfferings(currentList, j, instructors, enrollments, components);
                        }

                        if (components[0]) {
                            indexClass = j;
                        }
                    }

                    else
                    {
                        calculateCourseOfferings(currentList, j, instructors, enrollments, components);
                        if (components[0]) {
                            indexClass = j;
                        }
                    }

                    if (currentSem != currentList.get(j + 1).getSemester()
                            || !currentLoc.equals(currentList.get(j + 1).getLocation())
                            || j == currentList.size()-2)
                    {
                        printCourseOfferings(instructors, enrollments, components, currentSem, currentLoc,
                                indexClass, currentList);
                    }

                }
            }

            // place newline after current course group
            System.out.println();
        }
    }

    private void calculateCourseOfferings(ArrayList<Data> currentList, int j, ArrayList<String> instructors,
                                          int[] enrollments, boolean[] components)
    {

        // grab all the instructors in a class' semester's location
        // add those to our list who are not already in it
        ArrayList<String> currentInstructors =  currentList.get(j).getInstructors();
        for (String currentInstructor : currentInstructors) {
            if (!(instructors.contains(currentInstructor))) {
                instructors.add(currentInstructor);
            }
        }

        switch (currentList.get(j).getComponentCode()) {

            case "LAB":
                // add up all lab data for a class' semester's location
                enrollments[0] += currentList.get(j).getEnrollmentTotal();
                enrollments[5] += currentList.get(j).getEnrollmentCapacity();
                components[0] = true;
                break;

            case "OPL":
                // add up all pol data for a class' semester's location
                enrollments[2] += currentList.get(j).getEnrollmentTotal();
                enrollments[7] += currentList.get(j).getEnrollmentCapacity();
                components[2] = true;
                break;

            case "TUT":
                // add up all tut data for a class' semester's location
                enrollments[3] += currentList.get(j).getEnrollmentTotal();
                enrollments[8] += currentList.get(j).getEnrollmentCapacity();
                components[3] = true;
                break;

            case "WKS":
                // add up all opl data for a class' semester's location
                enrollments[4] += currentList.get(j).getEnrollmentTotal();
                enrollments[9] += currentList.get(j).getEnrollmentCapacity();
                components[4] = true;
                break;


            default:
                enrollments[1] += currentList.get(j).getEnrollmentTotal();
                enrollments[6] += currentList.get(j).getEnrollmentCapacity();
                components[1] = true;
        }

    }

    private void printCourseOfferings(ArrayList<String> instructors, int[] enrollments, boolean[] components,
                                      int currentSem, String currentLoc, int indexClass, ArrayList<Data> currentList) {
        System.out.println("\t" + currentSem + " in " + currentLoc + " by " +
                instructors.toString()
                        .replace("[", "")
                        .replace("]", "")
                        .replace("<null>", "")
                        .replace("  ", " ")
                        .trim());

        if (components[0]) {
            System.out.println("\t\t" + "Type=LAB, Enrollment=" + enrollments[0] +
                    "/" + enrollments[5]);

            enrollments[0] = 0;
            enrollments[5] = 0;
            components[0] = false;
        }

        if (components[1]) {
            System.out.println("\t\t" + "Type=" + currentList.get(indexClass).getComponentCode() +
                    ", Enrollment=" + enrollments[1] + "/" + enrollments[6]);

            enrollments[1] = 0;
            enrollments[6] = 0;
            components[1] = false;
        }

        if (components[2]) {
            System.out.println("\t\t" + "Type=OPL, Enrollment=" + enrollments[2] +
                    "/" + enrollments[7]);

            enrollments[2] = 0;
            enrollments[7] = 0;
            components[2] = false;
        }

        if (components[3]) {
            System.out.println("\t\t" + "Type=TUT, Enrollment=" + enrollments[3] +
                    "/" + enrollments[8]);

            enrollments[3] = 0;
            enrollments[8] = 0;
            components[3] = false;
        }

        if (components[4]) {
            System.out.println("\t\t" + "Type=WKS, Enrollment=" + enrollments[4] +
                    "/" + enrollments[9]);

            enrollments[4] = 0;
            enrollments[9] = 0;
            components[4] = false;
        }

        instructors.clear();
    }

    // Debug: Display all instructors
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

    // Debug: Display the class data in their grouped order
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
        }
    }

}
