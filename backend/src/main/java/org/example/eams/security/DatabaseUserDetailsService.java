package org.example.eams.security;

import lombok.RequiredArgsConstructor;
import org.example.eams.entity.SysUser;
import org.example.eams.enums.UserStatus;
import org.example.eams.mapper.AuthRoleMapper;
import org.example.eams.mapper.SysUserMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final AuthRoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("账号不存在");
        }
        List<GrantedAuthority> authorities = roleMapper
                .selectRoleCodesByUserId(user.getId())
                .stream()
                .map(roleCode -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + roleCode))
                .toList();


        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(user.getStatus() != UserStatus.ENABLED)
                .authorities(authorities)
                .build();
    }
}

