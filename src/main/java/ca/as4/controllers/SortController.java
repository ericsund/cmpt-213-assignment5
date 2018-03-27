package ca.as4.controllers;

import ca.as4.models.Data;
import java.util.ArrayList;
import java.util.Arrays;

public class SortController {

    public SortController() { }

    public void sortDataByClassName(ArrayList<Data>[] organizeClasses, ArrayList<Data> allData)
    {
        int counter = 0;
        for (int i = 0; i < allData.size(); i++)
        {
            boolean inOrganizedClass = checkInOrganizedClass(allData.get(i), organizeClasses);
            boolean isComponentCodeAClass = (!allData.get(i).getComponentCode().equals("TUT")
                    || !allData.get(i).getComponentCode().equals("LAB")
                    || !allData.get(i).getComponentCode().equals("OPL")
                    || !allData.get(i).getComponentCode().equals("WKS"));

            if (!inOrganizedClass && isComponentCodeAClass)
            {
                organizeClasses[counter].add(allData.get(i));
                findAndInsertRelatedData(allData, organizeClasses, allData.get(i), counter);
                counter++;
            }
        }

        displayClassData(organizeClasses);
//        displayDump(organizeClasses);
    }

    private boolean checkInOrganizedClass(Data checkData, ArrayList<Data>[] organizeClasses)
    {
        for (int i = 0; i < organizeClasses.length; i++)
        {
            if (organizeClasses[i].contains(checkData))
            {
                return true;
            }
        }
        return false;
    }

    private void findAndInsertRelatedData(ArrayList<Data> allData, ArrayList<Data>[] organizeClasses, Data compareData, int counter)
    {
        ArrayList<Data> otherCampus = new ArrayList<>();
        int indexLecOfOtherCampus = 0;
        int numClassesInOtherCampus = 0;
        boolean otherCampusDataPresent = false;

        for (Data currentFile : allData)
        {
            if (currentFile != compareData)
            {
                boolean dataIsRelated = checkClassRelated(currentFile, compareData);
                if (dataIsRelated)
                {
                    if (!currentFile.getLocation().equals(compareData.getLocation()))
                    {
                        otherCampusDataPresent = true;
                        if (currentFile.getComponentCode().equals("LEC"))
                        {
                            indexLecOfOtherCampus = numClassesInOtherCampus;
                        }

                        otherCampus.add(currentFile);
                        numClassesInOtherCampus++;
                    }
                    else
                    {
                        organizeClasses[counter].add(currentFile);
                    }
                }
            }
        }

        if (otherCampusDataPresent)
        {
            organizeClasses[counter].add(otherCampus.get(indexLecOfOtherCampus));
            for (int i = 0; i < otherCampus.size(); i++)
            {
                if (i != indexLecOfOtherCampus)
                {
                    organizeClasses[counter].add(otherCampus.get(i));
                }
            }
        }
    }

    private boolean checkClassRelated(Data currentFile, Data compareData)
    {
        boolean checkSemesterSame = compareData.getSemester() == currentFile.getSemester();
        boolean checkSubjectSame = compareData.getSubject().equals(currentFile.getSubject());
        boolean checkCatalogSame = compareData.getCatalogNumber().equals(currentFile.getCatalogNumber());

        if (checkSemesterSame && checkSubjectSame && checkCatalogSame)
        {
            return true;
        }
        return false;
    }

    //Debug: Display the class data in their grouped order
    private void displayClassData(ArrayList<Data>[] organizedData)
    {
        System.out.println();

        int countNumData = 0;
        for (ArrayList<Data> currentList : organizedData)
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

    /* todo this method scans through each element in the organizedData ArrayList, and prints out course offerings
    for each course.  Each element in organizedData is a specific semester (ie. 1171) of a course (ie. CMPT 295).
    Each course was assumed to be grouped side-by-side (ie. semester A of CMPT 295, semester B of CMPT 295, semester A
    of MATH 232, semester B of MATH 232, etc.).  This method checks if consecutive courses are the same, and prints its
    offerings accordingly.  Bug right now: it doesn't print offerings for non-consecutive courses.

    Something to consider: is this even a good approach?  If we want to do this then we need to make sure all courses
    are grouped together, which is not the case right now (ie. 1177 CMPT 295 is way down the list away from everything).
    */


//    private void displayDump(ArrayList<Data>[] organizedData) {
////        for (ArrayList<Data> currentList : organizedData)
////        {
////            System.out.println(currentList.get(0).getSubject() + " "
////                    + currentList.get(0).getCatalogNumber());
////
////            System.out.print("      ");
////
////        }
//
//        for (int j = 0; j < organizedData.length; j++) {
//
//            ArrayList<Data> currentList = organizedData[j];
//            if (currentList.size() > 0) {
//
//                // on the very first iteration, print everything
//                if (j == 0) {
//
//                    // print the course subject and catalog number
//                    System.out.println(currentList.get(0).getSubject() + " " +
//                            currentList.get(0).getCatalogNumber());
//
//                    // per section and location of a course, print its offerings
//                    for (int i = 0; i < currentList.size(); i++) {
//                        System.out.println("\t" + currentList.get(i).getSemester() + " in " +
//                                currentList.get(i).getLocation() + " by " + currentList.get(i).getInstructors());
//                        System.out.println("\tType=" + currentList.get(i).getComponentCode() + ", Enrollment=blank");
//                    }
//                }
//
//                // on all other iterations
//                // if the current course is the same as the last one, we only need its offerings
//                else if (organizedData[j].get(0).getSubject().equals(organizedData[j - 1].get(0).getSubject()) &&
//                organizedData[j].get(0).getCatalogNumber().equals(organizedData[j - 1].get(0).getCatalogNumber())) {
//
//                    // per section and location of a course, print its offerings
//                    for (int i = 0; i < currentList.size(); i++) {
//                        System.out.println("\t" + currentList.get(i).getSemester() + " in " +
//                                currentList.get(i).getLocation() + " by " + currentList.get(i).getInstructors());
//                        System.out.println("\tType=" + currentList.get(i).getComponentCode() + ", Enrollment=blank");
//                    }
//                }
//
//                else {
//                    // print the course subject and catalog number
//                    System.out.println(currentList.get(0).getSubject() + " " +
//                            currentList.get(0).getCatalogNumber());
//
                      // todo the output gets corrupted when trying to print the course offerings for a new course here
//                    // per section and location of a course, print its offerings
//                    for (int i = 0; i < currentList.size(); i++) {
//                        System.out.println("\t" + currentList.get(i).getSemester() + " in " +
//                                currentList.get(i).getLocation() + " by " + currentList.get(i).getInstructors());
//                        System.out.println("\tType=" + currentList.get(i).getComponentCode() + ", Enrollment=blank");
//                    }
//                }
//            }
//        }
//    }

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
}
