package org.example.eams.vo;

import java.util.List;

public record DepartmentTreeVo (Long id,
                                String name,
                                Long parentId,
                                Integer sortNo,
                                List<DepartmentTreeVo> children)
{

}
