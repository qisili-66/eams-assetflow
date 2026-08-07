package org.example.eams.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eams.common.Result;
import org.example.eams.dto.asset.AssetQuery;
import org.example.eams.dto.asset.AssetSaveRequest;
import org.example.eams.dto.asset.ScrapAssetRequest;
import org.example.eams.service.AssetService;
import org.example.eams.vo.AssetVo;
import org.example.eams.vo.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public Result<PageResult<AssetVo>> page(@Valid AssetQuery query) {
        return Result.success(assetService.page(query));
    }

    @GetMapping("/{id}")
    public Result<AssetVo> getById(@PathVariable Long id) {
        return Result.success(assetService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@Valid @RequestBody AssetSaveRequest req) {
        return Result.success(assetService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody AssetSaveRequest req
    ) {
        assetService.update(id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return Result.success();
    }

    @PatchMapping("/{id}/scrap")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> scrap(
            @PathVariable Long id,
            @Valid @RequestBody ScrapAssetRequest req
    ) {
        assetService.scrap(id, req);
        return Result.success();
    }

}
