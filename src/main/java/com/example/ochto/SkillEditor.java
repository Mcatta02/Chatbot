package com.example.ochto;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.example.logic.CSVReader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SkillEditor implements Initializable {

    @FXML
    private VBox responseVBox;
    @FXML
    private Label label;
    @FXML
    private Label label1;
    @FXML
    private Label label2;
    @FXML
    private VBox actionVBox;
    @FXML
    private ScrollPane scrollPane1;
    @FXML
    private ScrollPane scrollPane11;
    @FXML
    private ScrollPane scrollPane12;
    @FXML
    private VBox ruleVBox;

    public CSVReader reader;
    @FXML
    private Button saveButton;
    private final Stage stage7 = new Stage();
    private ChatScreen logic = new ChatScreen();
    private String filename;

    private String[] updates;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveButton = new Button();

        actionTable();
        ruleTable();
        responseTable();
    }

    public void actionTable() {
        String action = reader.getAction();
        Text input = new Text(action);
        input.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 15));

        input.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                // create a new TextField and set its initial value to the Text object's text
                TextField textField = new TextField(input.getText());

                // replace the Text object with the TextField in the VBox
                VBox parent = (VBox) input.getParent();
                parent.getChildren().set(parent.getChildren().indexOf(input), textField);

                // request focus and select all text in the TextField
                textField.requestFocus();
                textField.selectAll();

                // set an action listener on the TextField to handle the text input
                textField.setOnAction(actionEvent -> {
                    input.setText(textField.getText());
                    updates[1] = textField.getText();
                    parent.getChildren().set(parent.getChildren().indexOf(textField), input);
                });
            }
        });

        actionVBox.getChildren().addAll(input);
    }

    public void ruleTable() {
        ArrayList<String> rules = reader.getRules();

        for (String currentRule: rules) {
            Text ruleInput = new Text(currentRule);
            ruleInput.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 15));

            ruleInput.setOnMouseClicked(event -> {
                if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    // create a new TextField and set its initial value to the Text object's text
                    TextField textField = new TextField(ruleInput.getText());

                    // replace the Text object with the TextField in the VBox
                    VBox parent = (VBox) ruleInput.getParent();
                    parent.getChildren().set(parent.getChildren().indexOf(ruleInput), textField);

                    // request focus and select all text in the TextField
                    textField.requestFocus();
                    textField.selectAll();

                    // set an action listener on the TextField to handle the text input
                    textField.setOnAction(actionEvent -> {

                        for (int i = 2; i < updates.length; i++) {
                            if (updates[i].substring(4).equals(ruleInput.getText())){
                                updates[i] = "Rule" + textField.getText();
                            }
                        }

                        ruleInput.setText(textField.getText() + "\n");

                        parent.getChildren().set(parent.getChildren().indexOf(textField), ruleInput);
                    });
                }
            });
            ruleVBox.getChildren().addAll(ruleInput);
        }
    }

    public void responseTable() {
        ArrayList<String> responses = reader.getResponses();

        for (String currentResponse: responses) {
            Text responseInput = new Text(currentResponse);
            responseInput.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 15));

            responseInput.setOnMouseClicked(event -> {
                if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    // create a new TextField and set its initial value to the Text object's text
                    TextField textField = new TextField(responseInput.getText());

                    // replace the Text object with the TextField in the VBox
                    VBox parent = (VBox) responseInput.getParent();
                    parent.getChildren().set(parent.getChildren().indexOf(responseInput), textField);

                    // request focus and select all text in the TextField
                    textField.requestFocus();
                    textField.selectAll();

                    // set an action listener on the TextField to handle the text input
                    textField.setOnAction(actionEvent -> {

                        for (int i = 2; i < updates.length; i++) {
                            if (updates[i].substring(6).equals(responseInput.getText())){
                                updates[i] = "Action" + textField.getText();
                            }
                        }

                        responseInput.setText(textField.getText() + "\n");

                        parent.getChildren().set(parent.getChildren().indexOf(textField), responseInput);
                    });
                }
            });
            responseVBox.getChildren().addAll(responseInput);
        }
    }

    public void createCSV(String[] updates) {
        String fileName = "Questions/" + filename;
        updates[1] = "Rule" + updates[1];

        StringBuilder output = new StringBuilder();

        for (String currentUpdate: updates) {
            output.append(currentUpdate).append("\n");
        }

        try {
            FileWriter fw = new FileWriter(fileName);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            pw.write(String.valueOf(output));

            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSaveButton(ActionEvent event) {
        createCSV(updates);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view4.fxml"));

            SkillOverview controller = new SkillOverview();
            loader.setController(controller); //initialize
            Parent root = loader.load();
            controller.setLogic(logic);

            Scene scene = new Scene(root);
            Stage stage = (Stage) responseVBox.getScene().getWindow();
            stage.close();

            stage7.setScene(scene);
            stage7.setTitle("Skill Overview");
            stage7.setResizable(false);
            stage7.centerOnScreen();
            stage7.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLogic(ChatScreen logic) {
        this.logic = logic;
    }

    public void setFile(File filePath) {
        filename = filePath.getName();
        reader = new CSVReader(filePath);
        updates = reader.getEverything();
    }
}
