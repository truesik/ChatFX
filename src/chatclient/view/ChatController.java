package chatclient.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by truesik on 24.08.2015.
 */
public class ChatController {
    public ListView<String> nickNamesList;
    public TextArea textArea;
    public TextField messageField;
    public TabPane tabPane;
    public Button connectButton;
    public Button disconnectButton;
    public SplitPane splitPane;
    private PrivateTab privateTab;

    private Parent root = null;
    private Stage stage;

    private Socket socket = null;
    private Socket accept;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private static ObservableList<String> model = FXCollections.observableArrayList();
    private boolean isOn;

    public ChatController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Chat.fxml"));
        fxmlLoader.setController(this);
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectToServer(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        splitPane.setDisable(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messageField.requestFocus();
            }
        });
        isOn = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isOn) {
                        String inputLine = inputStream.readUTF();
                        System.out.println(inputLine);
                        String patternString = "<(.*?)><(.*?)>(.*)";
                        Pattern pattern = Pattern.compile(patternString);
                        Matcher matcher = pattern.matcher(inputLine);
                        if (matcher.matches()) {
                            String command = matcher.group(1);
                            String user = matcher.group(2);
                            String message = matcher.group(3);

                            if (command.equals("msg")) {
                                textArea.appendText(user + ": " + message + "\n");
                            }
                            if (command.equals("newuser")) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (model != null) {
                                            model.clear();
                                        }
                                    }
                                });
                            }
                            if (command.equals("user")) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        model.add(user);
                                    }
                                });
                            }
                            if (command.equals("prvt")) {
                                if (PrivateTab.getTabs().containsKey(user)) {
                                    PrivateTab openedPrivateTab = PrivateTab.getTabs().get(user);
                                    openedPrivateTab.getTextArea().appendText(user + ": " + message + "\n");
                                } else {
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            privateTab = new PrivateTab(user, outputStream);
                                            tabPane.getTabs().addAll(privateTab);
                                            privateTab.getTextArea().appendText(user + ": " + message + "\n");
                                        }
                                    });
                                }
                            }
                        }
//                        if (inputLine.substring(0, 5).equals("<msg>")) {
//                            textArea.appendText(inputLine.substring(5) + "\n");
//                        }
//                        if (inputLine.equals("<newuser>")) {
//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (model != null) {
//                                        model.clear();
//                                    }
//                                }
//                            });
//                        }
//                        if (inputLine.substring(0, 6).equals("<user>")) {
//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    model.add(inputLine.substring(6));
//                                }
//                            });
//                        }
//                        if (inputLine.substring(0, 6).equals("<prvt>")) {
//                            todo: дописать ивент о приходе приватного сообщения
//                            String patternString = "<(.*?)><(.*?)>(.*)";
//                            Pattern pattern = Pattern.compile(patternString);
//                            Matcher matcher = pattern.matcher(inputLine);
//                            matcher.matches();
//                            if (PrivateTab.getTabs().containsKey(matcher.group(2))) {
//                                PrivateTab openedPrivateTab = PrivateTab.getTabs().get(matcher.group(2));
//                                openedPrivateTab.getTextArea().appendText(matcher.group(2) + ": " + matcher.group(3) + "\n");
//                            } else {
//                                Platform.runLater(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        privateTab = new PrivateTab(matcher.group(2), outputStream);
//                                        tabPane.getTabs().addAll(privateTab);
//                                        tabPane.getSelectionModel().select(PrivateTab.getTabByUsername(matcher.group(2)));
//                                        privateTab.getTextArea().appendText(matcher.group(2) + ": " + matcher.group(3) + "\n");
//                                    }
//                                });

//                            }
//                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isOn = false;
                } finally {
                    try {
                        System.out.println("finally");
                        outputStream.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ServerSocket fileServerSocket = null;
//                try {
//                    fileServerSocket = new ServerSocket(8083);
//                    while (true) {
//                        accept = fileServerSocket.accept();
//
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }).start();
    }

    public void launchChat() {
        stage = new Stage();
        if (root != null) {
            stage.setScene(new Scene(root));
        }
        stage.setTitle("Chat");
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                isOn = false;
                try {
                    if (socket != null) {
                        if (!socket.isClosed()) {
                            outputStream.writeUTF("<exit><>");
                            outputStream.flush();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Platform.exit();
                System.exit(0);
            }
        });
        messageField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    if (!messageField.getText().trim().isEmpty()) {
                        String message = "<msg><>" + messageField.getText();
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


        nickNamesList.setItems(model);
        nickNamesList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> lv) {
                ListCell<String> cell = new ListCell<>();
                ContextMenu contextMenu = new ContextMenu();
                MenuItem privateMessage = new MenuItem("Private");
                privateMessage.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String item = cell.getItem();
                        if (!PrivateTab.getTabs().containsKey(item)) {
                            privateTab = new PrivateTab(item, outputStream);
                            tabPane.getTabs().addAll(privateTab);
                        }
                        tabPane.getSelectionModel().select(PrivateTab.getTabByUsername(item));
                    }
                });
                MenuItem sendFile = new MenuItem("Send file...");
                sendFile.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String item = cell.getItem();
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Выыберите фаил");
                        File file = fileChooser.showOpenDialog(stage);
                        Socket fileSendingSocket = null;
                        BufferedOutputStream bufferedOutputStream = null;
                        byte[] myByteArray = new byte[1024];
                        try {
                            fileSendingSocket = new Socket(item.substring(1, 10), 8083);
                            InputStream inputStream = fileSendingSocket.getInputStream();
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
//                            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));
                            int bytesRead = inputStream.read(myByteArray, 0, myByteArray.length);
//                            dataOutputStream.write(myByteArray, 0, myByteArray.length);
                            bufferedOutputStream.write(myByteArray, 0, myByteArray.length);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (bufferedOutputStream != null) {
                                try {
                                    bufferedOutputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (fileSendingSocket != null) {
                                try {
                                    fileSendingSocket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
                contextMenu.getItems().addAll(privateMessage, sendFile);

                cell.textProperty().bind(cell.itemProperty());
                cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                    if (isNowEmpty) {
                        cell.setContextMenu(null);
                    } else {
                        cell.setContextMenu(contextMenu);
                    }
                });
                return cell;
            }
        });
        splitPane.setDisable(true);
    }

    public void connectButtonHandle(ActionEvent actionEvent) {
        LoginController loginController = new LoginController(this);
        loginController.launch();
    }

    public void disconnectButtonHandle(ActionEvent actionEvent) {
        isOn = false;
        try {
            if (socket != null) {
                if (!socket.isClosed()) {
                    outputStream.writeUTF("<exit><>");
                    outputStream.flush();
                    model.clear();
                    splitPane.setDisable(true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void setLogin(String nickname) throws IOException {
        outputStream.writeUTF("<login>" + "<" + nickname + ">");
        outputStream.flush();
    }
}
