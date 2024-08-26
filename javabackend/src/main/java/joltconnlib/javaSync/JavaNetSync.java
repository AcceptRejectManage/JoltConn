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
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonStructure;

import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreFetchRequest;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreGetKeysRequest;
import com.github.raeleus.gamejoltapi.GameJoltRequest;


import joltconnlib.backend.ISync;

public class JavaNetSync implements ISync{
    
    private static final String GameJoltSite = "https://api.gamejolt.com/api/game/";
    private static final String GameJoltVersion = "v1_2";

    private final MessageDigest urlSha;

    public JavaNetSync(MessageDigest urlSha ) {
        this.urlSha = urlSha;
    }

    public String getEntry(String key, String GameID, String GameKey) {

        String keyData = null;
        String url = GetRawUrl(DataStoreFetchRequest.builder().gameID(GameID).key(key).build());
        String keyJson = performRequest(url, GameKey );

        JsonReader reader = Json.createReader(new StringReader(keyJson));
            
        JsonStructure js = reader.read();
        JsonObject response = js.asJsonObject().getJsonObject("response");
        JsonString result = response.getJsonString("success");

        if (!result.getString().equals("true")){
            return null;
        }

        if (!response.containsKey("data")) {
            return null;
        }

        keyData = response.getString("data");


        return keyData;
    
    }
    
    public MessageDigest getSha() {
        return urlSha;
    }
    
    public List<String> getAllEntries(String GameID, String GameKey) {
        List<String> webKeys = new ArrayList<String>();

        String rawUrl = GetRawUrl(DataStoreGetKeysRequest.builder().gameID(GameID).build());
        if (rawUrl.length() > 0) {
            String keysRequestAsJsonStr = performRequest(rawUrl, GameKey);
            JsonReader reader = Json.createReader(new StringReader(keysRequestAsJsonStr));
            
            JsonStructure js = reader.read();
            JsonObject response = js.asJsonObject().getJsonObject("response");
            JsonString result = response.getJsonString("success");
            JsonArray keys = response.getJsonArray("keys");
            List<JsonObject> keysView = keys.getValuesAs(JsonObject.class);
            for (JsonObject k: keysView) {
                webKeys.add(k.getString("key"));
            }
        }
        return webKeys;
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

}
