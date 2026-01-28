
package com.capgemini.common.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, String identifier) {
        super("RESOURCE_NOT_FOUND",
                String.format("%s not found with identifier: %s", resourceName, identifier));
    }
}