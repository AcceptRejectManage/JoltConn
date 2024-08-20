package joltconnlib;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class FolderState {
    
    Path folderPath;
    
    public String parseDirectory(File path) {
        if (!setPath(path)) {
            return "";
        }

        File[] files = scanFolder();
        if (files == null) {
            return "";
        }

        ArrayList<JoltEntry> entries = calculateHashes(files);
        if (entries == null) {
            return "";
        }

        return jsonifyFiles(entries);
    }

    public boolean setPath(File file) {
        if (!file.isDirectory()) {
            folderPath = null;
            return false;
        }
        folderPath = file.toPath();
        return true;
    }

    public File[] scanFolder() {
        File[] result = folderPath.toFile().listFiles(file -> file.isFile());
        Arrays.sort(result, Comparator.comparing(File::getName));
        return result;
    }


    public ArrayList<JoltEntry> calculateHashes(File[] scannedFiles) {
        if (scannedFiles.length == 0) {
            return null;
        }
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
