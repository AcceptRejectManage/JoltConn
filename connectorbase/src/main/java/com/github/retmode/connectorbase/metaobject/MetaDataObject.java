package com.github.retmode.connectorbase.metaobject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class MetaDataObject {

    public static String FILTER_FILE = "__filter";
    public static String METAHASH_FILE = "__metahash";
    public static String METADATA_FILE = "__metadata";

    private TreeSet<JoltEntry> sortedFiles;
    private String metaHash;
    private TreeSet<String> sortedFilter;

    private boolean fresh;

    public MetaDataObject() {
        sortedFiles = new TreeSet<JoltEntry>((JoltEntry left, JoltEntry right) -> left.key.compareTo(right.key));
        metaHash = null;
        sortedFilter = new TreeSet<>((String left, String right) -> left.compareTo(right));
        fresh = true;
    }

    public void clearEntries() {
        sortedFiles.clear();
    }

    public void addEntry(JoltEntry entry) {
        sortedFiles.add(entry);
    }

    public void addAllEntries(ArrayList<JoltEntry> entries) {
        sortedFiles.clear();
        sortedFiles.addAll(entries);
    }

    public JoltEntry[] getEntries() {
        return sortedFiles.toArray(new JoltEntry[sortedFiles.size()]);
    }
    
    public void clearFilters() {
        sortedFilter.clear();
    }

    public void addFilter(String path) {
        sortedFilter.add(path);
    }

    public void addAllFilters(String[] paths) {
        sortedFiles.clear();
        sortedFilter.addAll(Arrays.asList(paths));
    }

    public String[] getFilters() {
        return sortedFilter.toArray(new String[sortedFilter.size()]);
    }

    public void setMetaHash(String metaHash) {
        this.metaHash = metaHash;
    }

    public String getMetaHash() {
        return metaHash;
    }

    public boolean otherMetaHashEqual(String otherMetaHash) {
        return otherMetaHash.equals(metaHash);
    }

    public boolean isEmpty() {
        return sortedFiles.size() == 0 || metaHash == null;
    }

    public void invalidate() {
        fresh = false;
    }

    public boolean isFresh() {
        return fresh;
    }

}
