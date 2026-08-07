package org.example.eams.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.eams.dto.CreateDepartmentRequest;
import org.example.eams.dto.UpdateDepartmentRequest;
import org.example.eams.entity.SysDepartment;
import org.example.eams.entity.SysUser;
import org.example.eams.enums.ErrorCode;
import org.example.eams.exception.BusinessException;
import org.example.eams.mapper.DepartmentMapper;
import org.example.eams.mapper.SysUserMapper;
import org.example.eams.service.DepartmentService;
import org.example.eams.vo.DepartmentTreeVo;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<DepartmentTreeVo> getTree() {
        List<SysDepartment> list = departmentMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>()
                        .orderByAsc(SysDepartment::getSortNo)
                        .orderByAsc(SysDepartment::getId)
        );

        Map<Long, DepartmentTreeVo> nodes = new LinkedHashMap<>();
        for (SysDepartment dept : list) {
            nodes.put(
                    dept.getId(),
                    new DepartmentTreeVo(
                            dept.getId(),
                            dept.getName(),
                            dept.getParentId(),
                            dept.getSortNo(),
                            new ArrayList<>()
                    )
            );
        }

        List<DepartmentTreeVo> roots = new ArrayList<>();
        for (DepartmentTreeVo node : nodes.values()) {
            if (Long.valueOf(0).equals(node.parentId())) {
                roots.add(node);
                continue;
            }

            DepartmentTreeVo parent = nodes.get(node.parentId());
            if (parent == null) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "部门数据存在无效父级关系"
                );
            }

            parent.children().add(node);
        }

        return roots;
    }

    @Override
    public Long create(CreateDepartmentRequest req) {
        String name = req.name().trim();

        validateParent(null, req.parentId());
        validateName(null, req.parentId(), name);

        SysDepartment dept = new SysDepartment();
        dept.setName(name);
        dept.setParentId(req.parentId());
        dept.setSortNo(req.sortNo());
        departmentMapper.insert(dept);

        return dept.getId();
    }

    @Override
    public void update(Long id, UpdateDepartmentRequest req) {
        SysDepartment dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "部门不存在");
        }

        String name = req.name().trim();
        validateParent(id, req.parentId());
        validateName(id, req.parentId(), name);

        dept.setName(name);
        dept.setParentId(req.parentId());
        dept.setSortNo(req.sortNo());
        departmentMapper.updateById(dept);
    }

    @Override
    public void delete(Long id) {
        if (departmentMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "部门不存在");
        }

        Long childCount = departmentMapper.selectCount(
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "存在子部门，不能删除");
        }

        Long userCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDepartmentId, id)
        );
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "部门下存在用户，不能删除");
        }

        departmentMapper.deleteById(id);
    }

    private void validateName(Long id, Long parentId, String name) {
        LambdaQueryWrapper<SysDepartment> wrapper =
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getParentId, parentId)
                        .eq(SysDepartment::getName, name);

        if (id != null) {
            wrapper.ne(SysDepartment::getId, id);
        }

        if (departmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "同一父部门下名称不能重复"
            );
        }
    }

    private void validateParent(Long id, Long parentId) {
        if (Long.valueOf(0).equals(parentId)) {
            return;
        }

        if (id != null && id.equals(parentId)) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "部门不能设置自身为父部门"
            );
        }

        SysDepartment parent = departmentMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "父部门不存在");
        }

        if (id != null && isDescendant(id, parent)) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "部门不能移动到自己的子部门下"
            );
        }
    }

    private boolean isDescendant(Long id, SysDepartment dept) {
        Set<Long> ids = new HashSet<>();

        while (true) {
            if (!ids.add(dept.getId())) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "部门层级数据存在循环关系"
                );
            }

            if (id.equals(dept.getId())) {
                return true;
            }

            if (Long.valueOf(0).equals(dept.getParentId())) {
                return false;
            }

            dept = departmentMapper.selectById(dept.getParentId());
            if (dept == null) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "部门数据存在无效父级关系"
                );
            }
        }
    }
}