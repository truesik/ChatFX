package chatclient;

import javafx.application.Application;
import javafx.stage.Stage;
import chatclient.view.ChatController;

/**
 * Created by truesik on 24.08.2015.
 */
public class ChatClient extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ChatController chatController = new ChatController();
        chatController.launchChat();
    }
}
