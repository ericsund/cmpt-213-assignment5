package ca.as4.controllers;

import ca.as4.models.Data;
import ca.as4.models.DisplayOrganizedData;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.System.exit;

@RestController
public class DataInputController {
    private ArrayList<String[]> csvData = new ArrayList<>();
    private ArrayList<Data> allData = new ArrayList<>();
    private final int SIZE_OF_A_CLASS = 8;

    private String[] topCSVRow = {"SEMESTER", "SUBJECT", "CATALOGNUMBER",
                                  "LOCATION", "ENROLMENTCAPACITY", "ENROLMENTTOTAL",
                                  "INSTRUCTORS", "COMPONENTCODE"};

    @GetMapping("/dump-model")
    public void dumpModel()
    {
        CSVReader();
        populateDataModel();

        DisplayOrganizedData display = new DisplayOrganizedData(allData);
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
        String subject = tempArr[1];
        String catalogNumber = tempArr[2];
        String location = tempArr[3];
        int enrollmentCapacity = Integer.parseInt(tempArr[4]);
        int enrollmentTotal = Integer.parseInt(tempArr[5]);
        ArrayList<String> instructors = new ArrayList<>();
        String componentCode = "";

        if (tempArr.length >= SIZE_OF_A_CLASS && tempArr[7].length() > 3)
        {
            int currentIndex = 6;
            while (currentIndex < tempArr.length)
            {
                if (tempArr[currentIndex].contains("\""))
                {
                    String changedStr = tempArr[currentIndex].replace("\"", "");
                    instructors.add(changedStr);
                }
                else
                {
                    instructors.add(tempArr[currentIndex]);
                }

                currentIndex++;
            }
        }
        else
        {
            if (tempArr[6].contains("\""))
            {
                String changedStr = tempArr[6].replace("\"", "");
                instructors.add(changedStr);
            }
            else
            {
                instructors.add(tempArr[6]);
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
        
        allData.add(classData);
    }
}
