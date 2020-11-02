package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PrivateChat {
    private final List<User> users;
    private final List<Message> messages;

    public PrivateChat(User user1, User user2){
        this.users = new ArrayList<>(2);
        this.users.add(user1);
        this.users.add(user2);

        this.messages = new ArrayList<>();
    }

    public void addMessage(String content, User sender){
        synchronized (messages){
            messages.add(new Message(content, sender));
            messages.notifyAll();
        }
    }

    public void addMessage(Message message){
        synchronized (messages){
            messages.add(message);
            messages.notifyAll();
        }
    }

    public Iterator<Message> getMessagesIterator(){
        return this.messages.listIterator();
    }

    public boolean belongsTo(User user1, User user2){
        return (users.contains(user1) && users.contains(user2));
    }

    public List<Message> getMessagesMutex(){
        return this.messages;
    }

    public synchronized Message getNewMessage(){
        synchronized (this.messages) {
            return this.messages.get(this.messages.size() - 1);
        }
    }
}
