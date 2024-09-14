package com.github.retmode.connectorbase.backend;

import java.util.List;

public class Containers {
    public static class JoltStringContainer {
        public String value;
        public boolean isValid(){
            return value != null;
        }
        public void clear() {
            value = null;
        }
    }

    public static class JoltArrayOfStringsContainer {
        public List<String> value;
        public boolean isValid(){
            return value != null;
        }
        public void clear() {
            value = null;
        }
    }
}
