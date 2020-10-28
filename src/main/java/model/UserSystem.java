package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import exceptions.WrongPassException;

//deze klasse houdt een lijst bij van alle users, en kan gebruikt worden om te identificeren.
public class UserSystem {
        private final List<User> users;
        private final String fileName;

        public UserSystem(){
            this("LoginDB.txt");
        }

        public UserSystem(String fileName) {
            this.users = new ArrayList<>();
            this.fileName = fileName+".txt";
            this.readFromFile();
        }

        @Override
        protected void finalize() {
            exitUserSystem();
        }

        public void closeUserSystem(){
            exitUserSystem();
        }

        public void exitUserSystem() {
            System.out.println("saving database...");
            if(this.logToFile()) {
                System.out.println("logged succesfully.");
                return;
            }
            System.out.println("Critical error with logging detected.");
        }

        private User registerUser(String name, String password) {
            System.out.println("Registering user: "+name);
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
            User result = new User(name, hash);
            users.add(result);
            return result;
        }

        //Er wordt een wrongpass-exception opgegooid als user al geregistreerd is met een ander passwoord
        //daar kan je de message van gewoon op een van beide fout-locaties printen die voorzien zijn in de loginController.
        public User validateUser(String name, String password) throws WrongPassException{
            User user = findUserByName(name);
            if(user != null) {
                if(!BCrypt.checkpw(password, user.getPassword())) {
                    throw new WrongPassException();
                }
            } else {
                user = registerUser(name, password);
            }
            return user;
        }

        public User findUserByName(String name) {
            for(User u: users) {
                if(u.getName().equals(name)) {
                    return u;
                }
            }
            return null;
        }

        private boolean logToFile() {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName));
                writer.write(Integer.toString(users.size()));
                writer.newLine();
                for(User user: users) {
                    writer.write(user.toString());
                    writer.newLine();
                }
                writer.close();
                return true;
            } catch (IOException ioe) {
                return false;
            }
        }

        private boolean readFromFile() {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(this.fileName));
                int AmountOfUsers = Integer.parseInt(reader.readLine());
                for(int i=0; i<AmountOfUsers; i++) {
                    String[] userInfo = reader.readLine().split(",");
                    users.add(new User(userInfo[0], userInfo[1]));
                }
                reader.close();
                return true;
            } catch (IOException ioe) {
                return false;
            }
        }
}
