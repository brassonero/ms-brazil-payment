package mx.com.ebitware.stripe.payment.service;

public interface ConfirmationTokenService {
    String generateToken();
    void saveToken(String email, String token);
    String validateToken(String token);

}
