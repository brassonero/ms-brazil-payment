package mx.com.ebitware.stripe.payment.model;

import org.springframework.http.HttpStatus;

public class ApiResponse<T > {
    private HttpStatus httpStatus;
    private String message;
    private Object messageParams;
    private T data;

    public ApiResponse(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public ApiResponse(HttpStatus httpStatus, String message) {
        super();
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public ApiResponse(HttpStatus httpStatus, T data) {
        this.httpStatus = httpStatus;
        this.data = data;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public Object getMessageParams() {
        return messageParams;
    }

    public T getData() {
        return data;
    }
}
