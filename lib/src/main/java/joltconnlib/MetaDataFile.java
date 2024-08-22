package joltconnlib;

import java.security.MessageDigest;
import java.util.ArrayList;


public class MetaDataFile {
    ArrayList<JoltEntry> files;
    byte[] metahash;
    ArrayList<String> filter;

    MetaDataFile() {
        files = new ArrayList<>();
        filter = new ArrayList<>();
    }

    public void addEntry(JoltEntry entry) {
        files.add(entry);
    }

    public void addFilter(String path) {
        filter.add(path);
    }

    public void calculateMetaHash(MessageDigest sha) {
        //sort ?? should be already sorted

    }
}
