package org.example.eams.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eams.common.Result;
import org.example.eams.dto.LoginRequest;
import org.example.eams.service.AuthService;
import org.example.eams.vo.CurrentUserVo;
import org.example.eams.vo.LoginResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("login")
    public Result<LoginResponse>loginResponseResult(
            @Valid @RequestBody LoginRequest request
    ){
        return Result.success(authService.login(request));
    }
    @GetMapping("/me")
    public Result<CurrentUserVo>currentUser(
            Authentication authentication ){
        return Result.success(
                authService.getCurrentUser(authentication.getName())
        );
    }

}
