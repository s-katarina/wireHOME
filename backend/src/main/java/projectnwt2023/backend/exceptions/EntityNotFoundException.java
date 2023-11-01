package projectnwt2023.backend.exceptions;

import org.springframework.http.HttpStatus;


public class EntityNotFoundException extends RuntimeException {

    public String message;
    public HttpStatus httpStatus;

    public EntityNotFoundException(Class<?> c) {
        super();
        this.message = c.getSimpleName().concat(" not found");
        this.httpStatus = HttpStatus.NOT_FOUND;
    }

}