package com.ebitware.chatbotpayments.util;

import com.ebitware.chatbotpayments.service.LogHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.UUID;

@Component
public class KibanaLogHelper implements LogHelper {
    private long startTime;
    private String operationId;
    private String methodName;
    private String httpVerb;
    private String urlService;
    private int companyId;
    private String className;

    @Value("${app.log.auditoria}")
    private boolean activeAud;


    @Override
    public void startLog(Object authorizedUser, String method, String message, String http, Object body, String url,
                         String className) {
        httpVerb = http;
        methodName = method;
        startTime = System.currentTimeMillis();
        operationId = UUID.randomUUID().toString();
        urlService = url;

        LoggerUtils.loggerInputDefault(operationId, companyId, method, "200", "0", message);
        LoggerUtils.loggerInputAud(operationId, className, companyId, method, httpVerb, urlService, null, body, activeAud);
    }

    @Override
    public void finishLog(Object body, String message) {
        companyId =  ObjectUtils.isEmpty(companyId) ? -1 : companyId;
        long endTime = System.currentTimeMillis() - startTime;
        LoggerUtils.loggerOutputAud(operationId, className, companyId, endTime, methodName, httpVerb, urlService, null, body, activeAud);
        LoggerUtils.loggerOutputDefault(operationId, companyId, endTime, methodName, "200", "0", message);

        clean();
    }

    @Override
    public void clean() {
        startTime = 0;
        operationId = null;
        methodName = null;
        httpVerb = null;
        urlService = null;
        className = null;
    }
}
