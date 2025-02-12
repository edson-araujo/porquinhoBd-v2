package com.wave.porquinho.responses;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiRetornoResponse {
    private int status;
    private String message;
    private Map<String, String> fieldErrors;

    public static ApiRetornoResponse of(int status, String message) {
        return new ApiRetornoResponse(status, message, null);
    }

    public static ApiRetornoResponse withFields(int status, String message, Map<String, String> fieldErrors) {
        return new ApiRetornoResponse(status, message, fieldErrors);
    }
}
