package joltconnlib;

public class JoltEntry {
    byte[] sha;
    String key;

    public JoltEntry(byte[] sha, String key) {
        this.sha = sha;
        this.key = key;
    }
}
