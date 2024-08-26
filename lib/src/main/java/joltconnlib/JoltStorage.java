/*
 * This source file was generated by the Gradle 'init' task
 */
package joltconnlib;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.github.raeleus.gamejoltapi.GameJoltRequest;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreFetchRequest;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreGetKeysRequest;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreRemoveRequest;

import joltconnlib.backend.IJson;
import joltconnlib.backend.ISync;
import joltconnlib.metaObject.JoltEntry;
import joltconnlib.metaObject.MetaDataObject;

public class JoltStorage {
    
    private static final String GameJoltSite = "https://api.gamejolt.com/api/game/";
    private static final String GameJoltVersion = "v1_2";
    private final MessageDigest urlSha;
    private final Configuration configuration;
    private final MetaDataObject metaDataObject;
    private final ISync sync;

    public JoltStorage(Configuration configuration, ISync sync) {
        MessageDigest temp;
        try{
            temp = MessageDigest.getInstance("SHA-1");
        } catch (Exception e) {
            temp = null;
            e.printStackTrace();
        }
        urlSha = temp;
        this.configuration = configuration;
        this.sync = sync;
        this.metaDataObject = buildMetaObject();

    }

    public JoltStorage(Configuration configuration, MetaDataObject folderObject, ISync sync) {
        MessageDigest temp;
        MetaDataObject tempObject;
        try{
            temp = MessageDigest.getInstance("SHA-1");
        } catch (Exception e) {
            temp = null;
            e.printStackTrace();
        }
        urlSha = temp;
        this.configuration = configuration;


        tempObject = buildMetaObject();
        this.metaDataObject = tempObject;
        this.sync = sync;
    }

    // private boolean isSynced(MetaDataObject folderObject) {
    //     String metaHash = getKey(MetaDataObject.METAHASH_FILE);
    //     if (metaHash != null && folderObject.otherMetaHashEqual(metaHash)) {
    //         return true;
    //     }

    //     String metaData = getKey(MetaDataObject.METADATA_FILE);



    //     return false;
    // }

    private MetaDataObject buildMetaObject() {
        final MetaDataObject metaObject = new MetaDataObject();
        
        // get all keys
        List<String> keys = getWebKeys();




        // try downloading metadata
        int metaDataIndex = keys.indexOf(MetaDataObject.METADATA_FILE);
        if (metaDataIndex >= 0) {
            String metaData = getKey(MetaDataObject.METADATA_FILE);
            keys.remove(metaDataIndex);
        }

        // try downloading filter
        int filterIndex = keys.indexOf(MetaDataObject.FILTER_FILE);
        if (filterIndex >= 0) {
            String filters = getKey(MetaDataObject.FILTER_FILE);
            keys.remove(filterIndex);
        }

        // try downloading metahash
        int metaHashIndex = keys.indexOf(MetaDataObject.METAHASH_FILE);
        if (metaHashIndex >= 0) {
            String metaHash = getKey(MetaDataObject.METAHASH_FILE);
            keys.remove(metaHashIndex);
            
        }

        if (keys.removeIf((key) -> key.startsWith("__"))) {
            metaObject.invalidate();
        }

        /* here we should check directory hash and other stuff; if meta object is invalid, just clear 
         *  keys redundant keys, check present keys and upload whats needed
         */
        calculateHashes(keys, metaObject);

        return metaObject;
    }

    private void calculateHashes(List<String> scannedFiles, MetaDataObject metaObject) {
        ArrayList<JoltEntry> entries = new ArrayList<JoltEntry>();
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            MessageDigest cryptSum = MessageDigest.getInstance("SHA-1");
            cryptSum.reset();
            for(String key: scannedFiles) {
                byte[] fileData = getKey(key).getBytes(StandardCharsets.UTF_8);
                crypt.reset();
                crypt.update(fileData);
                byte[] fileDigest = crypt.digest();
                entries.add(new JoltEntry(HexFormat.of().formatHex(fileDigest), key));
                cryptSum.update(fileDigest);
            }
            metaObject.addAllEntries(entries);
            metaObject.setMetaHash(HexFormat.of().formatHex(cryptSum.digest()));
        } catch(NoSuchAlgorithmException e) {
            System.out.println("No SHA-1 on this machine");
            e.printStackTrace();
        }
    }

    private String GetRawUrl(GameJoltRequest r) {
        return GameJoltSite + GameJoltVersion + r.toString();
    }

    private String performRequest(String path) {
        
        try{    
            String GameKey = configuration.getKey();
            String signature = HexFormat.of().formatHex(urlSha.digest((path+GameKey).getBytes()));
            URI uri = new URI(path + "&signature=" + signature);
            HttpRequest r = HttpRequest.newBuilder().uri(uri).GET().build();
            BodyHandler<String> bh = HttpResponse.BodyHandlers.ofString();
            HttpResponse<String> response = HttpClient.newHttpClient().send(r, bh);
            return response.body();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private List<String> getWebKeys() {

        String GameID = configuration.getID();
        List<String> webKeys = new ArrayList<String>();

        String rawUrl = GetRawUrl(DataStoreGetKeysRequest.builder().gameID(GameID).build());
        if (rawUrl.length() > 0) {
            String keysRequestAsJsonStr = performRequest(rawUrl);
            JsonReader reader = new JsonReader();
            JsonValue json = reader.parse(keysRequestAsJsonStr);
            JsonValue keysArray = json.get("response").get("keys");

            if (keysArray != null && keysArray.child() != null) {
                for (JsonValue key = keysArray.child; key != null; key = key.next) {
                    String keyLabel = key.getString("key");
                    webKeys.add(keyLabel);
                }
            }
        }
        return webKeys;
    }

    // private boolean deleteKey(String key) {

    //     String GameID = configuration.getID();
    //     String keyData = null;
    //     String keyJson = performRequest( GetRawUrl(DataStoreRemoveRequest.builder().gameID(GameID).key(key).build()));
    //     JsonReader reader = new JsonReader();

    //     JsonValue result = reader.parse(keyJson);
    //     if (result == null) {
    //         return false;
    //     }
    //     JsonValue keyResult = result.get("response");
    //     if (keyResult == null){
    //         return false;
    //     } 

    //     keyData = keyResult.getString("success");

    //     if (keyData == null || !keyData.equals("true")) {
    //         return false;
    //     }

    //     return true;
    // }

    private String getKey(String key) {

        String GameID = configuration.getID();
        String keyData = null;
        String keyJson = performRequest( GetRawUrl(DataStoreFetchRequest.builder().gameID(GameID).key(key).build()));
        JsonReader reader = new JsonReader();

        JsonValue result = reader.parse(keyJson);
        if (result == null) {
            return keyData;
        }
        JsonValue keyResult = result.get("response");
        if (keyResult == null){
            return keyData;
        } 

        keyData = keyResult.getString("data");

        if (keyData == null) {
            return keyData;
        }

        return keyData;

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

    public void writeFilesToDisk() {
        JoltEntry[] entries = metaDataObject.getEntries();
        for (JoltEntry entry: entries) {
            String entryStringified = getKey(entry.key);
            if (entryStringified != null) {
                try {
                    Path filePath = new File(configuration.getPath(), entry.key).toPath();
                    Files.writeString(filePath, entryStringified, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    System.out.println("File " + entry.key + " was not written");
                }
            } else {
                System.out.println("File " + entry.key + " was not present in the data store but was preset on metadata");
            }
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
}
