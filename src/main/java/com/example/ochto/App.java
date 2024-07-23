package com.example.ochto;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;


public class App extends Application {

    public String name;

    @Override
    public void start(Stage stage) throws IOException {
        runFR();
        if (name != null && !name.equals("Unknown (0.0)")){
            runChat(stage);
        }else {
            System.out.println("Unknown user detected. Would you like to add a new user? yes/no");
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine();
            if (response.equalsIgnoreCase("yes")) {
                System.out.println("Enter the image name: ");
                String imageName = scanner.nextLine();
                captureScreenshot(imageName);
            } else {
                return;
            }
        }
        stage.centerOnScreen();
        stage.show();
    }


    public void runFR() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("python", "Python_facial_recognition/model_1/Recognizer.py");
        Process proc = processBuilder.start();
        BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        name = out.readLine();
    }


    public void runChat(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view1.fxml"));
        Parent root = loader.load();
        ChatScreen controller = loader.getController();
        controller.setName(name);
        stage.setTitle("OCTO");
        Image image = new Image("file:src/main/resources/com/example/ochto/pics/img_5.png");
        stage.getIcons().add(image);
        stage.setResizable(false);
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }


    public void captureScreenshot(String imageName) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("python", "Python_facial_recognition/model_1/Capture.py", imageName);
        Process proc = processBuilder.start();
        BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        name = out.readLine();
    }


    public String getName() {
        return name;
    }


    public static void main(String[] args) {
        launch();
    }
}

