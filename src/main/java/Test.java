import exceptions.WrongPassException;
import model.Message;
import model.User;
import server.Server;

import java.util.Iterator;

public class Test {
    //testfile aanpassing
    public static void Main(String[] args) throws WrongPassException {
        System.out.println("Commencing test...");
        Server server = new Server("testServer");

        User user1 = server.validateUser("new user1", "password");

        try {
            server.validateUser("new user1", "p4ssw0rd");
        } catch (WrongPassException wpe){
            System.out.println("wrong password caught correctly.");
        }

        try {
            User user2 = server.validateUser("new user1", "password");
            if(user1.equals(user2)){
                System.out.println("correctly validated a user.");
            }
        } catch (WrongPassException wpe) {
            System.out.println("should never arrive here.");
        }

        //commencing testing of messaging system...
        System.out.println("commencing testing of messaging system...");
        server.addMessage("Message1", user1);
        server.addMessage("Message2", user1);
        server.addMessage("Message3", user1);
        server.addMessage("Message4", user1);

        Iterator<Message> msgIt = server.getMessageIterator();
        while(msgIt.hasNext()){
            System.out.println(msgIt.next());
        }

        server.addMessage("Message5", user1);
        server.addMessage("Message6", user1);
        server.addMessage("Message7", user1);

        msgIt = server.getMessageIterator();
        while(msgIt.hasNext()){
            System.out.println(msgIt.next());
        }
    }
}
