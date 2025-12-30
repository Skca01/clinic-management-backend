package com.amante.clinicmanagement.dto.response;

import com.amante.clinicmanagement.entity.User;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private User.Role role;
    private Long profileId;
    private String firstName;
    private String lastName;

    public AuthResponse() {
    }

    public AuthResponse(
            String token,
            String type,
            Long userId,
            String email,
            User.Role role,
            Long profileId,
            String firstName,
            String lastName
    ) {
        this.token = token;
        this.type = type;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.profileId = profileId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}