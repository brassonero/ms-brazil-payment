package mx.com.ebitware.stripe.payment.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
public class RequestException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final String code;
	private final HttpStatus status;

	public RequestException(String code, HttpStatus status, String message) {
		super(message);
		this.code = code;
		this.status = status;
	}
}