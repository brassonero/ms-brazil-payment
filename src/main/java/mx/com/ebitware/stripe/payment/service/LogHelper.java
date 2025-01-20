package mx.com.ebitware.stripe.payment.service;

public interface LogHelper {
    void startLog(Object authorizedUser, String method, String message, String http, Object body, String url, String className);

    void finishLog(Object body, String message);

    void clean();
}
