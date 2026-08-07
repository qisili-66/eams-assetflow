package org.example.eams.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuthRoleMapper {

    @Select("""
              SELECT r.role_code
              FROM sys_role r 
              INNER JOIN sys_user_role ur ON ur.role_id=r.id
              WHERE ur.user_id=#{userId}
              ORDER BY r.role_code
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select(
            """
                             SELECT id
                             FROM sys_role
                             WHERE role_code=#{roleCode}
                    """
    )
    Long selectRoleIdByRoleCode(@Param("roleCode") String roleCode);
}
