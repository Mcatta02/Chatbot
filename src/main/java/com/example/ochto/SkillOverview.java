package com.example.ochto;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class SkillOverview implements Initializable {

    @FXML
    private Label label;
    @FXML
    private VBox mainVBox;
    @FXML
    private VBox editVBox;
    @FXML
    private ScrollPane scrollPane1;
    private Button saveButton;
    private final Stage stage4 = new Stage();
    private ChatScreen logic = new ChatScreen();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createQuestionList2();
    }


    public HashMap<String, File> getAllQuestions() {
        String folderPath = "Questions";
        HashMap<String, File> allFiles = new HashMap<>();

        // Get a list of all the files in the folder
        File folder = new File(folderPath);
        File[] fileList = folder.listFiles();

        // Loop over each file in the folder and read the first line
        for (File file : fileList) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                String secondLine = br.readLine();
                allFiles.put(secondLine, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return allFiles;
    }

    public void createQuestionList2() {
        HashMap<String, File> allQuestions = getAllQuestions(); // <Qs, path>

        Text explanation = new Text("Double Click on an Action to Edit it");
        explanation.setFont(Font.font("verdana", FontWeight.BLACK, FontPosture.REGULAR, 21));
        mainVBox.getChildren().addAll(explanation);
        Text line = new Text("________________________________________________________________________________________________________________");
        explanation.setFont(Font.font("verdana", FontWeight.BLACK, FontPosture.REGULAR, 21));
        mainVBox.getChildren().addAll(line);

        for (String currentQuestion : allQuestions.keySet()) {
            Text input = new Text(currentQuestion + "\n");
            input.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("view5.fxml"));

                        SkillEditor controller = new SkillEditor();
                        controller.setFile(allQuestions.get(currentQuestion));
                        controller.setLogic(logic);

                        loader.setController(controller); //initialize
                        Parent root = loader.load();

                        Scene scene = new Scene(root);
                        Stage stage = (Stage) input.getScene().getWindow();
                        stage.close();

                        stage4.setScene(scene);
                        stage4.setTitle("Skill Editor");
                        stage4.setResizable(false);
                        stage4.centerOnScreen();
                        stage4.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            input.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 15));
            mainVBox.getChildren().addAll(input);
        }
    }

    public void setLogic(ChatScreen logic) {
        this.logic = logic;
    }

    @FXML
    void onSaveButton(ActionEvent event) {
//        logic.reloadAllSkills();
        Stage stage = (Stage) mainVBox.getScene().getWindow();
        stage.close();
    }
}
