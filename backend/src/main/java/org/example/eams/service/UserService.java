package org.example.eams.service;

import org.example.eams.dto.user.CreateUserRequest;
import org.example.eams.dto.user.ResetPasswordRequest;
import org.example.eams.dto.user.UpdateUserRequest;
import org.example.eams.dto.user.UserQuery;
import org.example.eams.dto.user.UserStatusRequest;
import org.example.eams.vo.PageResult;
import org.example.eams.vo.UserPageVo;

public interface UserService {

    PageResult<UserPageVo> page(UserQuery query);
    UserPageVo getById(Long id);
    Long create(CreateUserRequest req);
    void update(Long id, UpdateUserRequest req);
    void updateStatus(Long id, UserStatusRequest req);
    void resetPassword(Long id, ResetPasswordRequest req);
}
