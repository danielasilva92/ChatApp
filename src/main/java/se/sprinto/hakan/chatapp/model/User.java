package se.sprinto.hakan.chatapp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private List<Message> messages = new ArrayList<>();

    public User() {
    }

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.passwordHash = password;
    }

    public User(String username, String password) {
        this.username = username;
        this.passwordHash = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return passwordHash;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void setPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}

