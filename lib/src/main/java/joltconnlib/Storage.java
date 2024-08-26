package joltconnlib;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;

import joltconnlib.backend.Containers;
import joltconnlib.backend.ISync;
import joltconnlib.backend.Containers.JoltArrayOfStringsContainer;
import joltconnlib.backend.Containers.JoltStringContainer;
import joltconnlib.configuration.Configuration;
import joltconnlib.metaObject.JoltEntry;
import joltconnlib.metaObject.MetaDataObject;

public class Storage {
    
    private final ISync netSync;
    private final ISync localSync;
    private final Configuration configuration;
    private final MetaDataObject metaDataObject;

    public Storage(Configuration configuration, ISync netSync, ISync localSync) {

        this.configuration = configuration;
        this.netSync = netSync;
        this.localSync = localSync;
        this.metaDataObject = new MetaDataObject();

    }
    public void updateNetMetaHash() {
        updateMetaHash(netSync);
    }

    public void updateLocalMetaHash() {
        updateMetaHash(localSync);
    }
    

    public void updateMetaHash(ISync sync) {

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
        updateFilter(netSync);
    }

    public void updateLocalFilter() {
        updateFilter(localSync);
    }


    public void updateFilter(ISync sync) {
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
        updateHashes(netSync);
    }

    public void updateLocalHashes() {
        updateHashes(localSync);
    }

    public void updateHashes(ISync sync) {
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
        storeFiles(netSync, localSync);
    }

    public void storeFilesNet() {
        storeFiles(localSync, netSync);
    }

    public void storeFiles(ISync from, ISync to) {
        JoltArrayOfStringsContainer jaos = new JoltArrayOfStringsContainer();
        from.getAllEntries(configuration, jaos);
        if (jaos.isValid()) {
            JoltStringContainer jsc = new JoltStringContainer();
            for (String entry :jaos.value) {
                jsc.clear();
                from.getEntry(entry, configuration, jsc);
                if (jsc.isValid()) {
                    to.writeFile(configuration, entry, jsc.value);
                }
            }
        }
    }
}
