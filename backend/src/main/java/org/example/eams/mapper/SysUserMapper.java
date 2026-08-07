package org.example.eams.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.eams.entity.SysUser;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("""
                  SELECT id,username,password,nickname,department_id, status,created_at,updated_at
                  FROM sys_user
                  WHERE username=#{username}
                  LIMIT 1
            """)
    SysUser selectByUsername(@Param("username") String username);

}
