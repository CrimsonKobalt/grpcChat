package server;

import exceptions.WrongPassException;
import model.Message;
import model.User;
import model.UserSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerSystem {
    private final UserSystem userSystem;
    private final List<Message> messages;

    private final int defaultChatSize;

    public ServerSystem(String userSystemName, int size){
        this.userSystem = new UserSystem(userSystemName);
        this.messages = new ArrayList<>();
        this.defaultChatSize = size;
    }

    public ServerSystem(String userSystemName){
        this(userSystemName, 5);
    }

    public ServerSystem(int size){
        this.userSystem = new UserSystem();
        this.messages = new ArrayList<>();
        this.defaultChatSize = size;
    }

    public ServerSystem(){
        this(5);
    }

    public UserSystem getUserSystem(){
        return this.userSystem;
    }

    public void addMessage(String content, User sender){
        synchronized (this){
            try {
                messages.add(new Message(content, sender));
            } catch (Exception e){
                System.out.println("thread-error encountered. Message order might be corrupted.");
            }
        }
    }

    public User validateUser(String usn, String psw) throws WrongPassException {
        synchronized (this){
            return userSystem.validateUser(usn, psw);
        }
    }

    //return an iterator that returns the last "defaultChatSize" messages
    public synchronized Iterator<Message> getMessageIterator(){
        if(messages.size() > defaultChatSize) {
            return messages.listIterator(messages.size() - defaultChatSize);
        } else {
            return messages.listIterator(0);
        }
    }

    public synchronized void closeServer(){
        userSystem.exitUserSystem();
    }
}
