package pl.bartekk.bot;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Controller {

    public Button button;
    public ComboBox portID;
    public Label statusLabel;

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
}
