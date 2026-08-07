package org.example.eams.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.eams.dto.asset.AssetQuery;
import org.example.eams.dto.asset.AssetSaveRequest;
import org.example.eams.dto.asset.ScrapAssetRequest;
import org.example.eams.entity.Asset;
import org.example.eams.entity.SysUser;
import org.example.eams.enums.AssetStatus;
import org.example.eams.enums.ErrorCode;
import org.example.eams.exception.BusinessException;
import org.example.eams.mapper.AssetMapper;
import org.example.eams.mapper.SysUserMapper;
import org.example.eams.service.AssetService;
import org.example.eams.vo.AssetVo;
import org.example.eams.vo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetMapper assetMapper;
    private final SysUserMapper userMapper;

    @Override
    public PageResult<AssetVo> page(AssetQuery query) {
        Page<Asset> page = assetMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                buildQuery(query)
        );
        Map<Long, String> userNames = loadUserNames(page.getRecords());
        List<AssetVo> records = page.getRecords().stream()
                .map(asset -> toVo(asset, userNames))
                .toList();

        return new PageResult<>(
                page.getCurrent(), page.getSize(), page.getTotal(), records
        );
    }

    @Override
    public AssetVo getById(Long id) {
        Asset asset = findAsset(id);
        return toVo(asset, loadUserNames(List.of(asset)));
    }

    @Override
    public Long create(AssetSaveRequest req) {
        validateAssetNo(req.assetNo(), null);

        Asset asset = new Asset();
        apply(asset, req);
        asset.setStatus(AssetStatus.FREE);
        assetMapper.insert(asset);
        return asset.getId();
    }

    @Override
    public void update(Long id, AssetSaveRequest req) {
        Asset asset = findAsset(id);
        validateAssetNo(req.assetNo(), id);
        apply(asset, req);
        assetMapper.updateById(asset);
    }

    @Override
    public void delete(Long id) {
        Asset asset = findAsset(id);
        if (asset.getStatus() != AssetStatus.FREE) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "只有空闲资产可以删除");
        }
        assetMapper.deleteById(id);
    }

    @Override
    public void scrap(Long id, ScrapAssetRequest req) {
        Asset asset = findAsset(id);
        if (asset.getStatus() != AssetStatus.FREE) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "只有空闲资产可以报废");
        }

        asset.setStatus(AssetStatus.SCRAP);
        asset.setScrapReason(req.reason().trim());
        asset.setScrappedAt(LocalDateTime.now());
        assetMapper.updateById(asset);
    }

    private LambdaQueryWrapper<Asset> buildQuery(AssetQuery query) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<Asset>()
                .orderByDesc(Asset::getId);

        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item
                    .like(Asset::getAssetNo, keyword)
                    .or()
                    .like(Asset::getName, keyword));
        }
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(Asset::getCategory, query.getCategory().trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Asset::getStatus, query.getStatus());
        }
        return wrapper;
    }

    private void apply(Asset asset, AssetSaveRequest req) {
        asset.setAssetNo(req.assetNo().trim());
        asset.setName(req.name().trim());
        asset.setCategory(req.category().trim());
        asset.setPrice(req.price());
        asset.setPurchaseDate(req.purchaseDate());
        asset.setImageUrl(trimToNull(req.imageUrl()));
        asset.setRemark(trimToNull(req.remark()));
    }

    private void validateAssetNo(String assetNo, Long id) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<Asset>()
                .eq(Asset::getAssetNo, assetNo.trim());
        if (id != null) {
            wrapper.ne(Asset::getId, id);
        }
        if (assetMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "资产编号已存在");
        }
    }

    private Asset findAsset(Long id) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资产不存在");
        }
        return asset;
    }

    private Map<Long, String> loadUserNames(Collection<Asset> assets) {
        List<Long> userIds = assets.stream()
                .map(Asset::getCurrentUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));
    }

    private AssetVo toVo(Asset asset, Map<Long, String> userNames) {
        return new AssetVo(
                asset.getId(),
                asset.getAssetNo(),
                asset.getName(),
                asset.getCategory(),
                asset.getPrice(),
                asset.getPurchaseDate(),
                asset.getStatus(),
                asset.getImageUrl(),
                asset.getRemark(),
                asset.getCurrentUserId() == null
                        ? null
                        : userNames.get(asset.getCurrentUserId()),
                asset.getScrapReason(),
                asset.getScrappedAt(),
                asset.getCreatedAt()
        );
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
