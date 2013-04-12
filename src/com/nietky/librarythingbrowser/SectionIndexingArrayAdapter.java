package com.nietky.librarythingbrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

class SectionIndexingArrayAdapter<T> extends ArrayAdapter<T> implements SectionIndexer {
    List<T> objects;
    
    HashMap<String, Integer> sectionsMap = new HashMap<String, Integer>();
    ArrayList<String> sectionsList = new ArrayList<String>();
    
    ArrayList<Integer> sectionForPosition = new ArrayList<Integer>();
    ArrayList<Integer> positionForSection = new ArrayList<Integer>();

    public SectionIndexingArrayAdapter(Context context, int textViewResourceId, List<T> objects) {
        super(context, textViewResourceId, objects);
        
        for (int i = 0; i < objects.size(); i++) {
            String objectString = objects.get(i).toString();
            if (objectString.length() > 0) {
                String firstLetter = objectString.substring(0, 1).toUpperCase();
                if (!sectionsMap.containsKey(firstLetter)) {
                    sectionsMap.put(firstLetter, sectionsMap.size());
                    sectionsList.add(firstLetter);
                }
            }
        }
        for (int i = 0; i < objects.size(); i++ ) {
            String objectString = objects.get(i).toString();
            if (objectString.length() > 0) {
                String firstLetter = objectString.substring(0, 1).toUpperCase();
                if (sectionsMap.containsKey(firstLetter)) {
                    sectionForPosition.add(sectionsMap.get(firstLetter));
                } else
                    sectionForPosition.add(0);
            } else 
                sectionForPosition.add(0);
        }
        for (int i = 0; i < sectionsMap.size(); i++)
            positionForSection.add(-1);
        for (int i = 0; i < sectionsMap.size(); i++) {
            for (int j = 0; j < objects.size(); j++) {
                Integer section = sectionForPosition.get(j);
                if (section == i) {
                    positionForSection.set(i, j);
                    break;
                }
            }
        }
        if (positionForSection.get(0) == -1)
            positionForSection.set(0, 0);
        for (int i = 1; i < sectionsMap.size(); i++)
            if (positionForSection.get(i) == -1)
                positionForSection.set(i, positionForSection.get(i - 1));
    }
    public int getPositionForSection(int section) {
        return positionForSection.get(section);
    }

    public int getSectionForPosition(int position) {
        return sectionForPosition.get(position);
    }

    public Object[] getSections() {
        return sectionsList.toArray();
    }
}