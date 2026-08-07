package org.example.eams.vo;

public record LoginResponse (
        String accessToken,
        String tokenType,
        long expiresIn,
        CurrentUserVo user
){
}
