package org.example.eams.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eams.common.Result;
import org.example.eams.dto.user.CreateUserRequest;
import org.example.eams.dto.user.ResetPasswordRequest;
import org.example.eams.dto.user.UpdateUserRequest;
import org.example.eams.dto.user.UserQuery;
import org.example.eams.dto.user.UserStatusRequest;
import org.example.eams.service.UserService;
import org.example.eams.vo.PageResult;
import org.example.eams.vo.UserPageVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<PageResult<UserPageVo>> page(@Valid UserQuery query) {
        return Result.success(userService.page(query));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody CreateUserRequest req) {
        return Result.success(userService.create(req));
    }

    @GetMapping("/{id}")
    public Result<UserPageVo> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest req
    ) {
        userService.update(id, req);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest req
    ) {
        userService.updateStatus(id, req);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest req
    ) {
        userService.resetPassword(id, req);
        return Result.success();
    }
}
