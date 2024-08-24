package joltconnlib;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;

import javax.swing.filechooser.FileFilter;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class LocalStorage {
    
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
    private final Configuration configuration;
    private final MetaDataObject metaDataObject;

    public LocalStorage(Configuration configuration) {
        this.configuration = configuration;
        this.metaDataObject = buildMetaObject();
    }

    private MetaDataObject buildMetaObject() {
        final File workdir = configuration.getPath();
        String[] filters = new String[0];
        MetaDataObject metaObject = new MetaDataObject();

        /* read filters if present */
        File filterFile = new File(workdir, MetaDataObject.FILTER_FILE);
        if (filterFile.isFile()) {
            String filterText = null;
            try {
                filterText = Files.readString(filterFile.toPath());
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
        ArrayList<JoltEntry> entries = calculateHashes(files);
        if (entries == null) {
            return null;
        }

        /* check if metaobject in directory is equal with calculated; if not - save it */
        return metaObject;
        //return jsonifyFiles(entries);
    }

    public ArrayList<JoltEntry> calculateHashes(File[] scannedFiles) {
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            MessageDigest cryptSum = MessageDigest.getInstance("SHA-1");
            cryptSum.reset();
            ArrayList<JoltEntry> entries = new ArrayList<JoltEntry>();
            for (File file: scannedFiles) {
                try{
                    byte[] fileData = Files.readAllBytes(file.toPath());
                    crypt.reset();
                    crypt.update(fileData);
                    byte[] fileDigest = crypt.digest();
                    entries.add(new JoltEntry(fileDigest, file.getName()));
                    cryptSum.update(fileDigest);
                } catch(IOException e) {
                    System.out.println("File corrupted - "+file.getName());
                    e.printStackTrace();
                }
            }
            entries.add(new JoltEntry(cryptSum.digest(), "__metadata"));
            return entries;
        }
        catch(NoSuchAlgorithmException e)
        {
            System.out.println("No SHA-1 on this machine");
            e.printStackTrace();
        }
        return null;
    }

    public String jsonifyFiles(ArrayList<JoltEntry> files) {
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        for (JoltEntry entry: files) {
            String key = entry.key;
            String sha = HexFormat.of().formatHex(entry.sha);
            JsonValue element = new JsonValue(sha);
            root.addChild(key, element);
        }
        return root.toJson(OutputType.json);
    }
}
