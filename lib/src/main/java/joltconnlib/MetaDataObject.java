package joltconnlib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;


public class MetaDataObject {

    public static String FILTER_FILE = "__filter";
    public static String METAHASH_FILE = "__metahash";
    public static String METADATA_FILE = "__metadata";
    ArrayList<JoltEntry> files;
    TreeSet<JoltEntry> sortedFiles;
    byte[] metaHash;
    ArrayList<String> filter;
    TreeSet<String> sortedFilter;

    MetaDataObject() {
        files = new ArrayList<>();
        sortedFiles = new TreeSet<JoltEntry>((JoltEntry left, JoltEntry right) -> left.key.compareTo(right.key));
        metaHash = null;
        filter = new ArrayList<>();
        sortedFilter = new TreeSet<>((String left, String right) -> left.compareTo(right));
    }

    public void addEntry(JoltEntry entry) {
        //files.add(entry);
        sortedFiles.add(entry);
    }

    public void addFilter(String path) {
        sortedFilter.add(path);
    }

    public void addAllFilters(String[] paths) {
        sortedFiles.clear();
        sortedFilter.addAll(Arrays.asList(paths));
    }

    public void calculateMetaHash(MessageDigest sha) {
        //sort ?? should be already sorted

    }

    public boolean isEmpty() {
        return files.size() == 0 || metaHash == null;
    }
}
