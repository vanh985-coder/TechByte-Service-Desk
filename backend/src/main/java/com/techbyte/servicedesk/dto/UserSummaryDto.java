package com.techbyte.servicedesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techbyte.servicedesk.domain.entity.User;
import com.techbyte.servicedesk.domain.enums.Role;
import com.techbyte.servicedesk.security.CustomUserPrincipal;

public class UserSummaryDto {

    private Long id;
    private String email;

    @JsonProperty("full_name")
    private String fullName;

    private Role role;

    public UserSummaryDto() {
    }

    public UserSummaryDto(Long id, String email, String fullName, Role role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public static UserSummaryDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }

    public static UserSummaryDto fromPrincipal(CustomUserPrincipal principal) {
        if (principal == null) {
            return null;
        }
        return new UserSummaryDto(
                principal.getId(),
                principal.getEmail(),
                principal.getFullName(),
                principal.getRole()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

