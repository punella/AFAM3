package logic;

public class NotImplementedYetException extends Exception{

    public NotImplementedYetException(String missingBehavior){
        super(missingBehavior);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
