package org.example.eams.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.eams.vo.UserRoleRow;

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

    @Select("""
            <script>
            SELECT id
            FROM sys_role
            WHERE role_code IN
            <foreach collection="codes" item="code" open="(" separator="," close=")">
                #{code}
            </foreach>
            </script>
            """)
    List<Long> selectRoleIdsByCodes(@Param("codes") List<String> codes);

    @Select("""
            <script>
            SELECT ur.user_id AS user_id, r.role_code AS role_code
            FROM sys_user_role ur
            INNER JOIN sys_role r ON r.id = ur.role_id
            WHERE ur.user_id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            ORDER BY r.role_code
            </script>
            """)
    List<UserRoleRow> selectRoleRowsByUserIds(@Param("ids") List<Long> ids);
}
