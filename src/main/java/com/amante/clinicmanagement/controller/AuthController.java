package com.amante.clinicmanagement.controller;

import com.amante.clinicmanagement.dto.request.LoginRequest;
import com.amante.clinicmanagement.dto.request.RegisterRequest;
import com.amante.clinicmanagement.dto.response.ApiResponse;
import com.amante.clinicmanagement.dto.response.AuthResponse;
import com.amante.clinicmanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "User registration and login endpoints"
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Registration successful",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = AuthResponse.class
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description =
                                    "Invalid input or email already exists"
                    )
            }
    )
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid
            @RequestBody
            RegisterRequest request
    ) {
        AuthResponse response =
                authService.register(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Registration successful",
                        response
                )
        );
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Login successful",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = AuthResponse.class
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Invalid email or password"
                    )
            }
    )
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {
        AuthResponse response =
                authService.login(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Login successful",
                        response
                )
        );
    }
}