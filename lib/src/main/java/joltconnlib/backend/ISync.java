package joltconnlib.backend;

import java.security.MessageDigest;

import joltconnlib.backend.Containers.JoltArrayOfStringsContainer;
import joltconnlib.backend.Containers.JoltStringContainer;

public interface ISync {
    public void getEntry(String key, String GameID, String GameKey, JoltStringContainer result);
    public void getAllEntries(final String GameID, final String GameKey, JoltArrayOfStringsContainer result) ;
    public MessageDigest getSha();
}
