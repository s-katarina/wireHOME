package projectnwt2023.backend.exceptions;

import org.springframework.http.HttpStatus;

public class EntityAlreadyExistsException extends RuntimeException {

    public String message;
    public HttpStatus httpStatus;

    public EntityAlreadyExistsException(Class<?> c) {
        super();
        this.message = c.getSimpleName().concat(" already exists");
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

}
