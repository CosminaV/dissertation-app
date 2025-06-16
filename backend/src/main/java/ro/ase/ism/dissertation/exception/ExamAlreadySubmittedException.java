package ro.ase.ism.dissertation.exception;

public class ExamAlreadySubmittedException extends RuntimeException {
    public ExamAlreadySubmittedException(String message) {
        super(message);
    }
}
