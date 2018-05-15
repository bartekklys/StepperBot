package pl.bartekk.bot;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.ardulink.core.Link;

import java.io.File;
import java.io.IOException;

public class Controller {

    // FIXME: 14.05.2018
    private Link link;


    private static String FEM_FILE_PATH = "n/a";

    public MenuItem aboutItem;
    public Button button;
    public ComboBox portID;

    public ComboBox motor1DirectionComboBox;
    public ComboBox motor2DirectionComboBox;

    public Label statusLabel;
    public Button rotateButton;

    // motor speed
    public Slider motor1SpeedSlider;
    public Slider motor2SpeedSlider;

    public Button startButton;
    public Button copyToMotor2Button;

    // images
    public ImageView motor1Image;
    public ImageView motor2Image;

    public CheckBox separatelyBox;
    public CheckBox collectivelyBox;

    public Pane motor2Pane;
    public Label timeLabel;
    public Label selectedFilePath;

    // pane
    public Pane controlPane;
    public Pane motorPane;
    public Pane summaryPane;

    public void handleCopyToMotor2Button() {
        if (!motor2Pane.isDisabled()) {
            motor2DirectionComboBox.setValue(motor1DirectionComboBox.getValue());
            motor2SpeedSlider.setValue(motor1SpeedSlider.getValue());
        }
    }

    public void handleCopyToMotor1Button() {
        // do not need to check if Motor 1 Pane i enabled (always is)
        motor1DirectionComboBox.setValue(motor2DirectionComboBox.getValue());
        motor1SpeedSlider.setValue(motor2SpeedSlider.getValue());
    }

    public void handleConnectButtonClick() {
        if (statusLabel.getText().equals(ConnectionStatus.DISCONNECTED)) {

            String URI = "ardulink://serial-jssc?baudrate=9600&pingprobe=false&port=";
            String resultURI = URI.concat(portID.getValue().toString().replaceAll("\\s+", ""));
            try {
                //link = Links.getLink(URIs.newURI(resultURI));
            } catch (RuntimeException e) {
                showConnectionErrorMessageDialog();
                return;
            }
            statusLabel.setText(ConnectionStatus.CONNECTED);
            button.setText("Disconnect");
            statusLabel.setTextFill(Paint.valueOf("GREEN"));
            portID.setDisable(true);
            disableDashboard(false);
        } else if (statusLabel.getText().equals(ConnectionStatus.CONNECTED)) {
            link = null;
            statusLabel.setText(ConnectionStatus.DISCONNECTED);
            button.setText("Connect");
            statusLabel.setTextFill(Paint.valueOf("RED"));
            portID.setDisable(false);
            disableDashboard(true);
        }
        System.out.println(portID.getValue());
    }

    private void disableDashboard(boolean disable) {

        double opacity = disable ? 0.25 : 1;

        controlPane.setDisable(disable);
        motorPane.setDisable(disable);
        summaryPane.setDisable(disable);
        motor1Image.setOpacity(opacity);
        motor2Image.setOpacity(opacity);
    }

    public void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            FEM_FILE_PATH = selectedFile.getAbsolutePath();
        }
        selectedFilePath.setText(FEM_FILE_PATH);
    }

    public void showAboutMessageDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("StepperBot Info");
        alert.setHeaderText("StepperBot v1");
        alert.setContentText("2018 Bartosz Kłys, WIMiIP\n" + "Email: klys.bartosz@gmail.com");
        alert.show();
    }

    public void showConnectionErrorMessageDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Connection error");
        alert.setHeaderText("StepperBot v1");
        alert.setContentText("Nie znaleziono podłączonego systemu Arduino.");
        alert.show();
    }

    public void rotateImage() {

        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(10), motor1Image);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(3600);
        if (rotateTransition.getStatus() == Animation.Status.RUNNING) {
            rotateTransition.pause();
        } else {
            rotateTransition.play();
        }
    }

    public void start() {

        if (startButton.getText().equals("Start")) {
            startButton.setTextFill(Paint.valueOf("RED"));
            startButton.setText("Stop");
            motorPane.setDisable(true);
        } else if (startButton.getText().equals("Stop")) {
            startButton.setTextFill(Paint.valueOf("GREEN"));
            startButton.setText("Start");
            motorPane.setDisable(false);
        }
        System.out.println(motor1DirectionComboBox.getValue());
        System.out.println(motor1SpeedSlider.getValue());
    }

    public void selectSeparately() {
        separatelyBox.setSelected(true);
        collectivelyBox.setSelected(false);
        motor2Pane.setDisable(false);
    }

    public void selectCollectively() {
        collectivelyBox.setSelected(true);
        separatelyBox.setSelected(false);
        motor2Pane.setDisable(true);
    }

    boolean x = true;

    public void testMethod() throws IOException {
        if (x) {
            if (link != null) {
                link.sendCustomMessage("1");
            }
            x = false;
        } else {
            if (link != null) {
                link.sendCustomMessage("0");
            }
            x = true;
        }
    }
}
