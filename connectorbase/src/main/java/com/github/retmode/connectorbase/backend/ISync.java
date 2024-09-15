package com.github.retmode.connectorbase.backend;

import java.security.MessageDigest;

import com.github.retmode.connectorbase.backend.Containers.JoltArrayOfStringsContainer;
import com.github.retmode.connectorbase.backend.Containers.JoltStringContainer;
import com.github.retmode.connectorbase.backend.IConfiguration;
import com.github.retmode.connectorbase.metaobject.MetaDataObject;

public interface ISync {
    public void getEntry(String key, IConfiguration configuration, JoltStringContainer result);
    public void getAllEntries(IConfiguration configuration, JoltArrayOfStringsContainer result) ;
    public void writeEntry(IConfiguration configuration, String key, String data);
    public void removeEntry(IConfiguration configuration, String key);
    public void writeMetaFiles(IConfiguration configuration, MetaDataObject metaDataObject);
    public MessageDigest getSha();
}
