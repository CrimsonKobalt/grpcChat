package model;

import java.time.LocalDateTime;

public class Message {
    private final String content;
    private final User sender;
    private final LocalDateTime postTime;
    private final String username;

    public Message(String content, User user, LocalDateTime time){
        this.content = content;
        this.sender = user;
        this.username = user.getName();
        this.postTime = time;
    }

    public Message(String content, String username, LocalDateTime time){
        this.username = username;
        this.content = content;
        this.sender = null;
        this.postTime = time;
    }

    public Message(String content, String username){
        this(content, username, LocalDateTime.now());
    }

    public Message(String content, User user){
        this(content, user, LocalDateTime.now());
    }

    @Override
    public String toString(){
        return content+","+sender.getName()+","+postTime.toString();
    }

    public String getContent(){
        return this.content;
    }

    public String getSenderName(){
        return this.username;
    }

    public String format(){
        return this.username+": "+this.content+"\n";
    }
}
