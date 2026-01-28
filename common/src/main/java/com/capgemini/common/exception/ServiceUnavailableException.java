package com.capgemini.common.exception;

public class ServiceUnavailableException extends BusinessException {
    public ServiceUnavailableException(String serviceName) {
        super("SERVICE_UNAVAILABLE",
                String.format("Service %s is temporarily unavailable", serviceName));
    }
}