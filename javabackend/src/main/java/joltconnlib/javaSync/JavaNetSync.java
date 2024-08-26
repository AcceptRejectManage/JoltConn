package joltconnlib.javaSync;

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

import joltconnlib.backend.Containers.JoltArrayOfStringsContainer;
import joltconnlib.backend.Containers.JoltStringContainer;
import joltconnlib.backend.ISync;
import joltconnlib.configuration.Configuration;
import joltconnlib.metaObject.JoltEntry;
import joltconnlib.metaObject.MetaDataObject;

public class JavaNetSync implements ISync{
    
    private static final String GameJoltSite = "https://api.gamejolt.com/api/game/";
    private static final String GameJoltVersion = "v1_2";

    private final MessageDigest urlSha;

    public JavaNetSync(MessageDigest urlSha ) {
        this.urlSha = urlSha;
    }

    public void getEntry(String key, Configuration configuration, JoltStringContainer result) {

        String url = GetRawUrl(DataStoreFetchRequest.builder().gameID(configuration.getID()).key(key).build());
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
    
    public void getAllEntries(Configuration configuration, JoltArrayOfStringsContainer result) {

        String rawUrl = GetRawUrl(DataStoreGetKeysRequest.builder().gameID(configuration.getID()).build());
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

    private String GetRawUrl(GameJoltRequest r) {
        return GameJoltSite + GameJoltVersion + r.toString();
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

    public void writeFile(Configuration configuration, String path, String data) {
        String rawUrl = GetRawUrl(DataStoreSetRequest.builder().gameID(configuration.getID()).key(path).data(data).build());
        if (rawUrl.length() > 0) {
            String keysRequestAsJsonStr = performRequest(rawUrl, configuration.getKey());
            JsonReader reader = Json.createReader(new StringReader(keysRequestAsJsonStr));
            
            JsonStructure js = reader.read();
            JsonObject response = js.asJsonObject().getJsonObject("response");
            JsonString success = response.getJsonString("success");
            // TODO check if response was success
        }
    }

    public void writeMetaFiles(Configuration configuration, MetaDataObject metaDataObject) {
        writeFile(configuration, MetaDataObject.FILTER_FILE, String.join("\n", metaDataObject.getFilters()));
        writeFile(configuration, MetaDataObject.METAHASH_FILE, String.join("\n", metaDataObject.getMetaHash()));
        writeFile(configuration, MetaDataObject.METADATA_FILE, jsonifyEntries(metaDataObject.getEntries()));
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
