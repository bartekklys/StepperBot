package pl.bartekk.bot;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.ardulink.core.Link;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Controller {

    // FIXME: 14.05.2018
    private Link link;

    private static String FEM_FILE_PATH = "n/a";

    public MenuItem aboutItem;
    public Button button;
    public ComboBox portID;
    public CheckBox femCheckbox;
    public Button chooseFileButton;
    public Label motor1Label;
    public Label motor2Label;

    // slow start mode
    public CheckBox slowStartCheckBox;
    public TextField slowStartTextField;
    public Label slowStartModeLabel;

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

    public ProgressIndicator connectionProgressIndicator;

    public void handleSlowStartModeButton() {
        if (slowStartTextField.isDisabled()) {
            slowStartTextField.setDisable(false);
            slowStartModeLabel.setDisable(false);
        } else {
            slowStartTextField.setDisable(true);
            slowStartModeLabel.setDisable(true);
        }
    }

    public void handleFemCheckbox() {
        if (chooseFileButton.isDisabled()) {
            chooseFileButton.setDisable(false);
        } else {
            chooseFileButton.setDisable(true);
            FEM_FILE_PATH = "n/a";
            selectedFilePath.setText(FEM_FILE_PATH);
        }
    }

    public void handleCopyToMotor2Button() {
        if (!motor2Pane.isDisabled()) {
            motor2SpeedSlider.setValue(motor1SpeedSlider.getValue());
        }
    }

    public void handleCopyToMotor1Button() {
        // do not need to check if Motor 1 Pane i enabled (always is)
        motor1SpeedSlider.setValue(motor2SpeedSlider.getValue());
    }

    public void handleConnectButtonClick() {
        if (statusLabel.getText().equals(ConnectionStatus.DISCONNECTED)) {

            connectionProgressIndicator.setVisible(true);
            connectionProgressIndicator.setProgress(-1);

            String URI = "ardulink://serial-jssc?baudrate=9600&pingprobe=false&port=";
            String resultURI = URI.concat(portID.getValue().toString().replaceAll("\\s+", ""));
            try {
                //link = Links.getLink(URIs.newURI(resultURI));
            } catch (RuntimeException e) {
                showConnectionErrorMessageDialog();
                return;
            }
            connectionProgressIndicator.setProgress(1);
            statusLabel.setText(ConnectionStatus.CONNECTED);
            button.setText("Disconnect");
            statusLabel.setTextFill(Paint.valueOf("GREEN"));
            portID.setDisable(true);
            disableDashboard(false);
        } else if (statusLabel.getText().equals(ConnectionStatus.CONNECTED)) {
            link = null;
            connectionProgressIndicator.setVisible(false);
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
            selectedFilePath.setText(FEM_FILE_PATH);
            String content = "";
            try {
                content = new String(Files.readAllBytes(Paths.get(FEM_FILE_PATH)));
            } catch (IOException e) {
                // FIXME: 15.05.2018
                e.printStackTrace();
            }
            System.out.println(content);
        }

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
        RotateTransition rotateTransition1 = new RotateTransition(Duration.seconds(10), motor1Image);
        RotateTransition rotateTransition2 = new RotateTransition(Duration.seconds(10), motor2Image);
        rotateTransition1.setFromAngle(0);
        rotateTransition1.setToAngle(3600);
        rotateTransition2.setFromAngle(0);
        rotateTransition2.setToAngle(3600);
        if (rotateTransition1.getStatus() == Animation.Status.RUNNING) {
            rotateTransition1.pause();
            rotateTransition2.pause();
        } else {
            rotateTransition1.play();
            if (!motor2Pane.isDisabled()) {
                rotateTransition2.play();
            }
        }
    }

    public void start() {

        if (startButton.getText().equals("Start")) {
            startButton.setTextFill(Paint.valueOf("RED"));
            startButton.setText("Stop");
            motorPane.setDisable(true);
            motor1Image.setOpacity(0.25);
        } else if (startButton.getText().equals("Stop")) {
            startButton.setTextFill(Paint.valueOf("GREEN"));
            startButton.setText("Start");
            motorPane.setDisable(false);
            motor1Image.setOpacity(1);
        }
        System.out.println(motor1SpeedSlider.getValue());
    }

    public void selectSeparately() {
        separatelyBox.setSelected(true);
        collectivelyBox.setSelected(false);
        motor2Pane.setDisable(false);
        motor2Image.setOpacity(1);
        motor1Label.setText("Motor 1");
        motor2Label.setText("Motor 2");
    }

    public void selectCollectively() {
        collectivelyBox.setSelected(true);
        separatelyBox.setSelected(false);
        motor2Pane.setDisable(true);
        motor1Label.setText("Motor 1 & 2");
        motor2Label.setText("n/a");
    }

    boolean x = true;

    // test method, should be deleted
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
