package com.pharma.application.exception;

public class ResourceNotFoundException extends PharmaException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super("%s не найден(а): %s".formatted(resourceName, id));
    }
}
