package org.academiadecodigo.cachealots.concurrentchat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Multi-threaded tcp chat server that responds to client commands
 */
public class ChatServer {

     //Port to run the server on
    private final int PORT = 8080;

    //Max number of users
    private final int threadNumb = 12;

    private ServerSocket serverSocket;
    private final List<Dispatcher> clientList;


    public static void main(String[] args) {

        new ChatServer();
    }


    public ChatServer() {

        clientList = Collections.synchronizedList(new ArrayList<>());

        try {

            serverSocket = new ServerSocket(PORT);
            initServer();
            listenForConnections();

        } catch (IOException e) { System.out.println(e.getMessage()); }
    }

    // Starts handling messages
    public void initServer() {

        System.out.println("Starting Server on port " + PORT + "…");
    }

    /**
     * Starts the chat server on a specified port
     *
     */
    public void listenForConnections() throws IOException {


        ExecutorService threadPool = Executors.newFixedThreadPool(threadNumb);

        Socket clientSocket;

        while(true){

            //listen for connections
            clientSocket = serverSocket.accept(); //blocks

            //create new Dispatcher (Runnable) object
            Dispatcher client = new Dispatcher(clientSocket, this);

            //add to LinkedList
            clientList.add(client);

            //submit task to thread pool
            threadPool.submit(client);

        }
    }

    /**
     * Broadcast a message to all server connected clients
     *
     * @param message the message to be sent to all
     */
    public void broadcast(String message) {

        //for every connected client
        for (Dispatcher client : clientList){

            //everyone receives the message
            client.receiveMessage(message);

        }
    }


    public void eject(Dispatcher dispatcher) {

        //ejects user and saves boolean return
        boolean success = clientList.remove(dispatcher);

        //temp check for errors: pipes back any error
        if(!success) dispatcher.receiveMessage("error ejecting form server");
    }


    public String getUsers(){

        StringBuilder userList = new StringBuilder("[Connected users:] " + "\n");

        synchronized (clientList) {

            for (Dispatcher client : clientList) {
                userList.append(client.getDetails()).append("\n");
            }
        }

        return userList.toString();
    }



}





