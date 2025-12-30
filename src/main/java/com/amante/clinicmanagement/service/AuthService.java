package com.amante.clinicmanagement.service;

import com.amante.clinicmanagement.dto.request.LoginRequest;
import com.amante.clinicmanagement.dto.request.RegisterRequest;
import com.amante.clinicmanagement.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}