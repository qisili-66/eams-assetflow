package org.example.eams.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eams.common.Result;
import org.example.eams.dto.CreateDepartmentRequest;
import org.example.eams.dto.UpdateDepartmentRequest;
import org.example.eams.service.DepartmentService;
import org.example.eams.vo.DepartmentTreeVo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @GetMapping("/tree")
    public Result<List<DepartmentTreeVo>> getTree() {
        return Result.success(departmentService.getTree());
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@Valid @RequestBody CreateDepartmentRequest req) {
        return Result.success(departmentService.create(req));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest req
    ) {
        departmentService.update(id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success();
    }
}
