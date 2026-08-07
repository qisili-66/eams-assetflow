package org.example.eams.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.example.eams.enums.UserStatus;

@Getter
@Setter
public class UserQuery {

    @Min(value=1,message="页码不能小于1")
    private long page =1;

    @Min(value =1,message="每页数量不能小于1")
    @Max(value=100,message="每页数量不能超过100")
    private long size=10;

    private String keyword;
    private Long departmentId;
    private UserStatus status;
}
