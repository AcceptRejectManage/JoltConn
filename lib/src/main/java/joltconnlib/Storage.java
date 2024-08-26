package joltconnlib;

import com.badlogic.gdx.Files;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;

import joltconnlib.backend.ISync;
import joltconnlib.metaObject.JoltEntry;
import joltconnlib.metaObject.MetaDataObject;

public class Storage {
    
    private final ISync sync;
    private final Configuration configuration;
    private final MetaDataObject metaDataObject;

    public Storage(Configuration configuration, ISync sync) {

        this.configuration = configuration;
        this.sync = sync;
        this.metaDataObject = new MetaDataObject();

    }


    private void calculateHashes(File[] scannedFiles, MetaDataObject metaObject) {
        ArrayList<JoltEntry> entries = new ArrayList<JoltEntry>();

        MessageDigest crypt = sync.getSha();
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

    }

    public void storeMetaFiles() {
    
    }

    public void storeFiles() {

    }

    public void calculateEntryHash() {
        
    }
}
