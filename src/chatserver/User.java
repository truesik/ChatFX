package chatserver;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

/**
 * Created by truesik on 04.09.2015.
 */
public class User {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String nickName;
    private Boolean isLogined = false;
    private static Set<User> onlineUsers = Collections.synchronizedSet(new HashSet<User>());
//    private static Map<String, User> onlineUsers = Collections.synchronizedMap(new HashMap<String, User>());

    public User(Socket s) throws IOException {
        socket = s;
        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void addUser(String nickName) {
        onlineUsers.add(this);
        setNickName(nickName);
//        isLogined = true;
    }

    public void removeUser(User user) throws IOException {
        onlineUsers.remove(user);
        isLogined = false;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
        isLogined = true;
    }

    public String getNickName() {
        return nickName;
    }

//    public static Set<User> getOnlineUsers() {
//        return onlineUsers;
//    }


    public void broadcastMessage(String msg, String nickName) throws IOException {
        if (isLogined) {
            for (User user : onlineUsers) {
                user.dataOutputStream.writeUTF("<msg>" + "<" + nickName + ">" + msg);
                user.dataOutputStream.flush();
            }
        }
    }

    public void refreshNickNamesList() throws IOException {
        if (isLogined) {
            dataOutputStream.writeUTF("<newuser><>");
            dataOutputStream.flush();
            if (!onlineUsers.isEmpty()) {
                for (User user : onlineUsers) {
                    dataOutputStream.writeUTF("<user>" + "<" + user.getNickName() + ">");
                    dataOutputStream.flush();
                }
            }
        }
    }

    public void outputStreamClose() throws IOException {
        this.dataOutputStream.close();
    }

    public void socketClose() throws IOException {
        socket.close();
    }

    public void setIsLogined(Boolean isLogined) {
        this.isLogined = isLogined;
    }

    public void sendPrivateMessage(String receiver, String sender, String message) throws IOException {
        if (isLogined) {
            for (User onlineUser : onlineUsers) {
                if (onlineUser.nickName.equals(receiver)) {
                    onlineUser.dataOutputStream.writeUTF("<prvt>" + "<" + sender + ">" + message);
                    onlineUser.dataOutputStream.flush();
                }
            }
        }
    }
}
