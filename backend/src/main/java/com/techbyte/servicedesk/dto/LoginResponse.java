package com.techbyte.servicedesk.dto;

public class LoginResponse {

    private String accessToken;
    private UserSummaryDto user;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, UserSummaryDto user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public UserSummaryDto getUser() {
        return user;
    }

    public void setUser(UserSummaryDto user) {
        this.user = user;
    }
}

