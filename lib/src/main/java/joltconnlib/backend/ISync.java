package joltconnlib.backend;

import java.security.MessageDigest;

import joltconnlib.backend.Containers.JoltArrayOfStringsContainer;
import joltconnlib.backend.Containers.JoltStringContainer;
import joltconnlib.configuration.Configuration;
import joltconnlib.metaObject.MetaDataObject;

public interface ISync {
    public void getEntry(String key, Configuration configuration, JoltStringContainer result);
    public void getAllEntries(Configuration configuration, JoltArrayOfStringsContainer result) ;
    public void writeFile(Configuration configuration, String path, String data);
    public void writeMetaFiles(Configuration configuration, MetaDataObject metaDataObject);
    public MessageDigest getSha();
}
