package com.github.retmode.connectorbase.backend;

import java.security.MessageDigest;

import com.github.retmode.connectorbase.backend.Containers.JoltArrayOfStringsContainer;
import com.github.retmode.connectorbase.backend.Containers.JoltStringContainer;
import com.github.retmode.connectorbase.backend.IConfiguration;
import com.github.retmode.connectorbase.metaobject.MetaDataObject;

public interface ISync {
    public void getEntry(String key, IConfiguration configuration, JoltStringContainer result);
    public void getAllEntries(IConfiguration configuration, JoltArrayOfStringsContainer result) ;
    public void writeFile(IConfiguration configuration, String path, String data);
    public void writeMetaFiles(IConfiguration configuration, MetaDataObject metaDataObject);
    public MessageDigest getSha();
}
