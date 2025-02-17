package com.github.retmode.connectorbackend.javasync;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import com.github.retmode.connectorbase.backend.ISync;
import com.github.retmode.connectorbase.backend.Containers.JoltArrayOfStringsContainer;
import com.github.retmode.connectorbase.backend.Containers.JoltStringContainer;
import com.github.retmode.connectorbase.backend.IConfiguration;
import com.github.retmode.connectorbase.metaobject.JoltEntry;
import com.github.retmode.connectorbase.metaobject.MetaDataObject;

public class JavaLocalSync implements ISync {

    private class Filter implements FilenameFilter {
        // private final String[] filters;

        // Filter(String[] filters) {
        //     this.filters = filters;
        // }

        @Override
        public boolean accept(File dir, String name) {
            //int index = Arrays.binarySearch(filters, name);
            return (!name.startsWith("__"));// && (index < 0);
        }
        
    }

    private final MessageDigest urlSha;

    public JavaLocalSync(MessageDigest urlSha ) {
        this.urlSha = urlSha;
    }

    public void getEntry(String key, IConfiguration configuration, JoltStringContainer result) {

        File workdir = new File(configuration.getPath());
        if (!workdir.isDirectory()) {
            return;
        }
        File file = new File(workdir, key);
        if (file.isFile()) { 
            try {
                result.value = Files.readString(file.toPath());
            } catch (IOException e) {
                ;
            }
        }
    }
    
    public MessageDigest getSha() {
        return urlSha;
    }
    
    public void getAllEntries(IConfiguration configuration, JoltArrayOfStringsContainer result) {

        File workdir = new File(configuration.getPath());
        if (!workdir.isDirectory()) {
            return;
        }

        File[] files = workdir.listFiles(new Filter());

        if (files == null || files.length == 0) {
            return;
        }

        /* important to sort now, as metahash rely on file order */
        Arrays.sort(files, Comparator.comparing(File::getName));
        result.value = new ArrayList<String>();
        for (File f:files) {
            result.value.add(f.getName());
        }
    }

    public void writeEntry(IConfiguration configuration, String key, String data) {
        Path filePath = new File(configuration.getPath(), key).toPath();
        try {
            Files.writeString(filePath, data, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Filter file was not written");
        }
    }

    public void removeEntry(IConfiguration configuration, String key) {
        if (key.contains("..")) {
            System.out.println("Double dots in key, ignoring...");
            return;
        }
        Path filePath = new File(configuration.getPath(), key).toPath();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.out.println("File was not removed: " + filePath);
        }
    }

    public void writeMetaFiles(IConfiguration configuration, MetaDataObject metaDataObject) {
        writeEntry(configuration, MetaDataObject.FILTER_FILE, String.join("\n", metaDataObject.getFilters()));
        writeEntry(configuration, MetaDataObject.METAHASH_FILE, String.join("\n", metaDataObject.getMetaHash()));
        writeEntry(configuration, MetaDataObject.METADATA_FILE, jsonifyEntries(metaDataObject.getEntries()));
        
    }

    private String jsonifyEntries(JoltEntry[] files) {
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        for (JoltEntry entry: files) {
            String key = entry.key;
            String sha = (entry.sha);
            JsonValue element = new JsonValue(sha);
            root.addChild(key, element);
        }
        return root.toJson(OutputType.json);
    }
}
