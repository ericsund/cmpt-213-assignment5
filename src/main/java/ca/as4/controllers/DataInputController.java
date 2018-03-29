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
    private DisplayOrganizedData display = new DisplayOrganizedData();
    private SortController sorter = new SortController();
    private ArrayList<String[]> csvData = new ArrayList<>();
    private ArrayList<Data> allData = new ArrayList<>();
    private int numLists = 0;


    private String[] topCSVRow = {"SEMESTER", "SUBJECT", "CATALOGNUMBER",
                                  "LOCATION", "ENROLMENTCAPACITY", "ENROLMENTTOTAL",
                                  "INSTRUCTORS", "COMPONENTCODE"};

    @GetMapping("/dump-model")
    public void dumpModel()
    {
        retrieveCSVData();
        populateDataModel();

        ArrayList<Data>[] organizeClasses = new ArrayList[numLists];
        buildArrayList(organizeClasses);
        ArrayList<Data>[] allSortedClasses = sorter.sortDataByClassName(organizeClasses, allData);
        display.printDump(allSortedClasses);
//        display.displayClassData(allSortedClasses);
    }

    private void retrieveCSVData()
    {
        String CSVFile = "data/course_data_2016.csv";
        String splitCSVBy = ",";
        String currentLine;

        // open csv file
        try (BufferedReader br = new BufferedReader(new FileReader(CSVFile)))
        {
            while ((currentLine = br.readLine()) != null)
            {
                // grab all entries on each line, add to ArrayList
                String[] line = currentLine.split(splitCSVBy);
                csvData.add(line);
            }
        }
        // file unable to open
        catch (IOException e)
        {
            exit(0);
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

    private void buildArrayList(ArrayList<Data>[] organizeByClass)
    {
        for (int i = 0; i < numLists; i++)
        {
            organizeByClass[i] = new ArrayList<>();
        }
    }
}
