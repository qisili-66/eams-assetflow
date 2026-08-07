package org.example.eams.config;

import lombok.RequiredArgsConstructor;
import org.example.eams.entity.SysUser;
import org.example.eams.enums.UserStatus;
import org.example.eams.mapper.AuthRoleMapper;
import org.example.eams.mapper.DepartmentMapper;
import org.example.eams.mapper.SysUserMapper;
import org.example.eams.mapper.UserRoleMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalUserInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final AuthRoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long departmentId = departmentMapper.selectIdByName("Technology Department");

        if (departmentId == null) {
            throw new IllegalStateException("未找到 Technology Department，请先执行 01_auth_tables.sql");
        }

        ensureUser(
                "admin",
                "Admin@123",
                "System Administrator",
                departmentId,
                "ADMIN"
        );

        ensureUser(
                "user",
                "User@123",
                "Demo User",
                departmentId,
                "USER"
        );
    }

    private void ensureUser(
            String username,
            String rawPassword,
            String nickname,
            Long departmentId,
            String roleCode
    ) {
        SysUser user = userMapper.selectByUsername(username);

        if (user == null) {
            user = new SysUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setNickname(nickname);
            user.setDepartmentId(departmentId);
            user.setStatus(UserStatus.ENABLED);
            userMapper.insert(user);
        }

        Long roleId = roleMapper.selectRoleIdByRoleCode(roleCode);

        if (roleId == null) {
            throw new IllegalStateException("未找到角色：" + roleCode);
        }

        if (userRoleMapper.countByUserIdAndRoleId(user.getId(), roleId) == 0) {
            userRoleMapper.insert(user.getId(), roleId);
        }
    }
}