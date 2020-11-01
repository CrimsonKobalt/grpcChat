package exceptions;

import model.User;

public class WrongPassException extends Exception{
    public WrongPassException(){
        super("invalid user details.");
    }

    public WrongPassException(String username){
        super("invalid password for user "+username);
    }
}
