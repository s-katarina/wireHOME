package projectnwt2023.backend.exceptions;

import org.springframework.http.HttpStatus;


public class UserForbiddenOperationException extends RuntimeException {

    public String message;
    public HttpStatus httpStatus;

    public UserForbiddenOperationException() {
        super();
        this.message = "Forbidden operation.";
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

}