package ca.as4.models;

import java.util.ArrayList;

public class DisplayOrganizedData {
    private ArrayList<Data> courseData;
    private int finalAmountListsUsed = 0;

    public DisplayOrganizedData(ArrayList<Data> courseData) {
        this.courseData = courseData;

//        for (Data currentData : courseData) {
//            System.out.println(currentData.getSemester());
//            System.out.println(currentData.getSubject());
//            System.out.println(currentData.getCatalogNumber());
//            System.out.println(currentData.getLocation());
//            System.out.println(currentData.getEnrollmentCapacity());
//            System.out.println(currentData.getEnrollmentTotal());
//            for (String instructor : currentData.getInstructors()) {
//                System.out.println(instructor);
//            }
//            System.out.println(currentData.getComponentCode());
//        }
    }
}
