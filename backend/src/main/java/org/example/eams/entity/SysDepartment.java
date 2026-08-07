package org.example.eams.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_department")
public class SysDepartment {

    @TableId(type= IdType.AUTO)
    private Long id;

    private Long parentId;

    private String name;

    private Integer sortNo;
}
