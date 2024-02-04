package projectnwt2023.backend.exceptions;

import org.springframework.http.HttpStatus;

public class IntervalNotValidException extends RuntimeException {

    public String message;
    public HttpStatus httpStatus;

    public IntervalNotValidException(Class<?> c) {
        super();
        this.message = c.getSimpleName().concat(" not valid range");
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

}
