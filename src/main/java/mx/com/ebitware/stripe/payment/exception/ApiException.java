package mx.com.ebitware.stripe.payment.exception;

import org.springframework.http.HttpStatus;


public class ApiException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private HttpStatus httpStatus;
    private int code;
    private String message;
    private Object errors;
    private Object data;

    public ApiException(ApiError payload) {
        super(payload.getMessage());
        this.httpStatus = payload.getHttpStatus();
        this.code = payload.getCode();
        this.message = payload.getMessage();
    }

    public ApiException(String message) {
        super();
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getErrors() {
        return errors;
    }

    public void setErrors(Object errors) {
        this.errors = errors;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
