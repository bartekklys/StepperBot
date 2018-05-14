package pl.bartekk.bot;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;

public class Controller {

    public Button button;
    public ComboBox portID;
    public Label statusLabel;

    public void handleConnectButtonClick() {

        if (statusLabel.getText().equals(ConnectionStatus.DISCONNECTED)) {
            button.setText("Disconnect");
            statusLabel.setTextFill(Paint.valueOf("GREEN"));
            portID.setDisable(true);
            statusLabel.setText(ConnectionStatus.CONNECTED);
        } else {
            button.setText("Connect");
            statusLabel.setTextFill(Paint.valueOf("RED"));
            portID.setDisable(false);
        }





        System.out.println(portID.getValue());
    }
}
