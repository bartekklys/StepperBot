package pl.bartekk.bot;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    // FIXME: 14.05.2018
    private Link link;

    private static String FEM_FILE_PATH = "n/a";

    public MenuItem aboutItem;
    public Button button;
    public ComboBox portID;
    public Button chooseFileButton;
    public Label motor1Label;
    public Label motor2Label;
    public TextField speed1TestField;
    public TextField speed2TestField;
    public Label manualLabel;
    public Label automaticLabel;
    public Label modeLabel;
    public Label controlsLabel;

    // slow start mode
    public CheckBox slowStartCheckBox;
    public TextField slowStartTextField;
    public Label slowStartModeLabel;

    public Label statusLabel;

    // motor speed
    public Slider motor1SpeedSlider;
    public Slider motor2SpeedSlider;

    public Button startButton;
    public Button copyToMotor1Button;
    public Button copyToMotor2Button;

    // images
    public ImageView motor1Image;
    public ImageView motor2Image;

    public RadioButton separatelyRadioButton;
    public RadioButton collectivelyRadioButton;
    public RadioButton femRadioButton;

    public Pane motor2Pane;
    public Label timeLabel;
    public Label selectedFilePath;

    // pane
    public Pane controlPane;
    public Pane motorPane;
    public Pane summaryPane;

    private RotateTransition rotateTransition1;
    private RotateTransition rotateTransition2;

    public ProgressIndicator connectionProgressIndicator;

    private Runnable helloRunnable;
    private ScheduledExecutorService executor;
    private File selectedFile;
    private String femFileContent;

    private void runFileChecker() {
        this.helloRunnable = () -> {
            System.out.println("SPRAWDZAM PLIK");
            String tempContent = getFemFileContent();
            if (!tempContent.equals(femFileContent)) {
                femFileContent = tempContent;
                try {
                    if (startButton.getText().equals("Stop")) {
                        setSpeedValueToSliders(femFileContent);
                        link.sendCustomMessage(femFileContent);
                        System.out.println("Wyslalem: " + femFileContent);
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
        if (statusLabel.getText().equals(ConnectionStatus.DISCONNECTED)) {

            connectionProgressIndicator.setVisible(true);
            connectionProgressIndicator.setProgress(-1);

            String URI = "ardulink://serial-jssc?baudrate=9600&pingprobe=false&port=";
            String resultURI = URI.concat(portID.getValue().toString().replaceAll("\\s+", ""));
            try {
                link = Links.getLink(URIs.newURI(resultURI));
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
        separatelyRadioButton.setOpacity(opacity);
        collectivelyRadioButton.setOpacity(opacity);
        femRadioButton.setOpacity(opacity);
        slowStartCheckBox.setOpacity(opacity);
        controlsLabel.setOpacity(opacity);
        manualLabel.setOpacity(opacity);
        automaticLabel.setOpacity(opacity);
        modeLabel.setOpacity(opacity);
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
                // FIXME: 15.05.2018
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

    public void showConnectionErrorMessageDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Connection error");
        alert.setHeaderText("StepperBot v1");
        alert.setContentText("Nie znaleziono podłączonego systemu Arduino.");
        alert.show();
    }

    public void rotateImage() {
        rotateTransition1 = new RotateTransition(Duration.seconds(60), motor1Image);
        rotateTransition2 = new RotateTransition(Duration.seconds(60), motor2Image);
        rotateTransition1.setToAngle(3600);
        rotateTransition1.setInterpolator(Interpolator.LINEAR);
        rotateTransition2.setToAngle(3600);
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
            if (firstMotorSpeed != 0) {
                if (firstMotorSpeed < 0) {
                    rotateTransition1.setToAngle(rotateTransition1.getToAngle() * -1);
                } else {
                    rotateTransition1.setToAngle(Math.abs(rotateTransition1.getToAngle()));
                }
                rotateTransition1.play();
            }
            if (secondMotorSpeed != 0) {
                if (secondMotorSpeed < 0) {
                    rotateTransition2.setToAngle(rotateTransition2.getToAngle() * -1);
                } else {
                    rotateTransition2.setToAngle(Math.abs(rotateTransition2.getToAngle()));
                }
                rotateTransition2.play();
            }
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
            startButton.setText("Start");
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
        modeLabel.setDisable(disabled);
        modeLabel.setOpacity(opacity);
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
        motor1Label.setText("Motor 1");
        motor2Label.setText("Motor 2");
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
        motor1Label.setText("Motor 1 & 2");
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
        motor1Label.setText("Motor 1");
        motor2Label.setText("Motor 2");
        copyToMotor1Button.setDisable(true);
        copyToMotor2Button.setDisable(true);
        motor1SpeedSlider.setDisable(true);
        motor2SpeedSlider.setDisable(true);
        speed1TestField.setEditable(false);
        speed2TestField.setEditable(false);
    }

    // TODO: 23.05.2018 test method, should be deleted
    public void testMethod() throws IOException {
        System.out.println("TEST OK");
        link.sendCustomMessage("500,500;0");
    }
}
