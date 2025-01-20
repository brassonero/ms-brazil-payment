package mx.com.ebitware.stripe.payment.constants;

public class LogsErrors {

    public static final String DELETE = "Received DELETE request";
    public static final String SEND_DELETE_OK = "DELETE request sent successfully";
    public static final String SEND_DELETE_ERROR = "Error sending DELETE request";
    // ********************************** Received **********************************
    /* GET */
    public static String GET = "Received GET data";
    public static String SEND_GET_OK = "Received GET data OK";
    public static String SEND_GET_CONFLIC = "Received GET data with CONFLICT";
    public static String SEND_GET_ERROR = "Received GET data with ERROR";

    /* POST */
    public static String POST = "Received POST data";
    public static String SEND_POST_OK = "Received POST data OK";
    public static String SEND_POST_CONFLIC = "Received POST data with CONFLICT";
    public static String SEND_POST_ERROR = "Received POST data with ERROR";


    /* PUT */
    public static String PUT = "Received to PUT data";
    public static String SEND_PUT_OK = "Received PUT data OK";
    public static String SEND_PUT_CONFLIC = "Received PUT data with CONFLICT";
    public static String SEND_PUT_ERROR = "Received PUT data with ERROR";

    // ********************************** Send **********************************
    /* GET */
    public static String GET_INTERN = "Try to GET data";
    public static String SEND_INTERN_GET_OK = "Send GET data OK";
    public static String SEND_INTERN_API_GET_CONFLIC = "Send GET data with CONFLICT";
    public static String SEND_INTERN_API_GET_ERROR = "Send GET data with ERROR";

    /* POST */
    public static String POST_INTERN = "Try to POST data";
    public static String SEND_INTERN_POST_OK = "Send POST data OK";
    public static String SEND_INTERN_POST_CONFLIC = "Send POST data with CONFLICT";
    public static String SEND_INTERN_POST_ERROR = "Send POST data with ERROR";


    /* PUT */
    public static String PUT_INTERN = "Try to PUT data";
    public static String SEND_INTERN_PUT_OK = "Send PUT data OK";
    public static String SEND_INTERN_PUT_CONFLIC = "Send PUT data with CONFLICT";
    public static String SEND_INTERN_PUT_ERROR = "Send PUT data with ERROR";

    // ********************************** Send API **********************************
    /* GET */
    public static String GET_API = "Try to GET data api";
    public static String SEND_API_GET_OK = "Send GET data api OK";
    public static String SEND_API_GET_CONFLIC = "Send GET data api with CONFLICT";
    public static String SEND_API_GET_ERROR = "Send GET data api with ERROR";

    /* POST */
    public static String POST_API = "Try to POST data api";
    public static String SEND_API_POST_OK = "Send POST data api OK";
    public static String SEND_API_POST_CONFLIC = "Send POST data api with CONFLICT";
    public static String SEND_API_POST_ERROR = "Send POST data api with ERROR";


    /* PUT */
    public static String PUT_API = "Try to PUT data api";
    public static String SEND_API_PUT_OK = "Send PUT data api OK";
    public static String SEND_API_PUT_CONFLIC = "Send PUT data api with CONFLICT";
    public static String SEND_API_PUT_ERROR = "Send PUT data api with ERROR";

}

