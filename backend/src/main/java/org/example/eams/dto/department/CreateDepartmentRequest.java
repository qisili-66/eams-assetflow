package org.example.eams.dto.department;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateDepartmentRequest(


        @NotBlank(message = "部门名称不能为空")
        @Size(max = 100, message = "部门名称不能超过100个字符")
        String name,

        @NotNull(message = "父部门不能为空")
        @PositiveOrZero(message = "父部门必须为0或有效部门ID")
        Long parentId,

        @NotNull(message = "排序值不能为空")
        @Min(value = 0, message = "排序值不能小于0")
        Integer sortNo
) {
}
