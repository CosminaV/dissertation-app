package ro.ase.ism.dissertation.exception;

public class ExamAlreadyStartedException extends RuntimeException {
    public ExamAlreadyStartedException(String message) {
        super(message);
    }
}
