package chatclient.view;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.print.PageLayout;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;


/**
 * Created by truesik on 06.09.2015.
 */
public class PrivateTab extends Tab {

    private VBox vBox;
    private HBox hBox;
    private TextArea textArea;
    private TextField messageField;

    private static Map<String, PrivateTab> tabs = new HashMap<>();

    public PrivateTab(String receiver, DataOutputStream outputStream) {
        super(receiver);
        tabs.put(receiver, this);
        textArea = new TextArea();
        messageField = new TextField();
        hBox = new HBox(messageField);
        vBox = new VBox(textArea, hBox);
        this.setContent(vBox);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        VBox.setVgrow(vBox, Priority.NEVER);
        HBox.setHgrow(messageField, Priority.ALWAYS);
        textArea.setEditable(false);
        this.setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                tabs.remove(receiver);
            }
        });
        messageField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    if (!messageField.getText().trim().isEmpty()) {
                        String message = "<prvt>" + "<" + receiver + ">" + messageField.getText();
                        textArea.appendText(messageField.getText() + "\n");
                        System.out.println(message);
                        try {
                            outputStream.writeUTF(message);
                            outputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        messageField.clear();
                    }
                }
            }
        });
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messageField.requestFocus();
            }
        });

    }

    public static PrivateTab getTabByUsername(String userName) {
        return tabs.get(userName);
    }

    public static Map<String, PrivateTab> getTabs() {
        return tabs;
    }

    public TextArea getTextArea() {
        return textArea;
    }
}
