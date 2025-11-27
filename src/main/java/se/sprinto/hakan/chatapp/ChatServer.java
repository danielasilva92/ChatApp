package se.sprinto.hakan.chatapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private final int port;

    //trådsäker mängd med klienter
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("\uD83C\uDF38✨ Server startar på port " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // hitta en användare med medd
    public ClientHandler findUser(String username) {
        for (ClientHandler client : clients) {
            if (client.getUser() != null && client.getUser()
                    .getUsername().equalsIgnoreCase(username)) {
                return client;
            }
        }
        return null;
    }
        //broadcast till alla användare
    void broadcast(String message, ClientHandler sender) {
        System.out.println("Meddelande från " + sender.getUser().getUsername() + ": " + message);
        for (ClientHandler client : clients) {
        if (client != sender) {


            client.sendMessage(sender.getUser().getUsername() + ": " + message);
        }

        }
    }

    void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println(client.getUser().getUsername() + " kopplade från.");
    }


}

