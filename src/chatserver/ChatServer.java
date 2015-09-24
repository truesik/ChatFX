package chatserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatServer {
    static final Object monitor = new Object();
    private boolean isOn;

    public ChatServer(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket accept = serverSocket.accept();
                System.out.println("Accepted from " + accept.getInetAddress() + ":" + accept.getPort());
                User user = new User(accept);

                ExecutorService executorService = Executors.newCachedThreadPool();
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            isOn = true;
                            while (isOn) {
                                String msg = user.getDataInputStream().readUTF();
                                System.out.println(msg);
                                String patternString = "<(.*?)><(.*?)>(.*)";
                                Pattern pattern = Pattern.compile(patternString);
                                Matcher matcher = pattern.matcher(msg);
                                if (matcher.matches()) {
                                    String command = matcher.group(1);
                                    String receiver = matcher.group(2);
                                    String message = matcher.group(3);
                                    if (command.equals("exit")) {
                                        user.broadcastMessage("exited", user.getNickName());
                                        isOn = false;
                                        break;
                                    }
                                    if (command.equals("prvt")) {
                                        user.sendPrivateMessage(receiver, user.getNickName(), message);
                                    }
                                    if (command.equals("msg")) {
                                        user.broadcastMessage(message, user.getNickName());
                                    }
                                    if (command.equals("login")) {
                                        user.addUser(receiver);
                                        Thread.sleep(500);
                                        synchronized (monitor) {
                                            monitor.notifyAll();
                                        }
                                    }
                                }
//                                if (msg.equals("e<xit>")) {
//                                    break;
//                                }
//                                if (msg.substring(0, 6).equals("<prvt>")) {
//                                    String patternString = "<(.*?)><(.*?)>(.*)";
//                                    Pattern pattern = Pattern.compile(patternString);
//                                    Matcher matcher = pattern.matcher(msg);
//                                    matcher.matches();
//                                    user.sendPrivateMessage(matcher.group(2), user.getNickName(), matcher.group(3)); //todo: дописать
//                                }
//                                if (msg.contains("<msg>")) {
//                                    user.broadcastMessage(msg.substring(5), user.getNickName());
//                                }
//                                if (msg.contains("<login>")) {
//                                    user.addUser(msg.substring(7));
//                                    user.refreshNickNamesList();
//                                    user.setNickName(msg.substring(8));
//                                    Thread.sleep(500);
//                                    synchronized (monitor) {
//                                        monitor.notifyAll();
//                                    }
//                                }
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            System.out.println("finally");
                            try {
                                user.removeUser(user);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                user.outputStreamClose();
                                user.socketClose();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            synchronized (monitor) {
                                monitor.notifyAll();
                            }
                        }
                    }
                });

                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                synchronized (monitor) {
                                    monitor.wait();
                                }
                                user.refreshNickNamesList();
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        if (args.length != 1) {
//            throw new RuntimeException("Syntax: chatserver.ChatServer <port>");
//        }
        try {
            String args0 = "8082";
            new ChatServer(Integer.parseInt(args0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}