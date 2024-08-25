package joltconnapp;

import com.badlogic.gdx.ApplicationAdapter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import joltconnlib.Configuration;
import joltconnlib.JoltStorage;
import joltconnlib.LocalStorage;
import joltconnlib.gdxSync.GdxNetSync;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class AppGdx extends ApplicationAdapter {

    @Override
    public void create() {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            GdxNetSync gdxSync = new GdxNetSync(sha);
            // LocalStorage localStorage = new LocalStorage(new Configuration(new File("./gameConfig.json"), "gameA"), gdxSync);
            // localStorage.writeMetaFilesToDisk();
            // JoltStorage joltStorage = new JoltStorage(new Configuration(new File("./gameConfig.json"), "gameB"), gdxSync);
            // joltStorage.writeMetaFilesToDisk();
            // joltStorage.writeFilesToDisk();
        } catch (NoSuchAlgorithmException e) {
            System.out.print("No sha-1 on this machine");
        }
    }

    @Override
    public void render() {

    }

    @Override
    public void dispose() {

    }
}
