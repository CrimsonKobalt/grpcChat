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

        User user1 = serverSystem.validateUser("new user3", "password");

        try {
            serverSystem.validateUser("new user1", "p4ssw0rd");
        } catch (WrongPassException wpe){
            System.out.println("wrong password caught correctly.");
        }

        try {
            User user2 = serverSystem.validateUser("new user1", "password");
            if(user1.equals(user2)){
                System.out.println("correctly validated a user.");
            }
        } catch (WrongPassException wpe) {
            System.out.println("should never arrive here.");
        }

        //commencing testing of messaging system...
        System.out.println("---------------");
        System.out.println("commencing testing of messaging system...");
        serverSystem.addMessage("Message1", user1);
        serverSystem.addMessage("Message2", user1);
        serverSystem.addMessage("Message3", user1);
        serverSystem.addMessage("Message4", user1);

        Iterator<Message> msgIt = serverSystem.getMessageIterator();
        System.out.println("printing first 4 messages: ");

        while(msgIt.hasNext()){
            System.out.println("\t" + msgIt.next());
        }

        serverSystem.addMessage("Message5", user1);

        msgIt = serverSystem.getMessageIterator();
        System.out.println("printing first 5 messages: ");
        while(msgIt.hasNext()){
            System.out.println("\t" + msgIt.next());
        }

        serverSystem.addMessage("Message6", user1);

        msgIt = serverSystem.getMessageIterator();
        System.out.println("should now that startSize is exceeded be printing last 5 messages: ");
        while(msgIt.hasNext()){
            System.out.println("\t" + msgIt.next());
        }

        serverSystem.closeServer();
    }
}
