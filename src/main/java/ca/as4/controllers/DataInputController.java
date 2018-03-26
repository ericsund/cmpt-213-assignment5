package ca.as4.controllers;

import ca.as4.models.Data;
import ca.as4.models.DisplayOrganizedData;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.System.exit;

@RestController
public class DataInputController {
    SortController sorter = new SortController();
    private ArrayList<String[]> csvData = new ArrayList<>();
    private ArrayList<Data> allData = new ArrayList<>();
    private final int SIZE_OF_A_CLASS = 8;
    private int numLists = 0;

    private String[] topCSVRow = {"SEMESTER", "SUBJECT", "CATALOGNUMBER",
                                  "LOCATION", "ENROLMENTCAPACITY", "ENROLMENTTOTAL",
                                  "INSTRUCTORS", "COMPONENTCODE"};

    @GetMapping("/dump-model")
    public void dumpModel()
    {
        CSVReader();
        populateDataModel();

        ArrayList<Data>[] organizeClasses = new ArrayList[numLists];
        buildArrayList(organizeClasses);
        sorter.sortDataByClassName(organizeClasses, allData);

//        DisplayOrganizedData display = new DisplayOrganizedData(allData);
    }

    private void CSVReader()
    {
        String CSVFileLocation = "data/course_data_2018.csv";
        String splitCSVBy = ",";
        String currentLine;

        try (BufferedReader br = new BufferedReader(new FileReader(CSVFileLocation)))
        {
            while ((currentLine = br.readLine()) != null)
            {
                String[] arrTemp = currentLine.split(splitCSVBy);
                csvData.add(arrTemp);
            }
        }
        catch (IOException e)
        {
            exit(0);
        }
    }

    private void populateDataModel()
    {
        for (String[] tempArr : csvData)
        {
            if (tempNotTopRow(tempArr[0]))
            {
                convertDataAndInsert(tempArr);
            }
        }
    }

    private boolean tempNotTopRow(String temp)
    {
        for (String compareString : topCSVRow)
        {
            if (compareString.equals(temp))
            {
                return false;
            }
        }
        return true;
    }

    private void convertDataAndInsert(String[] tempArr)
    {
        Data classData = new Data();
        int semester = Integer.parseInt(tempArr[0]);
        String subject = fixStrings(tempArr[1]);
        String catalogNumber = fixStrings(tempArr[2]);
        String location = fixStrings(tempArr[3]);
        int enrollmentCapacity = Integer.parseInt(tempArr[4]);
        int enrollmentTotal = Integer.parseInt(tempArr[5]);
        ArrayList<String> instructors = new ArrayList<>();
        String componentCode;

        if (tempArr.length >= SIZE_OF_A_CLASS && tempArr[7].length() > 3)
        {
            int currentIndex = 6;
            while (currentIndex < tempArr.length-1)
            {
                if (tempArr[currentIndex].contains("\""))
                {
                    String changedStr;
                    changedStr = tempArr[currentIndex].replace("\"", "");
                    changedStr = fixStrings(changedStr);

                    instructors.add(changedStr);
                }
                else
                {
                    String changedStr;
                    changedStr = fixStrings(tempArr[currentIndex]);
                    instructors.add(changedStr);
                }

                currentIndex++;
            }
            componentCode = tempArr[currentIndex];
        }
        else
        {
            if (tempArr[6].contains("\""))
            {
                String changedStr = tempArr[6].replace("\"", "");
                changedStr = fixStrings(changedStr);
                instructors.add(changedStr);
            }
            else
            {
                String changedStr;
                changedStr = fixStrings(tempArr[6]);
                instructors.add(changedStr);
            }
            componentCode = tempArr[7];
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

        for (int i = 0; i < stringToFix.length()-1; i++)
        {
            if (stringToFix.charAt(i) == ' ' && stringToFix.charAt(i+1) == ' ')
            {
                stringToFix = changedStr.substring(0, i);
            }
        }

        return stringToFix;
    }

    private void buildArrayList(ArrayList<Data>[] organizeByClass)
    {
        for (int i = 0; i < numLists; i++)
        {
            organizeByClass[i] = new ArrayList<>();
        }
    }
}
