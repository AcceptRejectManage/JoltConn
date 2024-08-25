package joltconnlib.gdxSync;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreFetchRequest;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreFetchValue;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreGetKeysRequest;
import com.github.raeleus.gamejoltapi.GameJoltDataStore.DataStoreGetKeysValue;
import com.github.raeleus.gamejoltapi.GameJoltApi;
import com.github.raeleus.gamejoltapi.GameJoltDataStore;

import joltconnlib.backend.ISync;

public class GdxNetSync implements ISync{
    
    private class JoltStringContainer {
        public String value;
    }

    private class JoltArrayOfStringsContainer {
        public List<String> value;
    }

    private final GameJoltApi gameJoltAPi;

    public GdxNetSync(MessageDigest urlSha ) {
        gameJoltAPi = new GameJoltApi();
    }

    public String getEntry(String key, String GameID, String GameKey) {

        final CountDownLatch latch = new CountDownLatch(1);
        final JoltStringContainer jc = new JoltStringContainer();
        DataStoreFetchRequest request = DataStoreFetchRequest.builder().gameID(GameID).key(key).build();

        gameJoltAPi.sendRequest(request, GameKey, new GameJoltDataStore.DataStoreFetchListener(){

            @Override
            public void dataStoreFetch(DataStoreFetchValue value) {
                jc.value = value.getData();
                latch.countDown();

            }

            @Override
            public void failed(Throwable t) {
                latch.countDown();
            }
            
            @Override
            public void cancelled() {
                latch.countDown();
            }

        });

        // try{
        //     synchronized(latch) {
        //         latch.wait();
        //     }
        // } catch (InterruptedException e ) {
        //     System.out.println("Data not fetched!");
        // }
        System.out.println(jc.toString());
        return jc.value;
    }
    
    public List<String> getAllEntries(String GameID, String GameKey) {

        final CountDownLatch latch = new CountDownLatch(1);
        final JoltArrayOfStringsContainer jc = new JoltArrayOfStringsContainer();
        DataStoreGetKeysRequest request = DataStoreGetKeysRequest.builder().gameID(GameID).build();

        gameJoltAPi.sendRequest(request, GameKey, new GameJoltDataStore.DataStoreGetKeysListener(){

            @Override
            public void dataStoreGetKeys(DataStoreGetKeysValue value) {
                jc.value = new ArrayList<String>(java.util.Arrays.asList());
                System.out.println("O");
                latch.countDown();
            }

            @Override
            public void failed(Throwable t) {
                jc.value = new ArrayList<String>();
                latch.countDown();
            }
            
            @Override
            public void cancelled() {
                jc.value = new ArrayList<String>();
                latch.countDown();
            }

        });

        System.out.println("K");
        // try{
        //     synchronized(latch) {
        //         latch.wait();
        //     }
        // } catch (InterruptedException e ) {
        //     System.out.println("Data not fetched!");
        // }
        
        System.out.println(jc.toString());
        
        return jc.value;
    }
    
}
