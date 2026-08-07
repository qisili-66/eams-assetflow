package org.example.eams.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eams.common.Result;
import org.example.eams.dto.application.ApplicationAuditRequest;
import org.example.eams.dto.application.ApplicationCreateRequest;
import org.example.eams.dto.application.ApplicationQuery;
import org.example.eams.service.AssetApplicationService;
import org.example.eams.vo.ApplicationVo;
import org.example.eams.vo.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asset-applications")
@RequiredArgsConstructor
public class AssetApplicationController {

    private final AssetApplicationService applicationService;

    @PostMapping
    public Result<Long> create(
            @Valid @RequestBody ApplicationCreateRequest req,
            Authentication authentication
    ) {
        return Result.success(applicationService.create(req, authentication.getName()));
    }

    @GetMapping("/my")
    public Result<PageResult<ApplicationVo>> pageMy(
            @Valid ApplicationQuery query,
            Authentication authentication
    ) {
        return Result.success(applicationService.pageMy(query, authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<ApplicationVo>> page(@Valid ApplicationQuery query) {
        return Result.success(applicationService.page(query));
    }

    @GetMapping("/{id}")
    public Result<ApplicationVo> getById(@PathVariable Long id, Authentication authentication) {
        return Result.success(applicationService.getById(id, authentication.getName(), isAdmin(authentication)));
    }

    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> audit(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationAuditRequest req,
            Authentication authentication
    ) {
        applicationService.audit(id, req, authentication.getName());
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, Authentication authentication) {
        applicationService.cancel(id, authentication.getName());
        return Result.success();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
