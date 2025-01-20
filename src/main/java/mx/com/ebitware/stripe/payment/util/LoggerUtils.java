package mx.com.ebitware.stripe.payment.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Logger;

public class LoggerUtils {
    private static ObjectMapper mapper = new ObjectMapper();
    private static final String MICROSERVICE = "BB112";
    private static final String SERVICE = "BBF";
    private static final String HUB = "HUBWHAT";
    private static final String WEBHOOK = "BBW";
    private static String prefixSeparator = " | ";
    private static Logger logger = Logger.getLogger("");

    public static void loggerInputDefault( String operationId, int companyId, String method, String httpCodeResponse, String codeResponse, String message) {
        StringBuilder data = new StringBuilder();
        data.append( MICROSERVICE );
        data.append( prefixSeparator );
        data.append( operationId );
        data.append( prefixSeparator );
        data.append( SERVICE );
        data.append( prefixSeparator );
        data.append( "ENTRADA" );
        data.append( prefixSeparator );
        data.append( method );
        data.append( prefixSeparator );
        data.append( companyId );
        data.append( prefixSeparator );
        data.append( "0" );
        data.append( prefixSeparator );
        data.append( httpCodeResponse );
        data.append( prefixSeparator );
        data.append( codeResponse );
        data.append( prefixSeparator );
        data.append( message );
        logger.info(data.toString());
    }

    public static void loggerOutputDefault( String operationId, int companyId, long timeProcess, String method, String httpCodeResponse, String codeResponse, String message) {
        StringBuilder data = new StringBuilder();
        data.append( MICROSERVICE );
        data.append( prefixSeparator );
        data.append( operationId );
        data.append( prefixSeparator );
        data.append( SERVICE );
        data.append( prefixSeparator );
        data.append( "SALIDA" );
        data.append( prefixSeparator );
        data.append( companyId );
        data.append( prefixSeparator );
        data.append( timeProcess );
        data.append( prefixSeparator );
        data.append( httpCodeResponse );
        data.append( prefixSeparator );
        data.append( codeResponse );
        data.append( prefixSeparator );
        data.append( message );
        logger.info(data.toString());
    }

    public static void loggerInputAud( String operationId, Class<?> clase,  int companyId, String method, String httpVerb, String url, Object header, Object body, boolean active) {
        String headerString = "NA", bodyString = "NA";
        if (active) {
            try {
                if (header != null)
                    headerString = mapper.writeValueAsString(header);
            } catch (Exception e) {}

            try {
                if (body != null)
                    bodyString = mapper.writeValueAsString(body);
            } catch (Exception e) {}

            StringBuilder data = new StringBuilder();
            data.append( MICROSERVICE );
            data.append( prefixSeparator );
            data.append( operationId  );
            data.append( prefixSeparator );
            data.append( HUB );
            data.append( prefixSeparator );
            data.append( "ENTRADA" );
            data.append( prefixSeparator );
            data.append( clase.getCanonicalName() );
            data.append( prefixSeparator );
            data.append( method );
            data.append( prefixSeparator );
            data.append( httpVerb );
            data.append( prefixSeparator );
            data.append( url );
            data.append( prefixSeparator );
            data.append( headerString );
            data.append( prefixSeparator );
            data.append( bodyString );
            logger.info(data.toString());
        }
    }

    public static void loggerInputAud( String operationId, String className,  int companyId, String method,	String httpVerb, String url, Object header, Object body, boolean active) {
        String headerString = "NA", bodyString = "NA";
        if (active) {
            try {
                if (header != null)
                    headerString = mapper.writeValueAsString(header);
            } catch (Exception e) {}

            try {
                if (body != null)
                    bodyString = mapper.writeValueAsString(body);
            } catch (Exception e) {}

            StringBuilder data = new StringBuilder();
            data.append( MICROSERVICE );
            data.append( prefixSeparator );
            data.append( operationId  );
            data.append( prefixSeparator );
            data.append( HUB );
            data.append( prefixSeparator );
            data.append( "ENTRADA" );
            data.append( prefixSeparator );
            data.append( className );
            data.append( prefixSeparator );
            data.append( method );
            data.append( prefixSeparator );
            data.append( httpVerb );
            data.append( prefixSeparator );
            data.append( url );
            data.append( prefixSeparator );
            data.append( headerString );
            data.append( prefixSeparator );
            data.append( bodyString );
            logger.info(data.toString());
        }
    }

    public static void loggerOutputAud( String operationId, Class<?> clase,  int companyId, long timeProcess, String method, String httpVerb, String url, Object header, Object body, boolean active) {
        String headerString = "NA", bodyString = "NA";
        if (active) {
            try {
                if (header != null)
                    headerString = mapper.writeValueAsString(header);
            } catch (Exception e) {}

            try {
                if (body != null)
                    bodyString = mapper.writeValueAsString(body);
            } catch (Exception e) {}


            StringBuilder data = new StringBuilder();
            data.append( MICROSERVICE );
            data.append( prefixSeparator );
            data.append( operationId );
            data.append( prefixSeparator );
            data.append( HUB );
            data.append( prefixSeparator );
            data.append( "SALIDA" );
            data.append( prefixSeparator );
            data.append( clase.getCanonicalName() );
            data.append( prefixSeparator );
            data.append( method );
            data.append( prefixSeparator );
            data.append( httpVerb );
            data.append( prefixSeparator );
            data.append( url );
            data.append( prefixSeparator );
            data.append( headerString );
            data.append( prefixSeparator );
            data.append( bodyString );
            logger.info(data.toString());
        }
    }

    public static void loggerOutputAud( String operationId, String className,  int companyId, long timeProcess, String method, String httpVerb, String url, Object header, Object body, boolean active) {
        String headerString = "NA", bodyString = "NA";
        if (active) {
            try {
                if (header != null)
                    headerString = mapper.writeValueAsString(header);
            } catch (Exception e) {}

            try {
                if (body != null)
                    bodyString = mapper.writeValueAsString(body);
            } catch (Exception e) {}


            StringBuilder data = new StringBuilder();
            data.append( MICROSERVICE );
            data.append( prefixSeparator );
            data.append( operationId );
            data.append( prefixSeparator );
            data.append( HUB );
            data.append( prefixSeparator );
            data.append( "SALIDA" );
            data.append( prefixSeparator );
            data.append( className );
            data.append( prefixSeparator );
            data.append( method );
            data.append( prefixSeparator );
            data.append( httpVerb );
            data.append( prefixSeparator );
            data.append( url );
            data.append( prefixSeparator );
            data.append( headerString );
            data.append( prefixSeparator );
            data.append( bodyString );
            logger.info(data.toString());
        }
    }

    public static void loggerInputAudApi( String operationId, Class<?> clase,  int companyId, String method, String httpVerb, String url, Object header, Object body, boolean active) {
        String headerString = "NA", bodyString = "NA";
        if (active) {
            try {
                if (header != null)
                    headerString = mapper.writeValueAsString(header);
            } catch (Exception e) {}

            try {
                if (body != null)
                    bodyString = mapper.writeValueAsString(body);
            } catch (Exception e) {}

            StringBuilder data = new StringBuilder();
            data.append( MICROSERVICE );
            data.append( prefixSeparator );
            data.append( operationId );
            data.append( prefixSeparator );
            data.append( SERVICE );
            data.append( prefixSeparator );
            data.append( "ENTRADA" );
            data.append( prefixSeparator );
            data.append( clase.getCanonicalName() );
            data.append( prefixSeparator );
            data.append( method );
            data.append( prefixSeparator );
            data.append( httpVerb );
            data.append( prefixSeparator );
            data.append( url );
            data.append( prefixSeparator );
            data.append( headerString );
            data.append( prefixSeparator );
            data.append( bodyString );
            logger.info(data.toString());
        }
    }

    public static void loggerOutputAudApi( String operationId, Class<?> clase,  int companyId, long timeProcess, String method, String httpVerb, String url, Object header, Object body, boolean active) {
        String headerString = "NA", bodyString = "NA";
        if (active) {
            try {
                if (header != null)
                    headerString = mapper.writeValueAsString(header);
            } catch (Exception e) {}

            try {
                if (body != null)
                    bodyString = mapper.writeValueAsString(body);
            } catch (Exception e) {}

            StringBuilder data = new StringBuilder();
            data.append( MICROSERVICE );
            data.append( prefixSeparator );
            data.append( operationId );
            data.append( prefixSeparator );
            data.append( SERVICE );
            data.append( prefixSeparator );
            data.append( "SALIDA" );
            data.append( prefixSeparator );
            data.append( clase.getCanonicalName() );
            data.append( prefixSeparator );
            data.append( method );
            data.append( prefixSeparator );
            data.append( httpVerb );
            data.append( prefixSeparator );
            data.append( url );
            data.append( prefixSeparator );
            data.append( headerString );
            data.append( prefixSeparator );
            data.append( bodyString );
            logger.info(data.toString());
        }
    }

    public static void loggerInputWebhook( String operationId, Class<?> clase,  int companyId, String method, String httpVerb, String url,Object header, Object body, boolean active) {
        String headerString = "NA", bodyString = "NA";
        if (active) {
            try {
                if (header != null)
                    headerString = mapper.writeValueAsString(header);
            } catch (Exception e) {}

            try {
                if (body != null)
                    bodyString = mapper.writeValueAsString(body);
            } catch (Exception e) {}

            StringBuilder data = new StringBuilder();
            data.append( MICROSERVICE );
            data.append( prefixSeparator );
            data.append( operationId );
            data.append( prefixSeparator );
            data.append( WEBHOOK  );
            data.append( prefixSeparator );
            data.append( "ENTRADA" );
            data.append( prefixSeparator );
            data.append( clase.getCanonicalName() );
            data.append( prefixSeparator );
            data.append( method );
            data.append( prefixSeparator );
            data.append( httpVerb );
            data.append( prefixSeparator );
            data.append( url );
            data.append( prefixSeparator );
            data.append( headerString );
            data.append( prefixSeparator );
            data.append( bodyString );
            logger.info(data.toString());
        }
    }

    public static void loggerOutputWebhook( String operationId, Class<?> clase,  int companyId, long timeProcess, String method, String httpVerb, String url, Object header, Object body, boolean active) {
        String headerString = "NA", bodyString = "NA";
        if (active) {
            try {
                if (header != null)
                    headerString = mapper.writeValueAsString(header);
            } catch (Exception e) {}

            try {
                if (body != null)
                    bodyString = mapper.writeValueAsString(body);
            } catch (Exception e) {}

            StringBuilder data = new StringBuilder();
            data.append( MICROSERVICE );
            data.append( prefixSeparator );
            data.append( operationId );
            data.append( prefixSeparator );
            data.append( WEBHOOK );
            data.append( prefixSeparator );
            data.append( "SALIDA" );
            data.append( prefixSeparator );
            data.append( clase.getCanonicalName() );
            data.append( prefixSeparator );
            data.append( method );
            data.append( prefixSeparator );
            data.append( httpVerb );
            data.append( prefixSeparator );
            data.append( url );
            data.append( prefixSeparator );
            data.append( headerString );
            data.append( prefixSeparator );
            data.append( bodyString );
            logger.info(data.toString());
        }
    }

}
