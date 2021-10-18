package org.academiadecodigo.cachealots.concurrentchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Dispatcher implements Runnable {

    private String username;
    private final ChatServer server;
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Dispatcher(Socket clientSocket, ChatServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {

        try {

            setupClient();
            getInputToSend();

        } catch (IOException e) { System.out.println(e.getMessage()); }


    }

    private void setupClient() throws IOException {

        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream());

        out.print("Username: ");
        out.flush();
        username = in.readLine();

        if(username.equals("")) username = "user";

        out.print("Welcome, " + username + "! Check available commands with [/commands]!\n");
        out.flush();

        server.broadcast(username + " has joined the chat!\n");

    }

    private void getInputToSend() throws IOException {

        while (!clientSocket.isClosed()) {

            String message = in.readLine();

            if (message == null) {
                clientSocket.close();
                break;
            } else if (message.equals("")) continue;

            //if message is command i.e. starts with "/"
            if (message.charAt(0) == ("/".charAt(0))) {
                String[] command = message.split(" ");

                switch (command[0]) {

                    case "/commands" -> {
                        String commands =
                                "Available commands:\n" +
                                "[/name] Change name\n" +
                                "[/quit] Leave server\n" +
                                "[/list] Show connected users\n";
                        out.print(commands);
                        out.flush();
                    }

                    case "/name" -> {
                        if (command.length != 2) {
                            out.print("Usage: /name <new_username> | No spaces allowed!" + "\n");
                            out.flush();
                            continue;
                        }

                        server.broadcast(username + " changed username to " + command[1] + "\n");
                        username = command[1];
                    }

                    case "/quit" -> {
                        server.broadcast(username + " has left the server!" + "\n");
                        server.eject(this);
                        out.close();
                        in.close();
                        clientSocket.close();
                    }

                    case "/list" -> {
                        out.print(server.getUsers());
                        out.flush();
                    }

                    default -> {
                        out.print(command[0] + ": not a command " + "\n");
                        out.flush();
                    }
                }

            } else {

                server.broadcast(username + ": " + message + "\n");
            }
        }
        server.eject(this);
    }

    public String getDetails() {
        return username + " on connection " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
    }

    public String getUsername() {
        return username;
    }

    public void receiveMessage(String message) {
        out.print(message);
        out.flush();
    }

}
