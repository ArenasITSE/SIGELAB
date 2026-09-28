package sigelab_api.auth.controller;

import lombok.RequiredArgsConstructor;
import sigelab_api.auth.dto.LoginRequest;
import sigelab_api.auth.dto.LoginResponse;
import sigelab_api.auth.service.AuthService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}