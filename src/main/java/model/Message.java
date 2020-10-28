package model;

import java.time.LocalDateTime;

public class Message {
    private String content;
    private User sender;
    private LocalDateTime postTime;

    public Message(String content, User user, LocalDateTime time){
        this.content = content;
        this.sender = user;
        this.postTime = time;
    }

    public Message(String content, User user){
        this(content, user, LocalDateTime.now());
    }

    @Override
    public String toString(){
        return content+","+sender.getName()+","+postTime.toString();
    }
}
