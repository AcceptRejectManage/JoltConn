package joltconnlib.gdxSync;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreFetchRequest;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreFetchValue;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreGetKeysRequest;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreGetKeysValue;
import com.badlogic.gdx.Gdx;
import com.github.raeleus.gamejoltapi.GameJoltApi;
import com.github.raeleus.gamejoltapi.GameJoltDataStore;

import joltconnlib.backend.ISync;
import joltconnlib.backend.Containers.JoltArrayOfStringsContainer;
import joltconnlib.backend.Containers.JoltStringContainer;
import joltconnlib.metaObject.MetaDataObject;

public class GdxNetSync implements ISync{
    
    private final MessageDigest urlSha;



    private final GameJoltApi gameJoltAPi;

    public MessageDigest getSha() {
        return urlSha;
    }

    public GdxNetSync(MessageDigest urlSha ) {
        gameJoltAPi = new GameJoltApi();
        this.urlSha = urlSha;
    }

    public void getEntry(String key, String GameID, String GameKey, JoltStringContainer result) {
        DataStoreFetchRequest request = DataStoreFetchRequest.builder().gameID(GameID).key(key).build();
        gameJoltAPi.sendRequest(request, GameKey, new GameJoltDataStore.DataStoreFetchListener(){

            @Override
            public void dataStoreFetch(DataStoreFetchValue value) {
                result.value = value.getData();
            }
        });
    }
    
    public void getAllEntries(final String GameID, final String GameKey, JoltArrayOfStringsContainer result) {
        DataStoreGetKeysRequest request = DataStoreGetKeysRequest.builder().gameID(GameID).build();
        gameJoltAPi.sendRequest(request, GameKey, new GameJoltDataStore.DataStoreGetKeysListener(){

            @Override
            public void dataStoreGetKeys(DataStoreGetKeysValue value) {
                result.value = Arrays.asList(value.keys.toArray());
            }
        });
    }
    
}
