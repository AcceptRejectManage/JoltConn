package com.github.retmode.connectorbase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;

import com.github.retmode.connectorbase.backend.Containers;
import com.github.retmode.connectorbase.backend.ISync;
import com.github.retmode.connectorbase.backend.Containers.JoltArrayOfStringsContainer;
import com.github.retmode.connectorbase.backend.Containers.JoltStringContainer;
import com.github.retmode.connectorbase.backend.IConfiguration;
import com.github.retmode.connectorbase.metaobject.JoltEntry;
import com.github.retmode.connectorbase.metaobject.MetaDataObject;

public class Storage {
    
    private final ISync netSync;
    private final ISync localSync;
    private final IConfiguration configuration;
    private final MetaDataObject metaDataObject;

    public enum Target {NET, LOCAL};

    private ISync getSync(Target target) {
        switch(target) {
            case NET:
            return netSync;
            case LOCAL:
            return localSync;
            default:
            return null; //?
        }
    }
    public Storage(IConfiguration configuration, ISync netSync, ISync localSync) {

        this.configuration = configuration;
        this.netSync = netSync;
        this.localSync = localSync;
        this.metaDataObject = new MetaDataObject();

    }
    public void updateNetMetaHash() {
        updateMetaHash(Target.NET);
    }

    public void updateLocalMetaHash() {
        updateMetaHash(Target.LOCAL);
    }
    

    public void updateMetaHash(Target target) {
        ISync sync = getSync(target);
        Containers.JoltStringContainer jsc = new Containers.JoltStringContainer();
        sync.getEntry(MetaDataObject.METAHASH_FILE, 
            configuration, jsc);
        if (jsc.isValid()) {
            metaDataObject.setMetaHash(jsc.value);
        } else {
            // rebuild
        }

    }

    public void updateNetFilter() {
        updateFilter(Target.NET);
    }

    public void updateLocalFilter() {
        updateFilter(Target.LOCAL);
    }


    public void updateFilter(Target target) {
        ISync sync = getSync(target);
        Containers.JoltStringContainer jsc = new Containers.JoltStringContainer();
        sync.getEntry(MetaDataObject.FILTER_FILE, 
            configuration,
            jsc);
        if (jsc.isValid()) {
            String[] filters = jsc.value.split("(\\s|\\n)+");
            Arrays.sort(filters);
            metaDataObject.addAllFilters(filters);
        } else {

        }
    }

    public void updateNetHashes() {
        updateHashes(Target.NET);
    }

    public void updateLocalHashes() {
        updateHashes(Target.LOCAL);
    }

    public void updateHashes(Target target) {
        ISync sync = getSync(target);
        String metaHash = null;
        ArrayList<JoltEntry> entries = new ArrayList<>();
        Containers.JoltArrayOfStringsContainer jasc = new Containers.JoltArrayOfStringsContainer();
        sync.getAllEntries(configuration, jasc);
        if (jasc.isValid()) {
            Containers.JoltStringContainer jsc = new Containers.JoltStringContainer();
            try {
                MessageDigest crypt = MessageDigest.getInstance("SHA-1");
                MessageDigest cryptSum = MessageDigest.getInstance("SHA-1");
                cryptSum.reset();
                String[] filters = metaDataObject.getFilters();
                
                for (String key: jasc.value) {
                    if (Arrays.binarySearch(filters, key) >= 0) {
                        continue;
                    }
                    sync.getEntry(key, configuration, jsc);
                    byte[] fileData = jsc.value.getBytes(StandardCharsets.UTF_8);//Files.readAllBytes();
                    crypt.reset();
                    crypt.update(fileData);
                    byte[] fileDigest = crypt.digest();
                    entries.add(new JoltEntry(HexFormat.of().formatHex(fileDigest), key));
                    cryptSum.update(fileDigest);
                    
                }
                metaHash = HexFormat.of().formatHex(cryptSum.digest());
            } catch (NoSuchAlgorithmException e) {
                ;
            }
        }
        metaDataObject.addAllEntries(entries);
        metaDataObject.setMetaHash(metaHash);

    }

    public void storeFilesLocally() {
        storeFiles(Target.NET, Target.LOCAL);
    }

    public void storeFilesNet() {
        storeFiles(Target.LOCAL, Target.NET);
    }

    public void storeFiles(Target targetFrom, Target targetTo) {
        ISync from = getSync(targetFrom);
        ISync to = getSync(targetTo);
        if (from == null || to == null) {
            System.out.println("Unknown target");
            return;
        }
        JoltArrayOfStringsContainer jaos = new JoltArrayOfStringsContainer();
        from.getAllEntries(configuration, jaos);
        if (jaos.isValid()) {
            JoltStringContainer jsc = new JoltStringContainer();
            for (String entry :jaos.value) {
                jsc.clear();
                from.getEntry(entry, configuration, jsc);
                if (jsc.isValid()) {
                    to.writeEntry(configuration, entry, jsc.value);
                }
            }
        }
    }

    public void removeAllFilesLocally() {
        removeAllFiles(Target.LOCAL);
    }

    public void removeAllFilesNet() {
        removeAllFiles(Target.NET);
    }

    public void removeAllFiles(Target target) {
        ISync from = getSync(target);
        if (from == null) {
            System.out.println("Unknown target");
            return;
        }
        JoltArrayOfStringsContainer jaos = new JoltArrayOfStringsContainer();
        from.getAllEntries(configuration, jaos);
        if (jaos.isValid()) {
            JoltStringContainer jsc = new JoltStringContainer();
            for (String entry :jaos.value) {
                jsc.clear();
                from.removeEntry(configuration, entry);
            }
        }
    }
}
