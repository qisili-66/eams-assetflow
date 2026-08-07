package org.example.eams.service;

import org.example.eams.dto.department.CreateDepartmentRequest;
import org.example.eams.dto.department.UpdateDepartmentRequest;
import org.example.eams.vo.DepartmentTreeVo;

import java.util.List;

public interface DepartmentService {
    List<DepartmentTreeVo>getTree();
    Long create(CreateDepartmentRequest req);
    void update(Long id, UpdateDepartmentRequest req);
    void delete(Long id);
}
