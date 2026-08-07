package org.example.eams.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface UserRoleMapper {

    @Select("""
            SELECT COUNT(1)
            FROM sys_user_role
            WHERE user_id = #{userId}
              AND role_id = #{roleId}
            """)
    int countByUserIdAndRoleId(
            @Param("userId") Long userId,
            @Param("roleId") Long roleId
    );

    @Insert("""
            INSERT INTO sys_user_role (user_id, role_id)
            VALUES (#{userId}, #{roleId})
            """)
    int insert(
            @Param("userId") Long userId,
            @Param("roleId") Long roleId
    );

    @Delete("""
            DELETE FROM sys_user_role
            WHERE user_id = #{userId}
            """)
    int deleteByUserId(@Param("userId") Long userId);
}

