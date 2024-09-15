package com.github.retmode.connectorapp;
// package com.github.retmode.connectorapp;

// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;

// import com.github.retmode.connectorbackend.javasync.JavaLocalSync;
// import com.github.retmode.connectorbackend.javasync.JavaNetSync;
// import com.github.retmode.connectorbackend.javasync.JavaConfiguration;

// import com.github.retmode.connectorbase.Storage;


// public class JoltConnectorApp {

//     public static void main(String[] args) {
//         try {
//             MessageDigest sha = MessageDigest.getInstance("SHA-1");
//             JavaNetSync javaSync = new JavaNetSync(sha);
//             JavaLocalSync localSync = new JavaLocalSync(sha);
//             Storage storage = new Storage(new JavaConfiguration("../gameConfig.json", "gameA"), javaSync, localSync);
//             storage.storeFiles(Storage.Target.NET, Storage.Target.LOCAL);

//             storage = new Storage(new JavaConfiguration("../gameConfig.json", "gameB"), javaSync, localSync);
//             storage.storeFiles(Storage.Target.LOCAL, Storage.Target.NET);
//             storage.removeAllFiles(Storage.Target.LOCAL);
//             storage.removeAllFiles(Storage.Target.NET);
//         } catch (NoSuchAlgorithmException e) {
//             System.out.print("No sha-1 on this machine");
//         }
//     }
// }

import java.io.File;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class JoltConnectorApp extends Application {

    private GridPane gridPane;

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        GridPane gridPane = new GridPane();

        TextField textField = new TextField();
        Scene scene = new Scene(gridPane, 640, 480);
        gridPane.add(textField,0,0);
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Config Files", "*.json"));
        System.out.println(fileChooser.showOpenDialog(stage));

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}