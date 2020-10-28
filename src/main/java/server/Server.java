package server;

import exceptions.WrongPassException;
import model.Message;
import model.User;
import model.UserSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server {
    private UserSystem userSystem;
    private List<Message> messages;

    public Server(String userSystemName){
        this.userSystem = new UserSystem(userSystemName);
        this.messages = new ArrayList<>();
    }

    public Server(){
        this.userSystem = new UserSystem();
        this.messages = new ArrayList<>();
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
            try {
                return userSystem.validateUser(usn, psw);
            } catch(Exception e){
                System.out.println("thread-error encountered. UserSystem might be corrupted.");
            }
        }
        return null;
    }

    public Iterator<Message> getMessageIterator(){
        if(messages.size() > 6) {
            return messages.listIterator(messages.size() - 5);
        } else {
            return messages.listIterator(0);
        }
    }
}
