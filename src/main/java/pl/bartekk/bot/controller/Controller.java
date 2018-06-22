package pl.bartekk.bot.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.util.URIs;
import pl.bartekk.bot.util.Labels;
import pl.bartekk.bot.util.MotorIndicator;

public class Controller {

    //menu
    public MenuItem aboutItem;

    //control pane
    public Pane controlPane;
    public Label controlsLabel;
    public Label manualLabel;
    public RadioButton separatelyRadioButton;
    public RadioButton collectivelyRadioButton;
    public Label automaticLabel;
    public RadioButton femRadioButton;
    public CheckBox slowStartCheckBox;
    public TextField slowStartTextField;
    public Label slowStartModeLabel;
    public Button startButton;

    //motor 1 pane
    public Pane motorPane;
    public Label motor1Label;
    public ImageView motor1Image;
    public Slider motor1SpeedSlider;
    public TextField speed1TestField;
    public Button copyToMotor2Button;

    //motor 2 pane
    public Pane motor2Pane;
    public Label motor2Label;
    public ImageView motor2Image;
    public Slider motor2SpeedSlider;
    public TextField speed2TestField;
    public Button copyToMotor1Button;

    //summary pane
    public Pane summaryPane;
    public Label timeLabel;
    public ProgressIndicator connectionProgressIndicator;

    //connection pane
    public Button connectButton;
    public Label selectedFilePath;
    public ComboBox portId;
    public Button chooseFileButton;
    public Label statusLabel;

    // other
    private Link link;
    private static String FEM_FILE_PATH = "n/a";
    private RotateTransition rotateTransition1;
    private RotateTransition rotateTransition2;
    private ScheduledExecutorService executor;
    private File selectedFile;
    private String femFileContent;

    private void runFileChecker() {
        Runnable helloRunnable = () -> {
            String tempContent = getFemFileContent();
            if (!tempContent.equals(femFileContent)) {
                femFileContent = tempContent;
                try {
                    if (startButton.getText().equals(Labels.STOP)) {
                        setSpeedValueToSliders(femFileContent);
                        link.sendCustomMessage(femFileContent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, 1, TimeUnit.SECONDS);
    }

    private void setSpeedValueToSliders(String femFileContent) {
        if (!femFileContent.isEmpty()) {
            femFileContent = femFileContent.substring(0, femFileContent.indexOf(";"));
            String[] speedValues = femFileContent.split(",");
            Double firstMotorSpeed = Double.valueOf(speedValues[0]);
            motor1SpeedSlider.setValue(firstMotorSpeed);
            Double secondMotorSpeed = Double.valueOf(speedValues[1]);
            motor2SpeedSlider.setValue(secondMotorSpeed);
            if (firstMotorSpeed.intValue() == 0) {
                stopRotating(MotorIndicator.FIRST);
            }
            if (secondMotorSpeed.intValue() == 0) {
                stopRotating(MotorIndicator.SECOND);
            }
        }
    }

    private void stopFileChecker() {
        if (this.executor != null && !this.executor.isShutdown()) {
            this.executor.shutdown();
            this.executor = null;
        }
    }

    public void handleSlowStartModeButton() {
        if (slowStartTextField.isDisabled()) {
            slowStartTextField.setDisable(false);
            slowStartTextField.setOpacity(1);
            slowStartModeLabel.setOpacity(1);
        } else {
            slowStartTextField.setOpacity(0.25);
            slowStartModeLabel.setOpacity(0.25);
            slowStartTextField.setDisable(true);
            slowStartTextField.setText("0");
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
        if (statusLabel.getText().equals(Labels.DISCONNECTED)) {

            connectionProgressIndicator.setVisible(true);
            connectionProgressIndicator.setProgress(-1);

            String URI = "ardulink://serial-jssc?baudrate=9600&pingprobe=false&port=";
            String resultURI = URI.concat(portId.getValue().toString().replaceAll("\\s+", ""));
            try {
                link = Links.getLink(URIs.newURI(resultURI));
            } catch (RuntimeException e) {
                showConnectionErrorMessageDialog();
                return;
            }
            connectionProgressIndicator.setProgress(1);
            statusLabel.setText(Labels.CONNECTED);
            connectButton.setText("Disconnect");
            statusLabel.setTextFill(Paint.valueOf("GREEN"));
            portId.setDisable(true);
            disableDashboard(false);
        } else if (statusLabel.getText().equals(Labels.CONNECTED)) {
            link = null;
            connectionProgressIndicator.setVisible(false);
            statusLabel.setText(Labels.DISCONNECTED);
            connectButton.setText("Connect");
            statusLabel.setTextFill(Paint.valueOf("RED"));
            portId.setDisable(false);
            disableDashboard(true);
        }
    }

    private void disableDashboard(boolean disable) {

        double opacity = disable ? 0.25 : 1;

        controlPane.setDisable(disable);
        motorPane.setDisable(disable);
        summaryPane.setDisable(disable);
        motor1Image.setOpacity(opacity);
        motor2Image.setOpacity(opacity);
        separatelyRadioButton.setOpacity(opacity);
        collectivelyRadioButton.setOpacity(opacity);
        femRadioButton.setOpacity(opacity);
        slowStartCheckBox.setOpacity(opacity);
        controlsLabel.setOpacity(opacity);
        manualLabel.setOpacity(opacity);
        automaticLabel.setOpacity(opacity);
    }

    public void selectFemFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        selectedFile = fileChooser.showOpenDialog(new Stage());
        femFileContent = getFemFileContent();
    }

    private String getFemFileContent() {
        String content = "";
        if (selectedFile != null) {
            FEM_FILE_PATH = selectedFile.getAbsolutePath();
            try {
                selectedFilePath.setText(FEM_FILE_PATH);
                content = new String(Files.readAllBytes(Paths.get(FEM_FILE_PATH)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setSpeedValueToSliders(content);
        return content;
    }

    public void showAboutMessageDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("StepperBot Info");
        alert.setHeaderText("StepperBot v1");
        alert.setContentText("2018 Bartosz Kłys, WIMiIP\n" + "Email: klys.bartosz@gmail.com");
        alert.show();
    }

    private void showConnectionErrorMessageDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Connection error");
        alert.setHeaderText("StepperBot v1");
        alert.setContentText("Nie znaleziono podłączonego systemu Arduino.");
        alert.show();
    }

    private void rotateImage() {
        rotateTransition1 = new RotateTransition(Duration.seconds(6000000), motor1Image);
        rotateTransition2 = new RotateTransition(Duration.seconds(6000000), motor2Image);
        rotateTransition1.setToAngle(360000000);
        rotateTransition1.setInterpolator(Interpolator.LINEAR);
        rotateTransition2.setToAngle(360000000);
        rotateTransition2.setInterpolator(Interpolator.LINEAR);
        if (rotateTransition1.getStatus() == Animation.Status.RUNNING) {
            rotateTransition1.pause();
            rotateTransition2.pause();
        } else {
            int firstMotorSpeed = Double.valueOf(speed1TestField.getText()).intValue();
            int secondMotorSpeed;
            if (collectivelyRadioButton.isSelected()) {
                secondMotorSpeed = firstMotorSpeed;
            } else {
                secondMotorSpeed = Double.valueOf(speed2TestField.getText()).intValue();
            }
            rotate(firstMotorSpeed, rotateTransition1);
            rotate(secondMotorSpeed, rotateTransition2);
        }
    }

    private void rotate(int firstMotorSpeed, RotateTransition rotateTransition1) {
        if (firstMotorSpeed != 0) {
            if (firstMotorSpeed < 0) {
                rotateTransition1.setToAngle(rotateTransition1.getToAngle() * -1);
            } else {
                rotateTransition1.setToAngle(Math.abs(rotateTransition1.getToAngle()));
            }
            rotateTransition1.play();
        }
    }

    private void stopRotating(int motorIndicator) {
        switch (motorIndicator) {
            case 0:
                rotateTransition1.pause();
                break;
            case 1:
                rotateTransition2.pause();
                break;
            case 2:
                rotateTransition1.pause();
                rotateTransition2.pause();
                break;
        }
    }

    public void start() throws IOException {
        if (startButton.getText().equals("Start")) {
            if (collectivelyRadioButton.isSelected()) {
                int firstMotorSpeed = Double.valueOf(speed1TestField.getText()).intValue();
                String message = String.valueOf(firstMotorSpeed) + "," + firstMotorSpeed + ";" + Integer.valueOf(slowStartTextField.getText());
                System.out.println(message);
                link.sendCustomMessage(message.trim());
                motor2SpeedSlider.setValue(motor1SpeedSlider.getValue());
                rotateImage();

            } else if (separatelyRadioButton.isSelected()) {
                int firstMotorSpeed = Double.valueOf(speed1TestField.getText()).intValue();
                int secondMotorSpeed = Double.valueOf(speed2TestField.getText()).intValue();
                String message = firstMotorSpeed + "," + secondMotorSpeed + ";" + Integer.valueOf(slowStartTextField.getText());
                System.out.println(message);
                link.sendCustomMessage(message.trim());
                rotateImage();
            } else if (femRadioButton.isSelected()) {
                link.sendCustomMessage(getFemFileContent().trim());
                rotateImage();
            }
            startButton.setTextFill(Paint.valueOf("RED"));
            startButton.setText("Stop");
            motorPane.setDisable(true);
            disableControls(true);
        } else if (startButton.getText().equals("Stop")) {
            String stopMessage = "0,0;0";
            System.out.println(stopMessage);
            link.sendCustomMessage(stopMessage.trim());
            startButton.setTextFill(Paint.valueOf("GREEN"));
            startButton.setText(Labels.START);
            motorPane.setDisable(false);
            disableControls(false);
            stopRotating(MotorIndicator.BOTH);
        }
    }

    private void disableControls(boolean disabled) {

        double opacity = disabled ? 0.25 : 1;

        controlsLabel.setDisable(disabled);
        controlsLabel.setOpacity(opacity);
        manualLabel.setDisable(disabled);
        manualLabel.setOpacity(opacity);
        automaticLabel.setDisable(disabled);
        automaticLabel.setOpacity(opacity);
        slowStartCheckBox.setDisable(disabled);
        slowStartCheckBox.setOpacity(opacity);
        separatelyRadioButton.setDisable(disabled);
        separatelyRadioButton.setOpacity(opacity);
        collectivelyRadioButton.setDisable(disabled);
        collectivelyRadioButton.setOpacity(opacity);
        femRadioButton.setDisable(disabled);
        femRadioButton.setOpacity(opacity);
    }

    public void selectSeparately() {
        separatelyRadioButton.setSelected(true);
        collectivelyRadioButton.setSelected(false);
        femRadioButton.setSelected(false);
        chooseFileButton.setDisable(true);
        FEM_FILE_PATH = "n/a";
        selectedFilePath.setText(FEM_FILE_PATH);
        stopFileChecker();
        motor2Pane.setDisable(false);
        motor2Image.setOpacity(1);
        motor1Label.setText(Labels.MOTOR_1);
        motor2Label.setText(Labels.MOTOR_2);
        copyToMotor1Button.setDisable(false);
        copyToMotor2Button.setDisable(false);
        slowStartCheckBox.setDisable(false);
        slowStartCheckBox.setOpacity(1);
        motor1SpeedSlider.setDisable(false);
        motor2SpeedSlider.setDisable(false);
        speed1TestField.setEditable(true);
        speed2TestField.setEditable(true);
    }

    public void selectCollectively() {
        collectivelyRadioButton.setSelected(true);
        separatelyRadioButton.setSelected(false);
        femRadioButton.setSelected(false);
        chooseFileButton.setDisable(true);
        FEM_FILE_PATH = "n/a";
        selectedFilePath.setText(FEM_FILE_PATH);
        stopFileChecker();
        motor2Pane.setDisable(true);
        motor1Label.setText(Labels.MOTOR_1_2);
        motor2Label.setText("n/a");
        copyToMotor2Button.setDisable(true);
        slowStartCheckBox.setDisable(false);
        slowStartCheckBox.setOpacity(1);
        motor1SpeedSlider.setDisable(false);
        motor2SpeedSlider.setDisable(false);
        speed1TestField.setEditable(true);
        speed2TestField.setEditable(true);
    }

    public void handleFemRadioButton() {
        if (chooseFileButton.isDisabled()) {
            chooseFileButton.setDisable(false);
            runFileChecker();
        }
        slowStartCheckBox.setSelected(false);
        slowStartCheckBox.setDisable(true);
        slowStartCheckBox.setOpacity(0.25);
        slowStartTextField.setOpacity(0.25);
        slowStartModeLabel.setOpacity(0.25);
        slowStartTextField.setDisable(true);
        slowStartTextField.setText("0");
        femRadioButton.setSelected(true);
        separatelyRadioButton.setSelected(false);
        collectivelyRadioButton.setSelected(false);
        motor2Pane.setDisable(false);
        motor2Image.setOpacity(1);
        motor1Label.setText(Labels.MOTOR_1);
        motor2Label.setText(Labels.MOTOR_2);
        copyToMotor1Button.setDisable(true);
        copyToMotor2Button.setDisable(true);
        motor1SpeedSlider.setDisable(true);
        motor2SpeedSlider.setDisable(true);
        speed1TestField.setEditable(false);
        speed2TestField.setEditable(false);
    }
}
