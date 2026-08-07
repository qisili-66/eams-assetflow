package org.example.eams.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.eams.dto.user.CreateUserRequest;
import org.example.eams.dto.user.ResetPasswordRequest;
import org.example.eams.dto.user.UpdateUserRequest;
import org.example.eams.dto.user.UserQuery;
import org.example.eams.dto.user.UserStatusRequest;
import org.example.eams.entity.SysDepartment;
import org.example.eams.entity.SysUser;
import org.example.eams.enums.ErrorCode;
import org.example.eams.enums.UserStatus;
import org.example.eams.exception.BusinessException;
import org.example.eams.mapper.AuthRoleMapper;
import org.example.eams.mapper.DepartmentMapper;
import org.example.eams.mapper.SysUserMapper;
import org.example.eams.mapper.UserRoleMapper;
import org.example.eams.service.UserService;
import org.example.eams.vo.PageResult;
import org.example.eams.vo.UserPageVo;
import org.example.eams.vo.UserRoleRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final AuthRoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserPageVo> page(UserQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getId);

        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item
                    .like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getNickname, keyword));
        }
        if (query.getDepartmentId() != null) {
            wrapper.eq(SysUser::getDepartmentId, query.getDepartmentId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }

        Page<SysUser> page = userMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        List<SysUser> users = page.getRecords();
        if (users.isEmpty()) {
            return new PageResult<>(
                    page.getCurrent(), page.getSize(), page.getTotal(), List.of()
            );
        }

        List<Long> userIds = users.stream().map(SysUser::getId).toList();
        List<Long> departmentIds = users.stream()
                .map(SysUser::getDepartmentId)
                .distinct()
                .toList();

        Map<Long, String> departmentNames = departmentMapper.selectByIds(departmentIds)
                .stream()
                .collect(Collectors.toMap(SysDepartment::getId, SysDepartment::getName));

        Map<Long, List<String>> roleCodes = roleMapper.selectRoleRowsByUserIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(
                        UserRoleRow::getUserId,
                        Collectors.mapping(UserRoleRow::getRoleCode, Collectors.toList())
                ));

        List<UserPageVo> records = users.stream()
                .map(user -> toVo(user, departmentNames, roleCodes))
                .toList();

        return new PageResult<>(
                page.getCurrent(), page.getSize(), page.getTotal(), records
        );
    }

    @Override
    public UserPageVo getById(Long id) {
        SysUser user = findUser(id);
        return new UserPageVo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getDepartmentId(),
                departmentMapper.selectNameById(user.getDepartmentId()),
                user.getStatus(),
                roleMapper.selectRoleCodesByUserId(user.getId()),
                user.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public Long create(CreateUserRequest req) {
        String username = req.username().trim();
        if (userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        ) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "账号已存在");
        }

        validateDepartment(req.departmentId());
        List<Long> roleIds = resolveRoleIds(req.roleCodes());

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setNickname(req.nickname().trim());
        user.setDepartmentId(req.departmentId());
        user.setStatus(UserStatus.ENABLED);
        userMapper.insert(user);

        saveRoles(user.getId(), roleIds);
        return user.getId();
    }

    @Override
    @Transactional
    public void update(Long id, UpdateUserRequest req) {
        SysUser user = findUser(id);
        validateDepartment(req.departmentId());
        List<Long> roleIds = resolveRoleIds(req.roleCodes());

        user.setNickname(req.nickname().trim());
        user.setDepartmentId(req.departmentId());
        userMapper.updateById(user);

        userRoleMapper.deleteByUserId(id);
        saveRoles(id, roleIds);
    }

    @Override
    public void updateStatus(Long id, UserStatusRequest req) {
        SysUser user = findUser(id);
        user.setStatus(req.status());
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id, ResetPasswordRequest req) {
        SysUser user = findUser(id);
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userMapper.updateById(user);
    }

    private UserPageVo toVo(
            SysUser user,
            Map<Long, String> departmentNames,
            Map<Long, List<String>> roleCodes
    ) {
        return new UserPageVo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getDepartmentId(),
                departmentNames.get(user.getDepartmentId()),
                user.getStatus(),
                roleCodes.getOrDefault(user.getId(), List.of()),
                user.getCreatedAt()
        );
    }

    private SysUser findUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void validateDepartment(Long departmentId) {
        if (departmentMapper.selectById(departmentId) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "部门不存在");
        }
    }

    private List<Long> resolveRoleIds(List<String> roleCodes) {
        List<String> codes = roleCodes.stream()
                .map(String::trim)
                .distinct()
                .toList();
        List<Long> roleIds = roleMapper.selectRoleIdsByCodes(codes);

        if (roleIds.size() != codes.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return roleIds;
    }

    private void saveRoles(Long userId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            userRoleMapper.insert(userId, roleId);
        }
    }
}
