package server;

import exceptions.WrongPassException;
import model.Message;
import model.PrivateChat;
import model.User;
import model.UserSystem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerSystem {
    private final UserSystem userSystem;
    private final List<Message> messages;
    private final int defaultChatSize;

    private final List<PrivateChat> privateChats;

    public ServerSystem(String userSystemName, int size){
        this.userSystem = new UserSystem(userSystemName);
        this.messages = new ArrayList<>();
        this.defaultChatSize = size;

        this.privateChats = new ArrayList<>();
    }

    public ServerSystem(String userSystemName){
        this(userSystemName, 10);
    }

    public ServerSystem(int size){
        this.userSystem = new UserSystem();
        this.messages = new ArrayList<>();
        this.defaultChatSize = size;

        this.privateChats = new ArrayList<>();
    }

    public ServerSystem(){
        this(10);
    }

    public UserSystem getUserSystem(){
        return this.userSystem;
    }

    public void addMessage(String content, User sender, Object newMessageMutex){
        synchronized (newMessageMutex){
            try {
                messages.add(new Message(content, sender));
                newMessageMutex.notifyAll();
            } catch (Exception e){
                System.out.println("thread-error encountered. Message order might be corrupted.");
            }
        }
    }

    public void addMessage(String content, String senderName, Object newMessageMutex) {
        synchronized (newMessageMutex){
            try {
                messages.add(new Message(content, senderName));
                newMessageMutex.notifyAll();
            } catch (Exception e){
                System.out.println("thread-error encountered. Message order might be corrupted.");
            }
        }
    }

    public void addNotification(String receiver, String content, Object newUserMutex){
        synchronized (newUserMutex){
            User user = this.userSystem.findUserByName(receiver);
            user.makeNotification(new Message(content, "SERVER"));
        }
    }

    public User validateUser(String usn, String psw, Object newUserMutex) throws WrongPassException {
            return userSystem.validateUser(usn, psw, newUserMutex);
    }

    public Iterator<User> getUserListIterator(){
        return this.userSystem.getUserListIterator();
    }

    public User getSyncUpdate(){
        return this.userSystem.getSyncUpdate();
    }

    //return an iterator that returns the last "defaultChatSize" messages
    public synchronized Iterator<Message> getMessageIterator(){
        if(messages.size() > defaultChatSize) {
            return messages.listIterator(messages.size() - defaultChatSize);
        } else {
            return messages.listIterator(0);
        }
    }

    public synchronized Message getFinalMessage(){
        return messages.get(messages.size()-1);
    }

    public synchronized void closeServer() {
        userSystem.exitUserSystem();
    }

    public PrivateChat findPrivateChat(User user1, User user2){
        synchronized (privateChats) {
            for (PrivateChat pc : privateChats) {
                if (pc.belongsTo(user1, user2)) {
                    return pc;
                }
            }
            PrivateChat result = new PrivateChat(user1, user2);
            privateChats.add(result);
            return result;
        }
    }
}
