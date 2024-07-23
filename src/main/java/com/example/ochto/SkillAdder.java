package com.example.ochto;

import java.io.*;
import java.net.URL;
import java.util.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

import javafx.stage.Stage;

public class SkillAdder implements Initializable {

    @FXML
    private VBox mainVBox;
    @FXML
    private Button actionButton;
    @FXML
    private Button ruleButton;
    @FXML
    private Button responseButton;
    @FXML
    private Button saveButton;
    @FXML
    private ScrollPane scrollPane1;
    @FXML
    private TextField actionTextfield;
    @FXML
    private TextField ruleTextfield;
    @FXML
    private TextField responseTextfield;

    public ArrayList<ArrayList<ArrayList<String>>> allActions;
    public ArrayList<ArrayList<String>> currentAction;
    public ArrayList<String> action;
    public ArrayList<String> rules;
    public ArrayList<String> responses;

    private HBox currentHBox;
    private HBox lineHBox;
    private VBox currentActionVbox;
    private VBox currentRuleResponseVbox;
    private VBox ruleVbox;
    private VBox responseVbox;
    private ChatScreen logic = new ChatScreen();

    public ArrayList<String> getAllActions() {
        String folderPath = "Questions";
        ArrayList<String> allQs = new ArrayList<>();

        // Get a list of all the files in the folder
        File folder = new File(folderPath);
        File[] fileList = folder.listFiles();

        // Loop over each file in the folder and read the first line
        for (File file : fileList) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                String secondLine = br.readLine();
                allQs.add(secondLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return allQs;
    }


    @FXML
    void onActionButton(ActionEvent event) {
        ArrayList<String> allQs = getAllActions();
        String input = actionTextfield.getText();

        for (String qs: allQs) {
            if (input.equalsIgnoreCase(qs)){
                input = "";
                System.out.println("Action already exists");
            }
        }

        if (!input.isEmpty()) {
            lineHBox = new HBox();
            currentHBox = new HBox();
            mainVBox.getChildren().add(lineHBox);
            mainVBox.getChildren().add(currentHBox);

            Text line = new Text("____________________________________________________________________________________________________________________________________________________________________________________");
            lineHBox.getChildren().addAll(line);

            currentActionVbox = new VBox();
            currentActionVbox.setPrefWidth(300);
            currentRuleResponseVbox = new VBox();
            currentRuleResponseVbox.setPrefWidth(600);
            ruleVbox = new VBox();
            ruleVbox.setPrefWidth(600);
            responseVbox = new VBox();
            responseVbox.setPrefWidth(600);

            currentHBox.getChildren().addAll(currentActionVbox);
            currentHBox.getChildren().addAll(currentRuleResponseVbox);

            currentRuleResponseVbox.getChildren().addAll(ruleVbox);
            currentRuleResponseVbox.getChildren().addAll(responseVbox);

            Text text = new Text(input);
            currentActionVbox.getChildren().addAll(text);

            Text ruleText = new Text("RULES:");
            ruleText.setFill(Color.BLUE);
            Text responseText = new Text("\nRESPONSES:");
            responseText.setFill(Color.BLUE);

            ruleVbox.getChildren().addAll(ruleText);
            responseVbox.getChildren().addAll(responseText);

            currentAction = new ArrayList<>();
            action = new ArrayList<>();
            responses = new ArrayList<>();
            rules = new ArrayList<>();
            action.add(input);
            currentAction.add(action);
            currentAction.add(rules);
            currentAction.add(responses);
            allActions.add(currentAction);

            actionTextfield.clear();
        } else {
            System.out.println("Input is Empty");
        }
    }

    @FXML
    void onRuleButton(ActionEvent event) {
        if (allActions.isEmpty()) {
            System.out.println("Enter an action first");
        } else {
            String ruleInput = ruleTextfield.getText();
            if (!ruleInput.isEmpty()) {
                Text ruleText = new Text(ruleInput);
                ruleVbox.getChildren().addAll(ruleText);
                rules.add(ruleInput);
                ruleTextfield.clear();

            } else {
                System.out.println("Rule Inputs are Empty");
            }
        }
    }

    @FXML
    void onResponseButton(ActionEvent event) {
        if (allActions.isEmpty()) {
            System.out.println("Enter an action first");
        } else {
            String responseInput = responseTextfield.getText();
            if (!responseInput.isEmpty()) {
                Text responseText = new Text(responseInput);
                responseVbox.getChildren().addAll(responseText);
                responses.add(responseInput);
                responseTextfield.clear();

            } else {
                System.out.println("Response Inputs are Empty");
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allActions = new ArrayList<>();
        currentHBox = new HBox();
        mainVBox.getChildren().add(currentHBox);

        currentActionVbox = new VBox();
        currentActionVbox.setPrefWidth(300);
        currentRuleResponseVbox = new VBox();
        currentRuleResponseVbox.setPrefWidth(600);
        ruleVbox = new VBox();
        ruleVbox.setPrefWidth(600);
        responseVbox = new VBox();
        responseVbox.setPrefWidth(600);

        currentRuleResponseVbox.getChildren().addAll(ruleVbox);
        currentRuleResponseVbox.getChildren().addAll(responseVbox);

        currentHBox.getChildren().addAll(currentActionVbox);
        currentHBox.getChildren().addAll(currentRuleResponseVbox);

        Text ruleText = new Text("RULES:");
        ruleText.setFill(Color.BLUE);
        Text responseText = new Text("\nRESPONSES:");
        responseText.setFill(Color.BLUE);

        Text actionText = new Text("<LOCATION> | <SCHEDULE>");
        currentActionVbox.getChildren().addAll(actionText);
        Text ruleText1 = new Text("<LOCATION> Where is <ROOM> | Where is <ROOM> located");
        ruleVbox.getChildren().addAll(ruleText, ruleText1);
        Text ruleText2 = new Text("<ROOM> DeepSpace | SpaceBox");
        ruleVbox.getChildren().addAll(ruleText2);
        Text ruleText3 = new Text("<SCHEDULE> Which lectures are there <TIME>");
        ruleVbox.getChildren().addAll(ruleText3);
        Text ruleText4 = new Text("<TIME> 9 | 12");
        ruleVbox.getChildren().addAll(ruleText4);
        Text responseText1 = new Text("<LOCATION> * <ROOM> DeepSpace DeepSpace is the first room after the entrance");
        responseVbox.getChildren().addAll(responseText ,responseText1);
        Text responseText3 = new Text("<SCHEDULE> * <TIME> 9 There are no lectures at 9");
        responseVbox.getChildren().addAll(responseText3);
    }

    @FXML
    void onSaveButton(ActionEvent event) {
        for (ArrayList<ArrayList<String>> currQs : allActions) {
            createCSV(currQs);
        }

        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    public void createCSV(ArrayList<ArrayList<String>> currentAction) {
        String fileName = "Questions/CFG" + (fileCount()) + ".csv";
        String action = currentAction.get(0).get(0);
        ArrayList<String> rules = currentAction.get(1);
        ArrayList<String> responses = currentAction.get(2);

        StringBuilder output = new StringBuilder();
        output.append("Rule <S> <ACTION>").append("\n");
        output.append("Rule <ACTION> ").append(action).append("\n");
        for (String currentRule : rules) {
            output.append("Rule ").append(currentRule).append("\n");
        }
        for (String currentResponse : responses) {
            output.append("Action ").append(currentResponse).append("\n");
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

    public int fileCount() {
        File folder = new File("Questions/");
        int count = 0;
        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isFile()) {
                count++;
            }
        }

        return count;
    }

    public static String cleanWord(String input) {
        return input.replaceAll("[^\\p{L}\\p{N}]+", "");
    }


    public void setLogic(ChatScreen logic) {
        this.logic = logic;
    }
}
