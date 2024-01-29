package projectnwt2023.backend.exceptions;

import org.springframework.http.HttpStatus;

public class AirConditionerRegimeNotSupportedException extends RuntimeException {

    public String message;
    public HttpStatus httpStatus;

    public AirConditionerRegimeNotSupportedException(Class<?> c) {
        super();
        this.message = c.getSimpleName().concat(" not valid regime");
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

}
