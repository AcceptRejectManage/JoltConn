package joltconnlib.backend;

import java.util.List;

public interface ISync {
    public String getEntry(String key, String GameID, String GameKey);
    public List<String> getAllEntries(String GameID, String GameKey); 
}
