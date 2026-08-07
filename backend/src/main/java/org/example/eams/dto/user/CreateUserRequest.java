package org.example.eams.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateUserRequest(

        @NotBlank(message = "账号不能为空")
        @Size(max = 50, message = "账号不能超过50个字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 50, message = "密码长度必须为6到50个字符")
        String password,

        @NotBlank(message = "昵称不能为空")
        @Size(max = 50, message = "昵称不能超过50个字符")
        String nickname,

        @NotNull(message = "部门不能为空")
        Long departmentId,

        @NotEmpty(message = "至少选择一个角色")
        List<@NotBlank(message = "角色不能为空") @Size(max = 20, message = "角色编码不能超过20个字符") String> roleCodes
) {
}
