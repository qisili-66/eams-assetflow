package org.example.eams.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest (
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 50, message = "密码长度必须为6到50个字符")
        String newPassword
){
}
