package org.example.urlshortener.auth;

import org.example.urlshortener.auth.dto.AuthResponse;
import org.example.urlshortener.auth.dto.LoginRequest;
import org.example.urlshortener.auth.dto.RegisterRequest;
import org.example.urlshortener.user.Role;
import org.example.urlshortener.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @Test
    void registerReturnsCreated() {
        RegisterRequest request = new RegisterRequest("alice", "Password1");
        UserResponse user = new UserResponse(1L, "alice", Role.USER, Instant.now());
        when(authService.register(request)).thenReturn(user);

        ResponseEntity<UserResponse> response = controller.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(user);
    }

    @Test
    void loginReturnsToken() {
        LoginRequest request = new LoginRequest("alice", "Password1");
        AuthResponse auth = AuthResponse.bearer("token", 1000L);
        when(authService.login(request)).thenReturn(auth);

        ResponseEntity<AuthResponse> response = controller.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(auth);
    }
}
