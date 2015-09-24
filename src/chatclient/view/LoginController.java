package chatclient.view;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by truesik on 12.09.2015.
 */
public class LoginController {
    public TextField serverAddressField;
    public TextField serverPortField;
    public TextField nicknameField;
    public Button connectButton;
    private Parent root = null;
    private Stage stage;
    private ChatController chatController;

    public LoginController(ChatController chatController) {
        this.chatController = chatController;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
        fxmlLoader.setController(this);
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void launch() {
        stage = new Stage();
        if (root != null) {
            stage.setScene(new Scene(root));
        }
        stage.setTitle("Connect to...");
        stage.initOwner(chatController.getStage());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.show();
        nicknameField.requestFocus();
        connectButton.setDisable(true);
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                validate();
            }
        };
        nicknameField.textProperty().addListener(changeListener);
        serverAddressField.textProperty().addListener(changeListener);
        serverPortField.textProperty().addListener(changeListener);
    }

    private void validate() {
        connectButton.disableProperty().set(nicknameField.getText().trim().isEmpty() ||
                                            serverAddressField.getText().trim().isEmpty() ||
                                            serverPortField.getText().trim().isEmpty());
    }

    public void connectButtonHandle(ActionEvent actionEvent) throws IOException {
        chatController.connectToServer(serverAddressField.getText(), Integer.parseInt(serverPortField.getText()));
        chatController.setLogin(nicknameField.getText());
        stage.close();
    }

    public void cancelButtonHandle(ActionEvent actionEvent) {
        stage.close();
    }
}
