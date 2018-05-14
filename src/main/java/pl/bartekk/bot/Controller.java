package pl.bartekk.bot;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller {

    public MenuItem aboutItem;
    public Button button;
    public ComboBox portID;
    public Label statusLabel;
    public ImageView motor1;
    public Button rotateButton;

    public void handleConnectButtonClick() {
        if (statusLabel.getText().equals(ConnectionStatus.DISCONNECTED)) {
            statusLabel.setText(ConnectionStatus.CONNECTED);
            button.setText("Disconnect");
            statusLabel.setTextFill(Paint.valueOf("GREEN"));
            portID.setDisable(true);
        } else if (statusLabel.getText().equals(ConnectionStatus.CONNECTED)) {
            statusLabel.setText(ConnectionStatus.DISCONNECTED);
            button.setText("Connect");
            statusLabel.setTextFill(Paint.valueOf("RED"));
            portID.setDisable(false);
        }
        System.out.println(portID.getValue());
    }

    public void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.showOpenDialog(new Stage());
    }

    public void showAboutMessageDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("StepperBot Info");
        alert.setHeaderText("StepperBot v1");
        alert.setContentText("2018 Bartosz Kłys, WIMiIP\n" + "Email: klys.bartosz@gmail.com");
        alert.show();
    }

    public void rotateImage() {

        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(10), motor1);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(3600);
        if (rotateTransition.getStatus() == Animation.Status.RUNNING) {
            rotateTransition.pause();
        } else {
            rotateTransition.play();
        }
    }
}
