package exceptions;

public class WrongPassException extends Exception{
    public WrongPassException(){
        super("invalid user details.");
    }
}
