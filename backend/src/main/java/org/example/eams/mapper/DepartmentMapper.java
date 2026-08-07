package org.example.eams.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.eams.entity.SysDepartment;

@Mapper
public interface DepartmentMapper extends BaseMapper<SysDepartment> {

    @Select("""
            SELECT name
            FROM sys_department
            WHERE id = #{departmentId}
            """)
    String selectNameById(@Param("departmentId") Long departmentId);

    @Select("""
            SELECT id
            FROM sys_department
            WHERE name = #{name}
            LIMIT 1
            """)
    Long selectIdByName(@Param("name") String name);
}