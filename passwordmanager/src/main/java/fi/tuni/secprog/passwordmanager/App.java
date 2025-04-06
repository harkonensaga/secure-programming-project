package fi.tuni.secprog.passwordmanager;

import java.io.File;
import javafx.util.Duration;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;

/**
 * Password manager application
 */
public class App extends Application {
    private Stage stage;

    /*
     * Creates a label with the given text.
     */
    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: #AF69EE; -fx-font-size: 12pt;");
        label.setPrefSize(100, 25);
        return label;
    }

    /*
     * Creates a label with the given text.
     */
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 10pt; -fx-padding: 5px 10px;");
        label.setPrefSize(80, 25);
        return label;
    }

    /*
     * Creates a big button with the given text.
     */
    private Button createBigBtn(String text) {
        Button bigBtn = new Button(text);
        bigBtn.setStyle("-fx-font-size: 16pt; -fx-padding: 10px 20px;");
        bigBtn.setPrefSize(200, 50);
        return bigBtn;
    }

    /*
     * Creates a small button with the given text.
     */
    private Button createSmallBtn(String text) {
        Button smallBtn = new Button(text);
        smallBtn.setStyle("-fx-font-size: 8pt; -fx-padding: 5px 10px;");
        smallBtn.setPrefSize(70, 15);
        return smallBtn;
    }

    /*
     * Creates a return button
     */
    private Button createReturnBtn() {
        Button returnBtn = new Button("↩");
        returnBtn.setPrefSize(35, 35);
        returnBtn.setStyle("-fx-font-size: 16pt; -fx-padding: 2px; -fx-background-radius: 25%;");
        return returnBtn;
    }

    /*
     * Creates a log out button
     */
    private Button createLogOuBtn() {
        Button logOutBtn = new Button("Log Out");
        logOutBtn.setPrefSize(80, 35);
        logOutBtn.setStyle("-fx-font-size: 12pt; -fx-padding: 2px; -fx-background-radius: 25%;");
        logOutBtn.setOnAction(e -> {
            // AESKeyHolder.clearKey();
            UserAuthentication.clearUserId();
            start(stage);
        });
        return logOutBtn;
    }

    /*
     * Creates a label and a field with the given label text and text field
     */
    private VBox createLabeledField(String labelText, TextField textField) {
        Label label = new Label(labelText);
        VBox vbox = new VBox(5, label, textField);
        vbox.setAlignment(Pos.TOP_LEFT);
        return vbox;
    }

    @Override
    public void start(Stage stage) {

        // Create buttons for signing in and logging in
        ImageView keyIcon = getIcon("keyIcon");
        Button signInBtn = createBigBtn("Sign In");
        Button logInBtn = createBigBtn("Log In");
        signInBtn.setOnAction(e -> signInScene());
        logInBtn.setOnAction(e -> loginScene());

        // Set layout
        VBox root = new VBox(20, keyIcon, logInBtn, signInBtn);
        root.setPadding(new Insets(20, 30, 20, 30));
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 420, 620);
        stage.setScene(scene);
        stage.setTitle("Password Manager");
        this.stage = stage;
        stage.show();
    }

    /*
     * Creates the login scene
     */
    private void loginScene() {
        /// Create text fields for username and password
        TextField usernameField = new TextField();

        // Use PasswordField for password to hide input text
        PasswordField passField = new PasswordField();

        Button loginBtn = createBigBtn("Log In");
        loginBtn.setOnAction(e -> {
            boolean isSuccesful = UserAuthentication.authenticateUser(usernameField.getText(), passField.getText());
            if (isSuccesful) {
                mainScene();
            } else {
                // THIS SHOULD BE A LABEL
                System.out.println("Login failed");
            }
        });

        // Set layout
        Button returnBtn = createReturnBtn();
        returnBtn.setOnAction(e -> start(stage));
        HBox loginTopBox = new HBox(20, returnBtn);
        loginTopBox.setAlignment(Pos.TOP_LEFT);

        // Create VBox with labeled fields for username and password, and the login button
        VBox root = new VBox(20, loginTopBox, 
            createLabeledField("Username:", usernameField),
            createLabeledField("Password:", passField), 
            loginBtn);

        root.setPadding(new Insets(20, 30, 20, 30));
        root.setAlignment(Pos.TOP_CENTER);
        Scene loginScene = new Scene(root, 420, 620);
        stage.setScene(loginScene);
    }

    /*
     * Creates the sign-in scene
     */
    private void signInScene() {
        TextField usernameField = new TextField();
        
        // Use PasswordField for password to hide input text
        PasswordField passField = new PasswordField();
        PasswordField passRepetitionField = new PasswordField();

        Button signinBtn = createBigBtn("Sign In");
        signinBtn.setOnAction(e -> {
            if (!passField.getText().equals(passRepetitionField.getText())) {
                // THIS SHOULD BE A LABEL
                System.out.println("Passwords don't match");
            } else {
                boolean isSuccesful = UserAuthentication.registerUser(usernameField.getText(), passField.getText());
                if (isSuccesful) {
                    loginScene();
                    // THIS SHOULD BE A LABEL
                    System.out.println("User registered successfully");
                } else {
                    // THIS SHOULD BE A LABEL
                    System.out.println("User registration failed");
                }
            }
        });

        // Set layout
        Button returnBtn = createReturnBtn();
        returnBtn.setOnAction(e -> start(stage));
        HBox signInTopBox = new HBox(20, returnBtn);
        signInTopBox.setAlignment(Pos.TOP_LEFT);

        // Create VBox with labeled fields
        VBox root = new VBox(20, signInTopBox, 
            createLabeledField("Username:", usernameField), 
            createLabeledField("Password:", passField), 
            createLabeledField("Repeat Password:", passRepetitionField), 
            signinBtn);
        root.setPadding(new Insets(20, 30, 20, 30));
        root.setAlignment(Pos.TOP_CENTER);
        Scene signInScene = new Scene(root, 420, 620);
        stage.setScene(signInScene);
    }

    /*
     * Creates the main scene when logged in
     */
    private void mainScene() {
        // Create buttons for keys and adding a key
        Button keysBtn = createBigBtn("My keys");
        Button addBtn = createBigBtn("+ Add key");
        keysBtn.setOnAction(e -> keysScene());
        addBtn.setOnAction(e -> addKeyScene());

        Button logOutBtn = createLogOuBtn();
        HBox mainTopBox = new HBox(20, logOutBtn);
        mainTopBox.setAlignment(Pos.TOP_RIGHT);

        // Set layout
        VBox root = new VBox(20, mainTopBox, keysBtn, addBtn);
        root.setPadding(new Insets(20, 30, 20, 30));
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 420, 620);
        stage.setScene(scene);
    }

    /*
     * Creates the scene where the credentials are shown. Website name and
     * username are shown, and the password can be copied to clipboard.
     */
    private void keysScene() {
        // Create a list of keys and present them in a VBox with headers
        VBox keysVBox = new VBox(5);
        keysVBox.setPadding(new Insets(5, 5, 5, 5));
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER);
        Label websiteHeader = createLabel("Website");
        Label usernameHeader = createLabel("Username");
        Label passwordHeader = createLabel("Password");
        headerBox.getChildren().addAll(websiteHeader, usernameHeader, passwordHeader);
        keysVBox.getChildren().addAll(headerBox);

        for (String website : PasswordManager.getWebsites()) {
            // Get the credentials for the website
            List<String> credentials = PasswordManager.getPassword(website);

            // Create a button to copy the password to clipboard
            Button copyPasswordBtn = createSmallBtn("Copy");
            copyPasswordBtn.setOnAction(e -> {
                String password = credentials.get(1);
                StringSelection stringSelection = new StringSelection(password);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                System.out.println("Copied to clipboard!");

                // Clear clipboard after 10 seconds
                PauseTransition pause = new PauseTransition(Duration.seconds(10));
                pause.setOnFinished(event -> {
                    StringSelection empty = new StringSelection("");
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(empty, null);
                    System.out.println("Clipboard cleared!");
                });
                pause.play();
            });
            HBox infoBox = new HBox(40, createLabel(website),
                                    createLabel(credentials.get(0)),
                                    copyPasswordBtn);
            infoBox.setAlignment(Pos.CENTER);
            keysVBox.getChildren().add(infoBox);
        }

        // Set layout
        Button returnBtn = createReturnBtn();
        returnBtn.setOnAction(e -> mainScene());
        HBox keysTopBox = new HBox(20, returnBtn);
        keysTopBox.setAlignment(Pos.TOP_LEFT);
        VBox root = new VBox(20, keysTopBox, keysVBox);
        root.setAlignment(Pos.TOP_CENTER);
        Scene keysScene = new Scene(root, 420, 620);
        stage.setScene(keysScene);
    }

    /*
     * Creates the scene where a new key can be added.
     */
    private void addKeyScene() {
        // Create text fileds
        TextField websiteField = new TextField();
        TextField usernameField = new TextField();
        TextField passLength = new TextField();
        passLength.setMaxWidth(60);

        // Use PasswordField for password to hide input text
        PasswordField passField = new PasswordField();
        Button generatePass = createSmallBtn("Generate");
        generatePass.setOnAction(e -> {
            // Generate a password and set it to the password field
            try {
                int length = Integer.parseInt(passLength.getText());
                if (length < 8) {
                    System.out.println("Password length must be at least 8 characters.");
                    return;
                } else if (length > 20) {
                    System.out.println("Password length must be at most 20 characters.");
                    return;
                }
                passField.setText(PasswordManager.generatePassword(length));
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input for password length. Please enter a valid number.");
            }
        });

        Button addKeyBtn = createBigBtn("Add key");
        addKeyBtn.setOnAction(e -> {
            String website = websiteField.getText();
            String username = usernameField.getText();
            String password = passField.getText();
            if (website.isEmpty() || username.isEmpty() || password.isEmpty()) {
                System.out.println("Please fill in all fields.");
                return;
            }
            // Add the key to the database
            PasswordManager.storeKey(website, username, password);
            keysScene();
        });

        // Set layout
        Button returnBtn = createReturnBtn();
        returnBtn.setOnAction(e -> mainScene());
        HBox addKeyTopBox = new HBox(20, returnBtn);
        addKeyTopBox.setAlignment(Pos.TOP_LEFT);

        // Create VBox with labeled fields and the add key button
        VBox root = new VBox(20, addKeyTopBox, 
            createLabeledField("Website:", websiteField),
            createLabeledField("Username:", usernameField),
            createLabeledField("Password length:", passLength),
            createLabeledField("Password:", passField),
            generatePass,
            addKeyBtn);

        root.setPadding(new Insets(20, 30, 20, 30));
        root.setAlignment(Pos.TOP_CENTER);
        Scene addKeyScene = new Scene(root, 420, 620);
        stage.setScene(addKeyScene);
    }

    /**
     * Gets the png image and returns the image.
     */
    private ImageView getIcon(String iconName) {
        try {
            Image iconImage = new Image(new File(
                String.format("icons/%s.png", iconName)).toURI().toString());
            ImageView icon = new ImageView(iconImage);
            icon.setFitWidth(80);
            icon.setFitHeight(80);
            return icon;
        } catch (Exception e) {
            System.out.println("Error: Can't open icon picture.");
            return null;
        }
    }


    public static void main(String[] args) {
        // Initialize the database
        DatabaseHelper.initializeDatabase();

        launch();
    }
}
