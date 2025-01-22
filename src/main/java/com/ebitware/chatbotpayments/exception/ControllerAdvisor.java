package com.ebitware.chatbotpayments.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@Slf4j
@RestControllerAdvice
@ResponseBody
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleUserNotFoundException(UserNotFoundException ex , WebRequest request){

        String messageResponse = "El usuario con ese ID no se encuentra";
        ErrorMessage message = new ErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                new Date(),
                messageResponse,
                request.getDescription(false));
		writeLog(message,ex);
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(value = RuntimeException.class)
	public ResponseEntity<ErrorMessage> runtimeExceptionHandler(RuntimeException ex , WebRequest request) {
		log.info("in runtimeExceptionHandler");
		ErrorMessage message = new ErrorMessage(
				HttpStatus.BAD_REQUEST.value(),
				new Date(),
				ex.getMessage(),
				request.getDescription(false)
		);
		writeLog(message,ex);
		return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = RequestException.class)
	public ResponseEntity<ErrorMessage> requestExceptionHandler(RequestException ex,WebRequest request) {
		ErrorMessage message = new ErrorMessage(
				HttpStatus.BAD_REQUEST.value(),
				new Date(),
				ex.getMessage(),
				request.getDescription(false)
		);
		writeLog(message,ex);
		return new ResponseEntity<>(message, ex.getStatus());
	}

	private static void writeLog (ErrorMessage errorResponse, Exception exception) {
		log.error("Exception location: {}", errorResponse.getDescription());
		log.error("An exception occurred: {}", exception.getMessage());
	}

}
