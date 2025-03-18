package fi.tuni.secprog.passwordmanager;

import java.io.File;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Password manager application
 */
public class App extends Application {
    private Stage stage;

    private UserAuthentication userAuth = new UserAuthentication();
    private DatabaseHelper database = new DatabaseHelper();

    /*
     * Creates a big button with the given text
     */
    private Button createBigBtn(String text) {
        Button bigBtn = new Button(text);
        bigBtn.setStyle("-fx-font-size: 16pt; -fx-padding: 10px 20px;");
        bigBtn.setPrefSize(200, 50);
        return bigBtn;
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
        // Initialize the database
        database.initializeDatabase();

        // Create buttons for signing in and logging in
        ImageView keyIcon = getIcon("keyIcon");
        Button signInBtn = createBigBtn("Sign In");
        Button logInBtn = createBigBtn("Log In");
        signInBtn.setOnAction(e -> signInScene());
        logInBtn.setOnAction(e -> loginScene());

        // Set layout
        VBox root = new VBox(20, keyIcon, logInBtn, signInBtn);
        root.setPadding(new Insets(10, 10, 10, 10));
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
        usernameField.setPrefWidth(200);

        // Use PasswordField for password to hide input text
        PasswordField passField = new PasswordField();
        passField.setPrefWidth(200);

        Button loginBtn = createBigBtn("Log In");
        loginBtn.setOnAction(e -> {
            boolean isSuccesful = userAuth.authenticateUser(usernameField.getText(), passField.getText());
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

        root.setPadding(new Insets(10, 10, 10, 10));
        root.setAlignment(Pos.TOP_CENTER);
        Scene loginScene = new Scene(root, 420, 620);
        stage.setScene(loginScene);
    }

    /*
     * Creates the sign-in scene
     */
    private void signInScene() {
        TextField usernameField = new TextField();
        usernameField.setPrefWidth(200);
        
        // Use PasswordField for password to hide input text
        PasswordField passField = new PasswordField();
        passField.setPrefWidth(200);
        PasswordField passRepetitionField = new PasswordField();
        passRepetitionField.setPrefWidth(200);

        Button signinBtn = createBigBtn("Sign In");
        signinBtn.setOnAction(e -> {
            if (!passField.getText().equals(passRepetitionField.getText())) {
                // THIS SHOULD BE A LABEL
                System.out.println("Passwords don't match");
            } else {
                boolean isSuccesful = userAuth.registerUser(usernameField.getText(), passField.getText());
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

        root.setPadding(new Insets(10, 10, 10, 10));
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
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 420, 620);
        stage.setScene(scene);
    }

    /*
     * Creates the scene where the keys are shown
     */
    private void keysScene() {
        // Here, reate a list of keys

        // Create a VBox for the keys
        VBox keysVBox = new VBox(10);
        keysVBox.setPadding(new Insets(10, 10, 10, 10));

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
     * Creates the scene where a new key can be added
     */
    private void addKeyScene() {
        // Create text fileds
        TextField websiteField = new TextField();
        websiteField.setPrefWidth(200);
        TextField usernameField = new TextField();
        usernameField.setPrefWidth(200);

        // Use PasswordField for password to hide input text
        PasswordField passField = new PasswordField();
        passField.setPrefWidth(200);

        // HOX
        // OR suggest a strong password and make it possible to copy it to clipboard
        // HOX

        Button addKeyBtn = createBigBtn("Add key");
        addKeyBtn.setOnAction(e -> {
            // Add the key to the database
        });

        // Set layout
        Button returnBtn = createReturnBtn();
        returnBtn.setOnAction(e -> mainScene());
        HBox addKeyTopBox = new HBox(20, returnBtn);
        addKeyTopBox.setAlignment(Pos.TOP_LEFT);

        // Create VBox with labeled fields for key name, password, and the add key button
        VBox root = new VBox(20, addKeyTopBox, 
            createLabeledField("Website:", websiteField),
            createLabeledField("Username:", usernameField), 
            createLabeledField("Password:", passField), 
            addKeyBtn);

        root.setPadding(new Insets(10, 10, 10, 10));
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
        launch();
    }
}
