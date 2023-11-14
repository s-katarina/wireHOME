package projectnwt2023.backend.exceptions;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    static class ErrorMessage {
        String message;
        public ErrorMessage(String message){
            this.message = message;
        }
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        System.out.println("HANDLING");
        return new ResponseEntity<>(ex.message, ex.httpStatus);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<String> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        String errorMessage = "Could not find acceptable representation. Please check your Accept headers.";
        return new ResponseEntity<>(errorMessage, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(value = { ConstraintViolationException.class })
    protected ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {

        String ret = "";

        for (ConstraintViolation violation : ex.getConstraintViolations()) {
            String fieldName = ((PathImpl) violation.getPropertyPath()).getLeafNode().toString();
            ret += "Constraint violation. Field (" + fieldName + ") format is not valid!\n";
        }

        return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { MethodArgumentNotValidException.class })
    protected ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        String ret = "";

        for (ObjectError error : ex.getBindingResult().getAllErrors())
            ret += error.getDefaultMessage() + "\n";

        return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { MethodArgumentTypeMismatchException.class })
    protected ResponseEntity<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {

        String fieldName = ex.getName();

        return new ResponseEntity<>("Wrong type. Field (" + fieldName + ") format is not valid!\n", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserForbiddenOperationException.class)
    protected ResponseEntity<String> handleUserForbiddenException(UserForbiddenOperationException ex) {
        return new ResponseEntity<>(ex.message, ex.httpStatus);
    }

    @ExceptionHandler(IOException.class)
    protected ResponseEntity<String> handleIOException(IOException ex) {
        return new ResponseEntity<>("Error - io", HttpStatus.BAD_REQUEST);
    }
}
