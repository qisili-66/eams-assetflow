package org.example.eams.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.eams.dto.auth.LoginRequest;
import org.example.eams.entity.SysUser;
import org.example.eams.enums.ErrorCode;
import org.example.eams.enums.UserStatus;
import org.example.eams.exception.BusinessException;
import org.example.eams.mapper.AuthRoleMapper;
import org.example.eams.mapper.DepartmentMapper;
import org.example.eams.mapper.SysUserMapper;
import org.example.eams.security.JwtTokenProvider;
import org.example.eams.service.AuthService;
import org.example.eams.vo.CurrentUserVo;
import org.example.eams.vo.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final SysUserMapper userMapper;
    private final AuthRoleMapper roleMapper;
    private final DepartmentMapper departmentMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request){
        try{
            Authentication authentication
                    = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.username(),
                                    request.password()
                            )
            );
            String username = authentication.getName();
            CurrentUserVo user = getCurrentUser(username);
            String token = jwtTokenProvider.createToken(username);

            return new LoginResponse(
                    token,
                    "Bearer",
                    jwtTokenProvider.getExpirationSeconds(),
                    user
            );
        }catch(DisabledException e){
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "账号已被禁用"
            );

        }catch(BadCredentialsException e){
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "账号或者密码错误"
            );
        }
    }
    @Override
    public CurrentUserVo getCurrentUser(String username) {
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        if(user.getStatus()!= UserStatus.ENABLED){
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已经被禁用");
        }
        List<String>roles=
                roleMapper.selectRoleCodesByUserId(user.getId());

        String departmentName=
                departmentMapper.selectNameById(user.getDepartmentId());
        return new CurrentUserVo(
            user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getDepartmentId(),
                departmentName,
                roles,
                List.of()
        );
    }
}
