package fi.tuni.secprog.passwordmanager;

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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import javafx.scene.layout.Region;
import static fi.tuni.secprog.passwordmanager.GUIElements.*;


/**
 * Password manager application.
 * This class handles the layout and user interactions.
 */
public class App extends Application {
    private Stage stage;
    private VBox root;
    private Button logOutBtn;
    private final String backgroundColor = "#22162B";

    @Override
    public void start(Stage stage) {
        // Create the root layout
        this.root = new VBox(20);
        root.setPadding(new Insets(20, 30, 20, 30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + backgroundColor);
        Scene scene = new Scene(root, 420, 620);
        stage.setScene(scene);
        stage.setTitle("Password Manager");
        this.stage = stage;
        stage.show();

        // Create buttons for signing in and logging in
        ImageView keyIcon = getIcon("keyIcon");
        Button signInBtn = createBigBtn("Sign In");
        Button logInBtn = createBigBtn("Log In");
        signInBtn.setOnAction(e -> signInScene());
        logInBtn.setOnAction(e -> loginScene());

        logOutBtn = createSmallBtn("Log Out");
        logOutBtn.setPrefSize(80, 35);
        logOutBtn.setOnAction(e -> {
            UserAuthentication.logoutUser();
            start(stage);
        });

        // Set layout
        root.getChildren().addAll(keyIcon, logInBtn, signInBtn);
    }

    /*
     * Creates the login scene
     */
    private void loginScene() {
        TextField usernameField = new TextField();
        PasswordField passField = new PasswordField();
        Label errorField = createErrorLabel("");

        // Box for showing the input field for TOTP code
        VBox TOTPcodeBox = new VBox(20);
        TOTPcodeBox.setAlignment(Pos.CENTER);

        Button loginBtn = createBigBtn("Log In");
        loginBtn.setOnAction(e -> {
            errorField.setText("");
            // Check if account is locked
            if (UserAuthentication.isAccountLocked(usernameField.getText())) {
                errorField.setText("Account is locked. Try again later.");
            } else {
                // Try to authenticate the user with username and password
                boolean isSuccesful = UserAuthentication.authenticateUser(usernameField.getText(),
                                                                        passField.getText().toCharArray());
                if (isSuccesful)  {
                    // Check user with TOTP
                    TextField TOTPcodeField = new TextField();
                    Button verifyBtn = createBigBtn("Verify");
                    TOTPcodeBox.getChildren().addAll(createLabeledField("TOTP code:", TOTPcodeField),
                                                    verifyBtn);
                    root.getChildren().remove(loginBtn);
                    verifyBtn.setOnAction(ev ->  {
                        boolean isTOTPValid = UserAuthentication.verifyTOTP(usernameField.getText(),
                                                                passField.getText().toCharArray(),
                                                                TOTPcodeField.getText());
                        if (isTOTPValid) {
                            mainScene();
                        } else {
                            errorField.setText("Invalid TOTP code. Please try again.");
                            TOTPcodeBox.getChildren().clear();
                            root.getChildren().add(loginBtn);
                        }
                    });
                } else {
                    errorField.setText("Invalid login credentials. Please try again.");
                }
            }
        });

        // Set layout
        Button returnBtn = createReturnBtn();
        returnBtn.setOnAction(e -> start(stage));
        HBox loginTopBox = new HBox(20, returnBtn);
        loginTopBox.setAlignment(Pos.TOP_LEFT);

        root.getChildren().clear();
        root.getChildren().addAll(loginTopBox, 
            createLabeledField("Username:", usernameField),
            createLabeledField("Password:", passField),
            errorField,
            loginBtn,
            TOTPcodeBox);
    }

    /*
     * Creates the sign-in scene
     */
    private void signInScene() {
        TextField usernameField = new TextField();
        PasswordField passField = new PasswordField();
        PasswordField passRepetitionField = new PasswordField();
        Label errorField = createErrorLabel("");
        
        // Box for showing the QR code and input field for TOTP code
        VBox TOTPcodeBox = new VBox(20);
        TOTPcodeBox.setAlignment(Pos.CENTER);

        Button signinBtn = createBigBtn("Sign In");
        signinBtn.setOnAction(e -> {
            errorField.setText("");
            // Check if the password is strong enough
            String password = passField.getText();
            String passwordStrength = UserAuthentication.checkPasswordStrenth(password);

            // Check if the fields are empty, and if the password is valid
            if (usernameField.getText().trim().isEmpty()) {
                errorField.setText("Please fill in all fields.");
            } else if (passwordStrength != null) {
                errorField.setText(passwordStrength);
            } else if (!passField.getText().equals(passRepetitionField.getText())) {
                errorField.setText("Passwords don't match.");
            } else {
                // Register the user
                Image QRcode = UserAuthentication.registerUser(usernameField.getText(),
                                                            password.toCharArray());
                if (QRcode != null) {
                    // Show the QR code and input field for TOTP code to the user
                    TextField TOTPcodeField = new TextField();
                    Button verifyBtn = createBigBtn("Verify");
                    ImageView QRcodeIV = new ImageView(QRcode);
                    QRcodeIV.setFitWidth(150);
                    QRcodeIV.setFitHeight(150);
                    TOTPcodeBox.getChildren().addAll(QRcodeIV,
                                                    createLabeledField("TOTP code:", TOTPcodeField),
                                                    verifyBtn);
                    root.getChildren().remove(signinBtn);

                    // Verify the TOTP code
                    verifyBtn.setOnAction(ev ->  {
                        boolean isTOTPValid = UserAuthentication.verifyTOTP(usernameField.getText(),
                                                                passField.getText().toCharArray(),
                                                                TOTPcodeField.getText());
                        if (isTOTPValid) {
                            mainScene();
                        } else {
                            errorField.setText("Invalid TOTP code. Please try again.");
                            TOTPcodeBox.getChildren().clear();
                            root.getChildren().add(signinBtn);
                        }
                    });
                } else errorField.setText("User registration failed.");
            }
        });

        // Set layout
        Button returnBtn = createReturnBtn();
        returnBtn.setOnAction(e -> start(stage));
        HBox signInTopBox = new HBox(20, returnBtn);
        signInTopBox.setAlignment(Pos.TOP_LEFT);

        root.getChildren().clear();
        root.getChildren().addAll(signInTopBox, 
            createLabeledField("Username:", usernameField), 
            createLabeledField("Password:", passField), 
            createLabeledField("Repeat Password:", passRepetitionField),
            errorField,
            signinBtn,
            TOTPcodeBox);
    }

    /*
     * Creates the main scene when logged in, where user can either view their keys
     * or add a new key.
     */
    private void mainScene() {
        // Create buttons for keys and adding a key
        Button keysBtn = GUIElements.createBigBtn("My keys");
        Button addBtn = GUIElements.createBigBtn("+ Add key");
        keysBtn.setOnAction(e -> keysScene());
        addBtn.setOnAction(e -> addKeyScene());

        HBox mainTopBox = new HBox(20, logOutBtn);
        mainTopBox.setAlignment(Pos.TOP_RIGHT);

        root.getChildren().clear();
        root.getChildren().addAll(mainTopBox, keysBtn, addBtn);
    }

    /*
     * Creates the scene where the credentials are shown.
     * Website name and username are shown, and the password can be
     * copied to clipboard.
     */
    private void keysScene() {
        // Create a list of keys and present them in a VBox with headers
        VBox keysVBox = new VBox(5);
        keysVBox.setPadding(new Insets(5, 5, 5, 5));
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER);
        Label websiteHeader = createHeaderLabel("Website");
        Label usernameHeader = createHeaderLabel("Username");
        Label passwordHeader = createHeaderLabel("Password");
        Label errorField = createErrorLabel("");
        headerBox.getChildren().addAll(websiteHeader, usernameHeader,
                                       passwordHeader, createHeaderLabel(""));
        keysVBox.getChildren().addAll(headerBox);

        for (String website : ManageCredentials.getWebsites()) {
            // Get the credentials for the website
            List<String> credentials = ManageCredentials.getCredentials(website);
            if (credentials == null) {
                errorField.setText("Error in getting the credentials.");
                continue;
            }

            // Create a button to copy the password to clipboard
            Button copyPasswordBtn = createSmallBtn("Copy");
            copyPasswordBtn.setOnAction(e -> {
                StringSelection stringSelection = new StringSelection(credentials.get(1));
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

                // Clear clipboard after 10 seconds
                PauseTransition pause = new PauseTransition(Duration.seconds(10));
                pause.setOnFinished(event -> {
                    StringSelection empty = new StringSelection("");
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(empty, null);
                });
                pause.play();
            });

            // Create a button to edit the keys
            Button editBtn = createSmallBtn("Edit");
            editBtn.setOnAction(e -> editKeyScene(website));

            HBox infoBox = new HBox(30, createLabel(website),
                                    createLabel(credentials.get(0)),
                                    copyPasswordBtn,
                                    editBtn);
            infoBox.setAlignment(Pos.CENTER);
            keysVBox.getChildren().addAll(infoBox);
        }

        // Set layout
        keysVBox.getChildren().add(errorField);
        Button returnBtn = GUIElements.createReturnBtn();
        returnBtn.setOnAction(e -> mainScene());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox keysTopBox = new HBox(20, returnBtn, spacer, logOutBtn);
        keysTopBox.setAlignment(Pos.TOP_LEFT);
        root.getChildren().clear();
        root.getChildren().addAll(keysTopBox, keysVBox);
    }

    /*
     * Creates the scene where a new key can be added.
     */
    private void addKeyScene() {
        // Create text fields
        TextField websiteField = new TextField();
        TextField usernameField = new TextField();
        TextField passLength = new TextField();
        passLength.setMaxWidth(50);
        PasswordField passField = new PasswordField();
        Button generatePass = createSmallBtn("Generate");
        Label errorField = createErrorLabel("");
        generatePass.setOnAction(e -> {generatePassword(passLength, passField, errorField);});

        Button addKeyBtn = createBigBtn("Add key");
        addKeyBtn.setOnAction(e -> {
            String website = websiteField.getText();
            String username = usernameField.getText();
            String password = passField.getText();
            String passwordStrength = UserAuthentication.checkPasswordStrenth(password);

            if (website.trim().isEmpty() || username.trim().isEmpty()) {
                errorField.setText("Please fill in all fields.");
            } else if (passwordStrength != null) {
                errorField.setText(passwordStrength);
            } else {
                // Add the key to the database
                if (!ManageCredentials.storeKey(website, username, password)) {
                    errorField.setText("Failed to add the key.");
                } else {
                    keysScene();
                }
            }
        });

        // Set layout
        Button returnBtn = GUIElements.createReturnBtn();
        returnBtn.setOnAction(e -> mainScene());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); 
        HBox addKeyTopBox = new HBox(20, returnBtn, spacer, logOutBtn);
        addKeyTopBox.setAlignment(Pos.TOP_LEFT);

        root.getChildren().clear();
        root.getChildren().addAll(addKeyTopBox, 
            createLabeledField("Website:", websiteField),
            createLabeledField("Username:", usernameField),
            createLabeledField("Password length:", passLength),
            createLabeledField("Password:", passField),
            generatePass,
            errorField,
            addKeyBtn);
    }

    /*
     * Creates the scene where a key can be edited.
     */
    private void editKeyScene(String website) {
        Label errorField = createErrorLabel("");
        // Get the credentials for the website
        List<String> credentials = ManageCredentials.getCredentials(website);
        // ERROR
        if (credentials == null) {
            errorField.setText("Error in getting the credentials.");
            return;
        }

        // Create text fields
        Label websiteField = createHeaderLabel(website);
        TextField usernameField = new TextField(credentials.get(0));
        TextField passLength = new TextField(String.valueOf(credentials.get(1).length()));
        passLength.setMaxWidth(60);
        PasswordField passField = new PasswordField();
        passField.setText(credentials.get(1));

        Button generatePass = createSmallBtn("Generate");
        generatePass.setOnAction(e -> {generatePassword(passLength, passField, errorField);});

        Button saveBtn = createBigBtn("Save");
        saveBtn.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passField.getText();

            // Check if the password is strong enough
            String passwordStrength = UserAuthentication.checkPasswordStrenth(password);

            // Check if the fields are empty
            if (username.trim().isEmpty() || password.trim().isEmpty()) {
                errorField.setText("Please fill in all fields.");
            } else if (passwordStrength != null) {
                errorField.setText(passwordStrength);           
            } else {
                // Update the credentials to the database
                if (!ManageCredentials.updateKey(website, username, password)) {
                    errorField.setText("Failed to update the key.");
                } else {
                    keysScene();
                }
            }
        });

        Button deleteBtn = createBigBtn("Delete");
        deleteBtn.setOnAction(e -> {
            // Create a confirmation dialog
            Stage confirmStage = new Stage();
            confirmStage.setTitle("Confirm Deletion");
            Label confirmLabel = createLabel("Are you sure you want to delete this key?");

            Button noBtn = createSmallBtn("No");
            noBtn.setOnAction(event -> confirmStage.close());
            // Create a button to confirm deletion
            Button yesBtn = createSmallBtn("Yes");
            yesBtn.setOnAction(event -> {
                confirmStage.close();
                // Delete the key from the database
                if (!ManageCredentials.deleteKey(website)) errorField.setText("Failed to delete the key.");
                else keysScene();
            });

            HBox btnBox = new HBox(20, noBtn, yesBtn);
            btnBox.setAlignment(Pos.CENTER);
            VBox confirmBox = new VBox(20, confirmLabel, btnBox);
            confirmBox.setPadding(new Insets(20));
            confirmBox.setAlignment(Pos.CENTER);
            confirmBox.setStyle("-fx-background-color: " + backgroundColor + ";");

            Scene confirmScene = new Scene(confirmBox, 300, 150);
            confirmStage.setScene(confirmScene);
            confirmStage.show();
        });

        // Set layout
        Button returnBtn = GUIElements.createReturnBtn();
        returnBtn.setOnAction(e -> keysScene());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox editKeyTopBox = new HBox(20, returnBtn, spacer, logOutBtn);
        editKeyTopBox.setAlignment(Pos.TOP_LEFT);

        root.getChildren().clear();
        root.getChildren().addAll(editKeyTopBox, 
            websiteField,
            createLabeledField("Username:", usernameField),
            createLabeledField("Password length:", passLength),
            createLabeledField("Password:", passField),
            generatePass,
            errorField,
            saveBtn,
            deleteBtn);
    }

    /*
     * Generates a password and sets it to the password field.
     */
    private void generatePassword(TextField passLength, PasswordField passField, Label errorField) {
        try {
            int length = Integer.parseInt(passLength.getText());
            passField.setText("");
            if (length < 8) {
                errorField.setText("Password length must be at least 8 characters.");
            } else if (length > 20) {
                errorField.setText("Password length must be at most 20 characters.");
            } else {
                passField.setText(ManageCredentials.generatePassword(length));
            }
        } catch (NumberFormatException ex) {
            errorField.setText("Please enter a valid number for password length.");
            passLength.clear();
        }
    }


    public static void main(String[] args) {
        // Initialize the database
        DatabaseHelper.initializeDatabase();

        launch();
    }
}
