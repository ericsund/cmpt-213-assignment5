package ca.as4.controllers;

import ca.as4.models.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class SortController {
    private int numClassesInList = 0;

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

        numClassesInList = counter;

        System.out.print(numClassesInList + " " + organizeClasses.length + " " + allData.size());

        ArrayList<Data>[] fixedListClasses = fixArrayListSize(organizeClasses);
        sortEachArrayList(fixedListClasses);
//        displayClassData(fixedListClasses);
        printDump(organizeClasses);
    }

    private ArrayList<Data>[] fixArrayListSize(ArrayList<Data>[] organizedClasses)
    {
        ArrayList<Data>[] fixedArr = new ArrayList[numClassesInList];
        for (int i = 0; i < numClassesInList; i++)
        {
            fixedArr[i] = new ArrayList<>();
            fixedArr[i] = organizedClasses[i];
        }
        return fixedArr;
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

    private void sortEachArrayList(ArrayList<Data>[] organizeClasses)
    {
        for (int i = 0; i < organizeClasses.length; i++)
        {
            Collections.sort(organizeClasses[i], new Comparator<Data>() {
                @Override
                public int compare(Data o1, Data o2) {
                    return o1.getSemester() - o2.getSemester();
                }
            });
        }

        Arrays.sort(organizeClasses, new Comparator<ArrayList<Data>>() {
            @Override
            public int compare(ArrayList<Data> o1, ArrayList<Data> o2) {
                String o1Class = o1.get(0).getSubject() + " " + o1.get(0).getCatalogNumber();
                String o2Class = o2.get(0).getSubject() + " " + o2.get(0).getCatalogNumber();
                return o1Class.compareTo(o2Class);
            }
        });
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
//        boolean checkSemesterSame = compareData.getSemester() == currentFile.getSemester();
        boolean checkSubjectSame = compareData.getSubject().equals(currentFile.getSubject());
        boolean checkCatalogSame = compareData.getCatalogNumber().equals(currentFile.getCatalogNumber());

        if (checkSubjectSame && checkCatalogSame)
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

    // groups all sections of the same class together
//    private ArrayList<ArrayList<ArrayList<Data>>> groupByClass(ArrayList<Data>[] organizedData) {
//        ArrayList<ArrayList<ArrayList<Data>>> groupedData = new ArrayList<>();
//        boolean addedExisting = false;
//
//
//
//        for (int j = 0; j < organizedData.length; j++) {
//            if (organizedData[j].size() > 0) {
//
//                String currentSubject = organizedData[j].get(0).getSubject();
//                String currentCatalogNumber = organizedData[j].get(0).getCatalogNumber();
//
//                // check if this is inside our groupedData structure
//                addToGroup : {
//                    for (int i = 0; i < groupedData.size(); i++) {
//
//                        if (groupedData.get(i).size() > 0) {
//
//                            String subjectCompare = groupedData.get(i).get(0).get(0).getSubject();
//                            String catalogNumberCompare = groupedData.get(i).get(0).get(0).getSubject();
//
//                            // add to existing group
//                            if (currentSubject.equals(subjectCompare) && currentCatalogNumber.equals(catalogNumberCompare)) {
//                                groupedData.get(i).add(organizedData[j]);
//                                addedExisting = true;
//                                break addToGroup;
//                            }
//                        }
//                    }
//                }
//
//                if (!addedExisting) {
//                    // we searched all of groupData and did not find it, so add the ArrayList to the back (a new spot)
//                    ArrayList<ArrayList<Data>> newList = new ArrayList<>();
//                    newList.add(organizedData[j]);
//                    groupedData.add(newList);
//                    addedExisting = false;
//                }
//            }
//        }
//        return groupedData;
//    }
//

    // todo move a lot of this code into private functions to clean it up!
    //Debug: Display the class data in their grouped order
    private void printDump(ArrayList<Data>[] organizedData) {

        for (ArrayList<Data> currentList : organizedData) {
            if (currentList.size() > 0) {

                // create an ArrayList to hold our instructors
                ArrayList<String> instructors = new ArrayList<>();
                // hold enrollments totals
                int enrollmentTotalLab = 0;
                int enrollmentTotalLec = 0;
                int enrollmentTotalOpl = 0;
                int enrollmentTotalTut = 0;
                int enrollmentTotalWks = 0;
                // hold enrollment capacities
                int enrollmentCapacityLec = 0;
                int enrollmentCapacityLab = 0;
                int enrollmentCapacityOpl = 0;
                int enrollmentCapacityTut = 0;
                int enrollmentCapacityWks = 0;

                boolean lab = false;
                boolean lec = false;
                boolean opl = false;
                boolean tut = false;
                boolean wks = false;

                // print course group
                System.out.println(currentList.get(0).getSubject() + " " +
                        currentList.get(0).getCatalogNumber());

                for (int j = 0; j < currentList.size(); j++) {

                    // get current semester, current location
                    int currentSem = currentList.get(j).getSemester();
                    String currentLoc = currentList.get(j).getLocation();

                    // check if the current semester is the same as the next one
                    // keep checking unless we reach the end of the ArrayList
                    if (j + 1 < currentList.size()) {

                        if (currentSem == currentList.get(j + 1).getSemester()) {

                            if (currentLoc.equals(currentList.get(j + 1).getLocation())) {
                                // grab all the instructors in a class' semester's location
                                // add those to our list who are not already in it
                                ArrayList<String> currentInstructors =  currentList.get(j).getInstructors();
                                for (String currentInstructor : currentInstructors) {
                                    if (!(instructors.contains(currentInstructor))) {
                                        instructors.add(currentInstructor);
                                    }
                                }

                                // add up all lab data for a class' semester's location
                                if (currentList.get(j).getComponentCode().equals("LAB")) {
                                    enrollmentTotalLab += currentList.get(j).getEnrollmentTotal();
                                    enrollmentCapacityLab += currentList.get(j).getEnrollmentCapacity();
                                    lab = true;
                                }

                                // add up all lec data for a class' semester's location
                                if (currentList.get(j).getComponentCode().equals("LEC")) {
                                    enrollmentTotalLec += currentList.get(j).getEnrollmentTotal();
                                    enrollmentCapacityLec += currentList.get(j).getEnrollmentCapacity();
                                    lec = true;
                                }

                                // add up all pol data for a class' semester's location
                                if (currentList.get(j).getComponentCode().equals("OPL")) {
                                    enrollmentTotalOpl += currentList.get(j).getEnrollmentTotal();
                                    enrollmentCapacityOpl += currentList.get(j).getEnrollmentCapacity();
                                    opl = true;
                                }

                                // add up all tut data for a class' semester's location
                                if (currentList.get(j).getComponentCode().equals("TUT")) {
                                    enrollmentTotalTut += currentList.get(j).getEnrollmentTotal();
                                    enrollmentCapacityTut += currentList.get(j).getEnrollmentCapacity();
                                    tut = true;
                                }

                                // add up all opl data for a class' semester's location
                                if (currentList.get(j).getComponentCode().equals("WKS")) {
                                    enrollmentTotalWks += currentList.get(j).getEnrollmentTotal();
                                    enrollmentCapacityWks += currentList.get(j).getEnrollmentCapacity();
                                    wks = true;
                                }

                            }

                            // currentLoc != currentLoc+1
                            else {
                                System.out.println("\t" + currentSem + " in " + currentLoc + " by " +
                                        instructors.toString());

                                if (lab) {
                                    System.out.println("\t\t" + "Type=LAB, Enrollment=" + enrollmentTotalLab +
                                            "/" + enrollmentCapacityLab);

                                    enrollmentTotalLab = 0;
                                    enrollmentCapacityLab = 0;
                                    lab = false;
                                }

                                if (lec) {
                                    System.out.println("\t\t" + "Type=LEC, Enrollment=" + enrollmentTotalLec +
                                            "/" + enrollmentCapacityLec);

                                    enrollmentTotalLec = 0;
                                    enrollmentCapacityLec = 0;
                                    lec = false;
                                }

                                if (opl) {
                                    System.out.println("\t\t" + "Type=OPL, Enrollment=" + enrollmentTotalOpl +
                                            "/" + enrollmentCapacityOpl);

                                    enrollmentTotalOpl = 0;
                                    enrollmentCapacityOpl = 0;
                                    opl = false;
                                }

                                if (tut) {
                                    System.out.println("\t\t" + "Type=TUT, Enrollment=" + enrollmentTotalTut +
                                            "/" + enrollmentCapacityTut);

                                    enrollmentTotalTut = 0;
                                    enrollmentCapacityTut = 0;
                                    tut = false;
                                }

                                if (wks) {
                                    System.out.println("\t\t" + "Type=WKS, Enrollment=" + enrollmentTotalWks +
                                            "/" + enrollmentCapacityWks);

                                    enrollmentTotalWks = 0;
                                    enrollmentCapacityWks = 0;
                                    wks = false;
                                }

                                instructors.clear();
                            }

                        }

                        // currentSem != currentSem+1
                        else {
                            System.out.println("\t" + currentSem + " in " + currentLoc + " by " +
                                    instructors.toString());

                            if (lab) {
                                System.out.println("\t\t" + "Type=LAB, Enrollment=" + enrollmentTotalLab +
                                        "/" + enrollmentCapacityLab);

                                enrollmentTotalLab = 0;
                                enrollmentCapacityLab = 0;
                                lab = false;
                            }

                            if (lec) {
                                System.out.println("\t\t" + "Type=LEC, Enrollment=" + enrollmentTotalLec +
                                        "/" + enrollmentCapacityLec);

                                enrollmentTotalLec = 0;
                                enrollmentCapacityLec = 0;
                                lec = false;
                            }

                            if (opl) {
                                System.out.println("\t\t" + "Type=OPL, Enrollment=" + enrollmentTotalOpl +
                                        "/" + enrollmentCapacityOpl);

                                enrollmentTotalOpl = 0;
                                enrollmentCapacityOpl = 0;
                                opl = false;
                            }

                            if (tut) {
                                System.out.println("\t\t" + "Type=TUT, Enrollment=" + enrollmentTotalTut +
                                        "/" + enrollmentCapacityTut);

                                enrollmentTotalTut = 0;
                                enrollmentCapacityTut = 0;
                                tut = false;
                            }

                            if (wks) {
                                System.out.println("\t\t" + "Type=WKS, Enrollment=" + enrollmentTotalWks +
                                        "/" + enrollmentCapacityWks);

                                enrollmentTotalWks = 0;
                                enrollmentCapacityWks = 0;
                                wks = false;
                            }

                            instructors.clear();
                        }
                    }

                    // todo this might be missing the calculation of the last component codes...
                    // reached end of ArrayList size, print last element we had
                    else {

                        // grab all the instructors in a class' semester's location
                        // add those to our list who are not already in it
                        ArrayList<String> currentInstructors =  currentList.get(j).getInstructors();
                        for (String currentInstructor : currentInstructors) {
                            if (!(instructors.contains(currentInstructor))) {
                                instructors.add(currentInstructor);
                            }
                        }

                        // add up all lab data for a class' semester's location
                        if (currentList.get(j).getComponentCode().equals("LAB")) {
                            enrollmentTotalLab += currentList.get(j).getEnrollmentTotal();
                            enrollmentCapacityLab += currentList.get(j).getEnrollmentCapacity();
                            lab = true;
                        }

                        // add up all lec data for a class' semester's location
                        if (currentList.get(j).getComponentCode().equals("LEC")) {
                            enrollmentTotalLec += currentList.get(j).getEnrollmentTotal();
                            enrollmentCapacityLec += currentList.get(j).getEnrollmentCapacity();
                            lec = true;
                        }

                        // add up all pol data for a class' semester's location
                        if (currentList.get(j).getComponentCode().equals("OPL")) {
                            enrollmentTotalOpl += currentList.get(j).getEnrollmentTotal();
                            enrollmentCapacityOpl += currentList.get(j).getEnrollmentCapacity();
                            opl = true;
                        }

                        // add up all tut data for a class' semester's location
                        if (currentList.get(j).getComponentCode().equals("TUT")) {
                            enrollmentTotalTut += currentList.get(j).getEnrollmentTotal();
                            enrollmentCapacityTut += currentList.get(j).getEnrollmentCapacity();
                            tut = true;
                        }

                        // add up all opl data for a class' semester's location
                        if (currentList.get(j).getComponentCode().equals("WKS")) {
                            enrollmentTotalWks += currentList.get(j).getEnrollmentTotal();
                            enrollmentCapacityWks += currentList.get(j).getEnrollmentCapacity();
                            wks = true;
                        }


                        System.out.println("\t" + currentSem + " in " + currentLoc + " by " +
                                instructors.toString());

                        if (lab) {
                            System.out.println("\t\t" + "Type=LAB, Enrollment=" + enrollmentTotalLab +
                                    "/" + enrollmentCapacityLab);

                            enrollmentTotalLab = 0;
                            enrollmentCapacityLab = 0;
                            lab = false;
                        }

                        if (lec) {
                            System.out.println("\t\t" + "Type=LEC, Enrollment=" + enrollmentTotalLec +
                                    "/" + enrollmentCapacityLec);

                            enrollmentTotalLec = 0;
                            enrollmentCapacityLec = 0;
                            lec = false;
                        }

                        if (opl) {
                            System.out.println("\t\t" + "Type=OPL, Enrollment=" + enrollmentTotalOpl +
                                    "/" + enrollmentCapacityOpl);

                            enrollmentTotalOpl = 0;
                            enrollmentCapacityOpl = 0;
                            opl = false;
                        }

                        if (tut) {
                            System.out.println("\t\t" + "Type=TUT, Enrollment=" + enrollmentTotalTut +
                                    "/" + enrollmentCapacityTut);

                            enrollmentTotalTut = 0;
                            enrollmentCapacityTut = 0;
                            tut = false;
                        }

                        if (wks) {
                            System.out.println("\t\t" + "Type=WKS, Enrollment=" + enrollmentTotalWks +
                                    "/" + enrollmentCapacityWks);

                            enrollmentTotalWks = 0;
                            enrollmentCapacityWks = 0;
                            wks = false;
                        }

                        instructors.clear();
                    }
                }
            }

            // place newline after current course group
            System.out.println();
        }
    }

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
