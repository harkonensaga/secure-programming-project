package fi.tuni.secprog.passwordmanager;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.io.File;
import javafx.geometry.Pos;


public class GUIElements {

    private static final String btnColor = "#76BCBC";
    private static final String btnHoverColor = "#69B5B5";
    private static final String textColor = "#F2F4F3";


    /*
     * Sets the style for the button. The button has a hover effect.
     */
    private static void setBtnStyle(Button btn, int fontSize) {
        btn.setStyle("-fx-font-size: " + fontSize + "pt;" +
                     "-fx-background-color: " + btnColor + ";" +
                     "-fx-border-radius: 20%;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-font-size: " + fontSize + "pt;" +
                                                "-fx-background-color: " + btnHoverColor + ";" +
                                                "-fx-border-radius: 20%;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-font-size: " + fontSize + "pt;" +
                                               "-fx-background-color: " + btnColor + ";" +
                                               "-fx-border-radius: 20%;"));
    }
    
    /*
     * Creates a header type label with the given text.
     */
    public static Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12pt;" +
                       "-fx-alignment: center;" +
                       "-fx-text-fill: " + textColor + ";" +
                       "-fx-font-weight: bold;");
        label.setPrefSize(200, 25);
        return label;
    }

    /*
     * Creates a label with the given text.
     */
    public static Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 10pt;" +
                       "-fx-alignment: CENTER_LEFT;" +
                       "-fx-text-fill: " + textColor + ";");
        label.setMinSize(80, 15);
        return label;
    }

    /*
     * Creates error label with the given text.
     */
    public static Label createErrorLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 10pt;" +
                       "-fx-alignment: center;" +
                       "-fx-text-fill: " + textColor + ";");
        label.setWrapText(true);
        return label;
    }

    /*
     * Creates a big button with the given text.
     */
    public static Button createBigBtn(String text) {
        Button bigBtn = new Button(text);
        setBtnStyle(bigBtn, 16);
        bigBtn.setPrefSize(200, 50);
        return bigBtn;
    }

    /*
     * Creates a small button with the given text.
     */
    public static Button createSmallBtn(String text) {
        Button smallBtn = new Button(text);
        setBtnStyle(smallBtn, 10);
        smallBtn.setMinSize(60, 15);
        return smallBtn;
    }

    /*
     * Creates a return button.
     */
    public static Button createReturnBtn() {
        Button returnBtn = new Button("↩");
        returnBtn.setPrefSize(35, 35);
        setBtnStyle(returnBtn, 12);
        return returnBtn;
    }

    /*
     * Creates a label and a field with the given label text and text field.
     */
    public static VBox createLabeledField(String labelText, TextField textField) {
        Label label = createLabel(labelText);
        VBox vbox = new VBox(5, label, textField);
        vbox.setAlignment(Pos.TOP_LEFT);
        return vbox;
    }

    

    /**
     * Gets the png image and returns the image.
     */
    public static ImageView getIcon(String iconName) {
        try {
            Image iconImage = new Image(new File(String.format("icons/%s.png", iconName)).toURI().toString());
            ImageView icon = new ImageView(iconImage);
            icon.setFitWidth(150);
            icon.setFitHeight(150);
            return icon;
        } catch (Exception e) {
            System.out.println("Error: Can't open icon picture.");
            return null;
        }
    }
}