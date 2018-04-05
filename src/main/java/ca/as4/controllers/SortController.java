package ca.as4.controllers;

import ca.as4.models.Data;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// todo this class might have to be moved into a model in an effort to keep logic away from controllers???
public class SortController {
    private int numClassesInList = 0;

    public SortController() { }

    public ArrayList<ArrayList<Data>> sortDataByClassName(ArrayList<ArrayList<Data>> organizeClasses, ArrayList<Data> allData)
    {
        int counter = 0;
        for (int i = 0; i < allData.size(); i++)
        {
            boolean inOrganizedClass = checkInOrganizedClass(allData.get(i), organizeClasses);
            boolean isComponentCodeAClass = (!allData.get(i).getComponent().equals("TUT")
                    || !allData.get(i).getComponent().equals("LAB")
                    || !allData.get(i).getComponent().equals("OPL")
                    || !allData.get(i).getComponent().equals("WKS"));

            if (!inOrganizedClass && isComponentCodeAClass)
            {
                ArrayList<Data> currentDataArr = new ArrayList<>();
                currentDataArr.add(allData.get(i));
                organizeClasses.add(currentDataArr);
                findAndInsertRelatedData(allData, organizeClasses, allData.get(i), counter);
                counter++;
            }
        }

        numClassesInList = counter;

        sortEachArrayList(organizeClasses);
        return organizeClasses;
    }


    private boolean checkInOrganizedClass(Data checkData, ArrayList<ArrayList<Data>> organizeClasses)
    {
        for (int i = 0; i < organizeClasses.size(); i++)
        {
            if (organizeClasses.get(i).contains(checkData))
            {
                return true;
            }
        }
        return false;
    }

    private void sortEachArrayList(ArrayList<ArrayList<Data>> organizeClasses)
    {
        for (int i = 0; i < organizeClasses.size(); i++)
        {
            Collections.sort(organizeClasses.get(i), new Comparator<Data>() {
                @Override
                public int compare(Data o1, Data o2) {
                    return o1.getSemester() - o2.getSemester();
                }
            });
        }

        Collections.sort(organizeClasses, new Comparator<ArrayList<Data>>() {
            @Override
            public int compare(ArrayList<Data> o1, ArrayList<Data> o2) {
                String o1Class = o1.get(0).getSubjectName() + " " + o1.get(0).getCatalogNumber();
                String o2Class = o2.get(0).getSubjectName() + " " + o2.get(0).getCatalogNumber();
                return o1Class.compareTo(o2Class);
            }
        });
    }

    private void findAndInsertRelatedData(ArrayList<Data> allData, ArrayList<ArrayList<Data>> organizeClasses, Data compareData, int counter)
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
                        if (currentFile.getComponent().equals("LEC"))
                        {
                            indexLecOfOtherCampus = numClassesInOtherCampus;
                        }

                        otherCampus.add(currentFile);
                        numClassesInOtherCampus++;
                    }
                    else
                    {
                        organizeClasses.get(counter).add(currentFile);
                    }
                }
            }
        }

        if (otherCampusDataPresent)
        {
            organizeClasses.get(counter).add(otherCampus.get(indexLecOfOtherCampus));
            for (int i = 0; i < otherCampus.size(); i++)
            {
                if (i != indexLecOfOtherCampus)
                {
                    organizeClasses.get(counter).add(otherCampus.get(i));
                }
            }
        }
    }

    private boolean checkClassRelated(Data currentFile, Data compareData)
    {
        boolean checkSubjectSame = compareData.getSubjectName().equals(currentFile.getSubjectName());
        boolean checkCatalogSame = compareData.getCatalogNumber().equals(currentFile.getCatalogNumber());

        if (checkSubjectSame && checkCatalogSame)
        {
            return true;
        }
        return false;
    }
}
