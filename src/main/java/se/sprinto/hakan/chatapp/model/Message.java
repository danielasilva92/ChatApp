package se.sprinto.hakan.chatapp.model;

import java.time.LocalDateTime;

public class Message {
    private int id;
    //använder endast Id här, eftersom hela Usern inte behövs
    private int userId;
    private String text;
    private LocalDateTime timestamp;

    public Message(int userId, String text, LocalDateTime timestamp) {
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;

    }
    public int getUserId() {
        return userId;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }


    public String getText() {

    return text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

