package cz.techlib.evidencevypujcek;

public class LoanException extends Exception {
    public LoanException(String errorMessage) {
        super(errorMessage);
    }
}
