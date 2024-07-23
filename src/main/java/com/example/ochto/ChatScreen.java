package com.example.ochto;

import com.example.logic.*;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class ChatScreen implements Initializable {
    @FXML
    private TextField text_field;
    @FXML
    private VBox vbox_message;
    @FXML
    private ScrollPane scrollPane;
    private final LocalTime time = LocalTime.now();
    private final LocalDate date = LocalDate.now();
    private final Random random = new Random();
    private String message;
    private final Timer timer = new Timer();
    @FXML
    private final VBox vBox = new VBox();
    @FXML
    Circle circle = new Circle();
    private final Stage stage2 = new Stage();
    private final Stage stage3 = new Stage();
    @FXML
    private Button theme_button = new Button();
    private boolean isDarkTheme = true;
    private ImageView dark_theme_ImageView;
    private ImageView light_theme_ImageView;
    private Image dark_theme_background = new Image("file:src/main/resources/com/example/ochto/pics/img_1.png");
    private Image light_theme_background = new Image("file:src/main/resources/com/example/ochto/pics/White_full.png");
    @FXML
    private ImageView main_image_view = new ImageView();
    @FXML
    private Button editButton;
    @FXML
    private Button skillButton;
    private CYKHandler handler;
    private SpellChecker spellChecker;
    @FXML
    private ChoiceBox<String> choiceBox;
    private String[] optionsForChoiceBox = {"Normal", "RNN", "Naive Bayes"};
    String assistantType = "Normal";
    private String preProcessingPy = " ";
    @FXML
    private Button algorithmButton = new Button();
    private ArrayList<String> userChatLog;
    private ArrayList<String> octoChatLog;
    private ArrayList<String> userSlotLog;
    private final App app = new App();
    private boolean isCYK = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userChatLog = new ArrayList<>();
        octoChatLog = new ArrayList<>();
        userSlotLog = new ArrayList<>();
        octoChatLog.add("Hi user");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        vbox_message.heightProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                scrollPane.setVvalue((Double) newValue);
            }
        });
        vbox_message.setBackground(new Background(new BackgroundFill(Color.rgb(30, 30, 30),
                CornerRadii.EMPTY,
                Insets.EMPTY)));

        Image img = new Image("file:src/main/resources/com/example/ochto/pics/img.png");
        ImageView view = new ImageView(img);
        view.setFitHeight(23);
        view.setPreserveRatio(true);

        text_field.setStyle("""
                .}text-field {
                 -fx-border-radius: 7 7 7 7;
                 -fx-background-radius: 7 7 7 7;""");


        Image im = new Image("file:src/main/resources/com/example/ochto/pics/img_6.png", false);
        circle.setFill(new ImagePattern(im));
        theme_button.setStyle(
                "-fx-background-radius: 5em; " +
                        "-fx-min-width: 50px; " +
                        "-fx-min-height: 50px; " +
                        "-fx-max-width: 50px; " +
                        "-fx-max-height: 50px;"
        );
        Image dark_theme = new Image("file:src/main/resources/com/example/ochto/pics/dark_theme.png");
        dark_theme_ImageView = new ImageView(dark_theme);
        Image light_theme = new Image("file:src/main/resources/com/example/ochto/pics/light_theme.png");
        light_theme_ImageView = new ImageView(light_theme);
        dark_theme_ImageView.setFitHeight(50);
        dark_theme_ImageView.setPreserveRatio(true);
        light_theme_ImageView.setFitHeight(50);
        light_theme_ImageView.setPreserveRatio(true);
        theme_button.setGraphic(light_theme_ImageView);
        main_image_view.setImage(dark_theme_background);
        choiceBox.getItems().addAll(optionsForChoiceBox);
        choiceBox.setOnAction(this::changeChoiceBoxOption);
    }

    @FXML
    public void handle(ActionEvent newActionEvent) {
        message = text_field.getText();
        String preMessage = message;

        addUMessage(message, vbox_message);
        if (!message.isEmpty()) {
            handler = new CYKHandler();

            if (assistantType.equalsIgnoreCase("Normal")) {
                if (octoChatLog.get(octoChatLog.size() - 1).contains("<") && octoChatLog.get(octoChatLog.size() - 1).contains(">")) {
                    userSlotLog.add(message);
                    String ans = userChatLog.get(userChatLog.size() - 1);
                    String slt = userSlotLog.get(userSlotLog.size() - 1);
                    message = handler.retrieveMergedAnswer(ans, slt);
                } else {
                    userChatLog.add(message);
                    message = handler.retrieveAnswer(message);
                }
                octoChatLog.add(message);
            } else if (assistantType.equalsIgnoreCase("Naive Bayes")) {
                Bayes bayesClassifier = new Bayes();
                message = bayesClassifier.getPromptAnswer(message);
            } else {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder("python", "src/main/java/com/example/logic/RNN.py", preMessage);
                    Process proc = processBuilder.start();
                    BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    preProcessingPy = out.readLine();
                    String preProcessingPy1= out.readLine();
                    String preProcessingPy2= out.readLine();
                    String preProcessingPy3= out.readLine();
                    if (preProcessingPy3.equals("In-domain")) {
                        if (octoChatLog.get(octoChatLog.size() - 1).contains("<") && octoChatLog.get(octoChatLog.size() - 1).contains(">")) {
                        userSlotLog.add(message);
                        String ans = userChatLog.get(userChatLog.size() - 1);
                        String slt = userSlotLog.get(userSlotLog.size() - 1);
                        message = handler.retrieveMergedAnswer(ans, slt);
                        } else {
                            userChatLog.add(message);
                            message = handler.retrieveAnswer(message);
                        }
                        octoChatLog.add(message);
                    }
                    else{
                        message = preProcessingPy3;
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } else {
            message = "Input something!";
        }

        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    addBMessage(message, vbox_message);
                });
            }

            ;
        };
        int delay = 1;
        timer.schedule(tt, delay * 500);
    }

    public void addUMessage(String message, VBox vbox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow TextFlow = new TextFlow(text);
        TextFlow.setStyle("-fx-color: rgb(37,211,102);" +
                "-fx-background-color: rgb(7, 94, 84);" +
                " -fx-background-radius: 20px;");

        TextFlow.setPadding(new Insets(5, 10, 5, 10));
        text.setFill(Color.color(1, 1, 1));
        text.setFont(Font.font("MathBold", FontWeight.BOLD, FontPosture.REGULAR, 15));

        ImageView imageView = new ImageView(new Image("file:src/main/resources/com/example/ochto/pics/senderIcon.png"));
        imageView.setFitHeight(25);
        imageView.setFitWidth(25);

        hBox.getChildren().addAll(TextFlow, imageView);
        HBox.setHgrow(TextFlow, Priority.NEVER);
        HBox.setHgrow(imageView, Priority.NEVER);
        HBox.setMargin(imageView, new Insets(0, 4, 0, 10)); // add some margin between the icon and the text

        vbox.getChildren().add(hBox);

        text_field.clear();
    }

    public static void addBMessage(String message, VBox vbox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow TextFlow = new TextFlow(text);
        TextFlow.setStyle("-fx-color: rgb(37,211,102);" +
                "-fx-background-color: rgb(0, 102, 204);" +
                " -fx-background-radius: 20px;");
        text.setFont(Font.font("MathBold", FontWeight.BOLD, FontPosture.REGULAR, 15));

        ImageView imageView = new ImageView(new Image("file:src/main/resources/com/example/ochto/pics/octoIconChat.png"));
        imageView.setFitHeight(25);
        imageView.setFitWidth(25);

        TextFlow.setPadding(new Insets(5, 10, 5, 10));
        text.setFill(Color.color(1, 1, 1));

        hBox.getChildren().addAll(imageView, TextFlow);
        HBox.setHgrow(TextFlow, Priority.NEVER);
        HBox.setHgrow(imageView, Priority.NEVER);
        HBox.setMargin(imageView, new Insets(0, 10, 0, 0)); // add some margin between the text and the icon

        vbox.getChildren().add(hBox);
    }

    public void setName(String s) {
        addBMessage("Hi " + s.toUpperCase() + "! How can i assist you?", vbox_message);
    }

    @FXML
    public void handleAddButton(ActionEvent newActionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view3.fxml"));
            Parent root = loader.load();

            SkillAdder controllerAdd = new SkillAdder();
            loader.setController(controllerAdd);
            controllerAdd.setLogic(this);

            Scene scene = new Scene(root);
            stage2.setScene(scene);
            stage2.setTitle("Skill Adder");
            stage2.setResizable(false);
            stage2.centerOnScreen();
            stage2.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEditButton(ActionEvent newActionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view4.fxml"));

            SkillOverview controllerEdit = new SkillOverview();
            loader.setController(controllerEdit);
            Parent root = loader.load();
            controllerEdit.setLogic(this);

            Scene scene = new Scene(root);
            stage3.setScene(scene);
            stage3.setTitle("Skill Overview");
            stage3.setResizable(false);
            stage3.centerOnScreen();
            stage3.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void changeTheme(ActionEvent newActionEvent) {
        if (isDarkTheme) {//here we change to light theme
            theme_button.setGraphic(dark_theme_ImageView);
            isDarkTheme = false;
            main_image_view.setImage(light_theme_background);
            vbox_message.setBackground(new Background(new BackgroundFill(Color.rgb(190, 190, 190),
                    CornerRadii.EMPTY,
                    Insets.EMPTY)));

        } else {//here we change to dark theme
            theme_button.setGraphic(light_theme_ImageView);
            isDarkTheme = true;
            main_image_view.setImage(dark_theme_background);
            vbox_message.setBackground(new Background(new BackgroundFill(Color.rgb(30, 30, 30),
                    CornerRadii.EMPTY,
                    Insets.EMPTY)));
        }
    }

    @FXML
    public void changeChoiceBoxOption(ActionEvent newActionEvent) {
        assistantType = choiceBox.getValue();
        System.out.println(assistantType);
    }
}



