package pl.konradchrzanowski.githubuserrepos.exception;

public class ReposNotFoundException extends RuntimeException {
    public ReposNotFoundException(String message) {
        super(message);
    }
}
