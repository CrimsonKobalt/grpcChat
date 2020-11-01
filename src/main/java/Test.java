import exceptions.WrongPassException;
import model.Message;
import model.User;
import server.ServerSystem;

import java.util.Iterator;

public class Test {
    //testfile aanpassing
    public static void main(String[] args) throws WrongPassException {
        System.out.println("Commencing test...");
        ServerSystem serverSystem = new ServerSystem("testServer");
        Object mutex = new Object();
        Object msgMutex = new Object();

        User user1 = serverSystem.validateUser("new user3", "password", mutex);

        try {
            serverSystem.validateUser("new user1", "p4ssw0rd", mutex);
        } catch (WrongPassException wpe){
            System.out.println("wrong password caught correctly.");
        }

        try {
            User user2 = serverSystem.validateUser("new user1", "password", mutex);
            if(user1.equals(user2)){
                System.out.println("correctly validated a user.");
            }
        } catch (WrongPassException wpe) {
            System.out.println("should never arrive here.");
        }

        //commencing testing of messaging system...
        System.out.println("---------------");
        System.out.println("commencing testing of messaging system...");
        serverSystem.addMessage("Message1", user1, msgMutex);
        serverSystem.addMessage("Message2", user1, msgMutex);
        serverSystem.addMessage("Message3", user1, msgMutex);
        serverSystem.addMessage("Message4", user1, msgMutex);

        Iterator<Message> msgIt = serverSystem.getMessageIterator();
        System.out.println("printing first 4 messages: ");

        while(msgIt.hasNext()){
            System.out.println("\t" + msgIt.next());
        }

        serverSystem.addMessage("Message5", user1, msgMutex);

        msgIt = serverSystem.getMessageIterator();
        System.out.println("printing first 5 messages: ");
        while(msgIt.hasNext()){
            System.out.println("\t" + msgIt.next());
        }

        serverSystem.addMessage("Message6", user1, msgMutex);

        msgIt = serverSystem.getMessageIterator();
        System.out.println("should now that startSize is exceeded be printing last 5 messages: ");
        while(msgIt.hasNext()){
            System.out.println("\t" + msgIt.next());
        }

        System.out.println("---------------");
        System.out.println("commencing testing of sync system...");
        System.out.println("last registered user: " + serverSystem.getSyncUpdate().getName());
        System.out.println("last message sent: " + serverSystem.getFinalMessage());
        System.out.println("adding new message...");
        serverSystem.addMessage("Message7", user1, msgMutex);
        System.out.println("last message sent: " + serverSystem.getFinalMessage());

        System.out.println("---------------");
        System.out.println("commencing shutdown test...");
        serverSystem.closeServer();
    }
}
