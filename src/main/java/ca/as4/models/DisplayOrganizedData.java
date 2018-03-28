package ca.as4.models;

import java.util.ArrayList;

public class DisplayOrganizedData {
    public DisplayOrganizedData() {
    }

    //Debug: Display the class data in their grouped order
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

    // todo move a lot of this code into private functions to clean it up!
    //Debug: Display the class data in their grouped order
    public void printDump(ArrayList<Data>[] allSortedClasses) {

        for (ArrayList<Data> currentList : allSortedClasses) {
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
