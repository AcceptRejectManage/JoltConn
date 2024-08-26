package joltconnlib;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class Configuration {
    private final File configPath;
    private final File path;
    private final String gameKey;
    private final String gameID;

    public String getKey() {
        return gameKey;
    }

    public String getID() {
        return gameID;
    }

    public String getPath() {
        return path.toString();
    }

    public String getConfigPath() {
        return configPath.toString();
    }

    @Override
    public String toString() {
        return new StringBuilder().append("gameID: ").append(gameID).append(" gameKey: ").append(gameKey)
            .append(" workdir: ").append(path.getAbsolutePath()).append(" configuration folder: ").append(configPath).toString();
    }
    
    public Configuration(String configFilePath, String gameName) {
        this(new File(configFilePath), gameName);
    }

    public Configuration(File configFilePath, String gameName) {
        boolean error = false;
        File configPath = null;
        File path = null;
        String gameKey = "";
        String gameID = "";
        if (configFilePath.isFile()) {
            try {
                configPath = configFilePath.getParentFile();
                String jsonString = Files.readString(configFilePath.toPath());
                JsonReader reader = new JsonReader();
                JsonValue json = reader.parse(jsonString).get(gameName);
                if (json != null) {
                    gameKey = json.getString("GameKey");
                    gameID = json.getString("GameID");
                    String folderRawPath = json.getString("Folder");
                    Path shortPath = Path.of(folderRawPath);
                    if (!shortPath.isAbsolute()) {
                        // relative but to what - first check relative to config;
                        File relativeToConfig = new File(configPath, folderRawPath);
                        if (relativeToConfig.isDirectory()) {
                            path = relativeToConfig;
                        } else {
                            // then check relative to app
                            File relativeToApp = shortPath.toFile();
                            path = relativeToApp;
                            if(!relativeToApp.isDirectory()) {
                                if (!path.mkdirs()) {
                                    //ignore config if we didnt created needed dirs after unsuccessful search
                                    error = true;
                                }
                            } 
                        }
                    } else {
                        // we have the path
                        path = shortPath.toFile();
                    }
                } else {
                    error = true;
                }
            } catch (Exception e) {
                error = true;
            }

            if (error) {
                configPath = null;
                path = null;
                gameKey = "";
                gameID = "";
            } 
        }
        this.configPath = configPath;
        this.path = path;
        this.gameKey = gameKey;
        this.gameID = gameID;
    }
}
