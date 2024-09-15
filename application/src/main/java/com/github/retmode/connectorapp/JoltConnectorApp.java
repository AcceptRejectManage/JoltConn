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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class JoltConnectorApp extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Config Files", "*.json"));
        System.out.println(fileChooser.showOpenDialog(stage));

        // String javaVersion = System.getProperty("java.version");
        // String javafxVersion = System.getProperty("javafx.version");

        TextField textField = new TextField();
        textField.setText("SD");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.add(textField,0,0);
        
        Scene scene = new Scene(gridPane, 640, 480);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setTitle("Jolt Connector");
        stage.show();
    }

    @Override
    public void init() {}

    @Override
    public void stop() {}
}