package joltconnlib;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import joltconnlib.backend.IJson;
import joltconnlib.backend.ISync;
import joltconnlib.metaObject.JoltEntry;
import joltconnlib.metaObject.MetaDataObject;

public class LocalStorage {
    
    private final Configuration configuration;
    private final MetaDataObject metaDataObject;
    private final ISync sync;

    private class Filter implements FilenameFilter {
        private final String[] filters;

        Filter(String[] filters) {
            this.filters = filters;
        }

        @Override
        public boolean accept(File dir, String name) {
            int index = Arrays.binarySearch(filters, name);
            return (!name.startsWith("__")) && (index < 0);
        }
        
    }

    public LocalStorage(Configuration configuration, ISync sync) {
        this.configuration = configuration;
        this.sync = sync;
        this.metaDataObject = buildMetaObject();

    }

    private MetaDataObject buildMetaObject() {
        final File workdir = new File(configuration.getPath());
        String[] filters = new String[0];
        MetaDataObject metaObject = new MetaDataObject();

        
        /* read filters if present */
        File filterFile = new File(workdir, MetaDataObject.FILTER_FILE);
        if (filterFile.isFile()) {
            String filterText = null;
            try {
                filterText = Files.readString(filterFile.toPath()).strip();
            } catch (IOException e) {
                ;
            }
            if (filterText != null) {
                filters = filterText.split("(\\s|\\n)+");
                Arrays.sort(filters);
                metaObject.addAllFilters(filters);
            }
        }

        File[] files = workdir.listFiles(new Filter(filters));

        if (files == null || files.length == 0) {
            return null;
        }

        /* important to sort now, as metahash rely on file order */
        Arrays.sort(files, Comparator.comparing(File::getName));
        
        // build the rest of meta object params
        calculateHashes(files, metaObject);

        /* TODO check if metaobject in directory is equal with calculated; if not - save it */
        return metaObject;
    }

    private void calculateHashes(File[] scannedFiles, MetaDataObject metaObject) {
        ArrayList<JoltEntry> entries = new ArrayList<JoltEntry>();
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            MessageDigest cryptSum = MessageDigest.getInstance("SHA-1");
            cryptSum.reset();
            for (File file: scannedFiles) {
                try{
                    byte[] fileData = Files.readAllBytes(file.toPath());
                    crypt.reset();
                    crypt.update(fileData);
                    byte[] fileDigest = crypt.digest();
                    entries.add(new JoltEntry(HexFormat.of().formatHex(fileDigest), file.getName()));
                    cryptSum.update(fileDigest);
                } catch(IOException e) {
                    System.out.println("File corrupted - "+file.getName());
                    e.printStackTrace();
                }
            }
            metaObject.addAllEntries(entries);
            metaObject.setMetaHash(HexFormat.of().formatHex(cryptSum.digest()));
        } catch(NoSuchAlgorithmException e) {
            System.out.println("No SHA-1 on this machine");
            e.printStackTrace();
        }
    }

    public void writeMetaFilesToDisk() {
        Path filterPath = new File(configuration.getPath(), MetaDataObject.FILTER_FILE).toPath();
        try {
            Files.writeString(filterPath, String.join("\n", metaDataObject.getFilters()), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Filter file was not written");
        }

        Path metaHashPath = new File(configuration.getPath(), MetaDataObject.METAHASH_FILE).toPath();
        try {
            Files.writeString(metaHashPath, String.join("\n", metaDataObject.getMetaHash()), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("MetaHash file was not written");
        }

        Path metaHashData = new File(configuration.getPath(), MetaDataObject.METADATA_FILE).toPath();
        try {
            String entries = jsonifyEntries(metaDataObject.getEntries());
            Files.writeString(metaHashData, entries, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("MetaData file was not written");
        }
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

    public void PushFolderToJolt() {
        
    }
}
