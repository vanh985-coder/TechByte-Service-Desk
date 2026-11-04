package com.techbyte.servicedesk.exception;

import java.util.Collections;
import java.util.List;

public class ApiErrorResponse {

    private ErrorDetail error;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(String code, String message, List<String> details) {
        this.error = new ErrorDetail(code, message, details != null ? details : Collections.emptyList());
    }

    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message, Collections.emptyList());
    }

    public static ApiErrorResponse of(String code, String message, List<String> details) {
        return new ApiErrorResponse(code, message, details);
    }

    public ErrorDetail getError() {
        return error;
    }

    public void setError(ErrorDetail error) {
        this.error = error;
    }

    public static class ErrorDetail {
        private String code;
        private String message;
        private List<String> details;

        public ErrorDetail() {
        }

        public ErrorDetail(String code, String message, List<String> details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<String> getDetails() {
            return details;
        }

        public void setDetails(List<String> details) {
            this.details = details;
        }
    }
}

