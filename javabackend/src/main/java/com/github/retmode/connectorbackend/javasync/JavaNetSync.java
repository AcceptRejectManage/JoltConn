package com.github.retmode.connectorbackend.javasync;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;

import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreFetchRequest;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreGetKeysRequest;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreSetRequest;
import com.github.raeleus.gamejoltapi.GameJoltRequest;

import com.github.retmode.connectorbase.backend.Containers.JoltArrayOfStringsContainer;
import com.github.retmode.connectorbase.backend.Containers.JoltStringContainer;
import com.github.retmode.connectorbase.backend.ISync;
import com.github.retmode.connectorbase.backend.IConfiguration;
import com.github.retmode.connectorbase.metaobject.JoltEntry;
import com.github.retmode.connectorbase.metaobject.MetaDataObject;

public class JavaNetSync implements ISync {
    
    private static final String GameJoltSite = "https://api.gamejolt.com/api/game/";
    private static final String GameJoltVersion = "v1_2";
    private static final String GameJoltQuery = "/data-store/";

    private final MessageDigest urlSha;

    public JavaNetSync(MessageDigest urlSha ) {
        this.urlSha = urlSha;
    }

    public void getEntry(String key, IConfiguration configuration, JoltStringContainer result) {

        String url = GetRawEntryUrl(configuration.getID(), key);
        
        String keyJson = performRequest(url, configuration.getKey());

        JsonReader reader = Json.createReader(new StringReader(keyJson));
            
        JsonStructure js = reader.read();
        JsonObject response = js.asJsonObject().getJsonObject("response");
        JsonString responseStr = response.getJsonString("success");

        if (!responseStr.getString().equals("true")){
            return ;
        }

        if (!response.containsKey("data")) {
            return ;
        }

        result.value =  response.getString("data");
    
    }
    
    public MessageDigest getSha() {
        return urlSha;
    }
    
    public void getAllEntries(IConfiguration configuration, JoltArrayOfStringsContainer result) {

        String rawUrl = GetRawEntriesUrl(configuration.getID());

        if (rawUrl.length() > 0) {
            String keysRequestAsJsonStr = performRequest(rawUrl, configuration.getKey());
            JsonReader reader = Json.createReader(new StringReader(keysRequestAsJsonStr));
            
            JsonStructure js = reader.read();
            JsonObject response = js.asJsonObject().getJsonObject("response");
            JsonString success = response.getJsonString("success");
            // TODO check if response was success
            JsonArray keys = response.getJsonArray("keys");
            List<JsonObject> keysView = keys.getValuesAs(JsonObject.class);
            
            if (keysView.size() > 0) {
                result.value = new ArrayList<String>();
                for (JsonObject k: keysView) {
                    result.value.add(k.getString("key"));
                }
            }
        }
    }

    private String GetRawEntryUrl(String gameID, String gameKey) {
        return GameJoltSite + GameJoltVersion + GameJoltQuery + "?game_id=" + gameID + "&key=" + gameKey;
    }

    private String GetRawEntriesUrl(String gameID) {
        return GameJoltSite + GameJoltVersion + GameJoltQuery + "get-keys/?game_id=" + gameID;
    }

    private String SetRawDataUrl(String gameID, String gameKey, String data) {
        return GameJoltSite + GameJoltVersion + GameJoltQuery + "set/?game_id=" + gameID + "&key=" + gameKey + "&data=" + data;
    }
    
    private String RemoveRawDataUrl(String gameID, String gameKey) {
        return GameJoltSite + GameJoltVersion + GameJoltQuery + "remove/?game_id=" + gameID + "&key=" + gameKey;
    }

    private String performRequest(String path, String GameKey) {
        
        try{    
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

    public void writeEntry(IConfiguration configuration, String key, String data){
        String rawUrl = SetRawDataUrl(configuration.getID(), key, data);

        if (rawUrl.length() > 0) {
            String keysRequestAsJsonStr = performRequest(rawUrl, configuration.getKey());
            JsonReader reader = Json.createReader(new StringReader(keysRequestAsJsonStr));
            
            JsonStructure js = reader.read();
            JsonObject response = js.asJsonObject().getJsonObject("response");
            JsonString success = response.getJsonString("success");
            // TODO check if response was success
        }
    }

    public void removeEntry(IConfiguration configuration, String key) {
        String rawUrl = RemoveRawDataUrl(configuration.getID(), key);

        if (rawUrl.length() > 0) {
            String keysRequestAsJsonStr = performRequest(rawUrl, configuration.getKey());
            JsonReader reader = Json.createReader(new StringReader(keysRequestAsJsonStr));
            
            JsonStructure js = reader.read();
            JsonObject response = js.asJsonObject().getJsonObject("response");
            JsonString success = response.getJsonString("success");
            // TODO check if response was success
        }
    }

    public void writeMetaFiles(IConfiguration configuration, MetaDataObject metaDataObject) {
        writeEntry(configuration, MetaDataObject.FILTER_FILE, String.join("\n", metaDataObject.getFilters()));
        writeEntry(configuration, MetaDataObject.METAHASH_FILE, String.join("\n", metaDataObject.getMetaHash()));
        writeEntry(configuration, MetaDataObject.METADATA_FILE, jsonifyEntries(metaDataObject.getEntries()));
    }

    private String jsonifyEntries(JoltEntry[] files) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (JoltEntry entry: files) {
            builder.add(entry.key, entry.sha);
        }
        JsonObject jo = builder.build();
        return jo.toString();
    }
}
