package org.example.eams.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.eams.dto.application.ApplicationAuditRequest;
import org.example.eams.dto.application.ApplicationCreateRequest;
import org.example.eams.dto.application.ApplicationQuery;
import org.example.eams.entity.Asset;
import org.example.eams.entity.AssetApplication;
import org.example.eams.entity.AssetHolding;
import org.example.eams.entity.SysUser;
import org.example.eams.enums.ApplicationStatus;
import org.example.eams.enums.AssetStatus;
import org.example.eams.enums.ErrorCode;
import org.example.eams.enums.HoldingStatus;
import org.example.eams.exception.BusinessException;
import org.example.eams.mapper.AssetApplicationMapper;
import org.example.eams.mapper.AssetHoldingMapper;
import org.example.eams.mapper.AssetMapper;
import org.example.eams.mapper.SysUserMapper;
import org.example.eams.service.AssetApplicationService;
import org.example.eams.vo.ApplicationVo;
import org.example.eams.vo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetApplicationServiceImpl implements AssetApplicationService {

    private final AssetApplicationMapper applicationMapper;
    private final AssetHoldingMapper holdingMapper;
    private final AssetMapper assetMapper;
    private final SysUserMapper userMapper;

    @Override
    public Long create(ApplicationCreateRequest req, String username) {
        SysUser user = findUser(username);
        Asset asset = findAsset(req.assetId());
        if (asset.getStatus() != AssetStatus.FREE) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "该资产当前不可申请");
        }
        if (Objects.equals(asset.getCurrentUserId(), user.getId())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "不能申请自己正在使用的资产");
        }

        AssetApplication application = new AssetApplication();
        application.setAssetId(asset.getId());
        application.setApplicantId(user.getId());
        application.setReason(req.reason().trim());
        application.setStatus(ApplicationStatus.WAITING);
        applicationMapper.insert(application);
        return application.getId();
    }

    @Override
    public PageResult<ApplicationVo> pageMy(ApplicationQuery query, String username) {
        return page(query, findUser(username).getId());
    }

    @Override
    public PageResult<ApplicationVo> page(ApplicationQuery query) {
        return page(query, null);
    }

    @Override
    public ApplicationVo getById(Long id, String username, boolean isAdmin) {
        AssetApplication application = findApplication(id);
        if (!isAdmin && !Objects.equals(application.getApplicantId(), findUser(username).getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该申请");
        }
        return toVo(application, loadAssets(List.of(application)), loadUserNames(List.of(application)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, ApplicationAuditRequest req, String username) {
        if (req.decision() != ApplicationStatus.PASS && req.decision() != ApplicationStatus.REJECT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "审核结果只能是 PASS 或 REJECT");
        }

        AssetApplication application = findApplication(id);
        if (application.getStatus() != ApplicationStatus.WAITING) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "该申请已处理");
        }

        SysUser auditor = findUser(username);
        LocalDateTime now = LocalDateTime.now();
        if (req.decision() == ApplicationStatus.REJECT) {
            if (updateApplicationAudit(application, ApplicationStatus.REJECT, auditor.getId(), req.comment(), now) != 1) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "该申请已处理");
            }
            return;
        }

        int updated = assetMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Asset>()
                .eq(Asset::getId, application.getAssetId())
                .eq(Asset::getStatus, AssetStatus.FREE)
                .set(Asset::getStatus, AssetStatus.USING)
                .set(Asset::getCurrentUserId, application.getApplicantId()));
        if (updated != 1) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "资产已被其他申请领用");
        }

        if (updateApplicationAudit(application, ApplicationStatus.PASS, auditor.getId(), req.comment(), now) != 1) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "该申请已处理");
        }
        AssetHolding holding = new AssetHolding();
        holding.setAssetId(application.getAssetId());
        holding.setUserId(application.getApplicantId());
        holding.setApplicationId(application.getId());
        holding.setReceivedAt(now);
        holding.setStatus(HoldingStatus.ACTIVE);
        holdingMapper.insert(holding);
    }

    @Override
    public void cancel(Long id, String username) {
        AssetApplication application = findApplication(id);
        if (!Objects.equals(application.getApplicantId(), findUser(username).getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能取消自己的申请");
        }
        if (application.getStatus() != ApplicationStatus.WAITING) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "只有待审核申请可以取消");
        }
        int updated = applicationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AssetApplication>()
                        .eq(AssetApplication::getId, id)
                        .eq(AssetApplication::getStatus, ApplicationStatus.WAITING)
                        .set(AssetApplication::getStatus, ApplicationStatus.CANCELLED));
        if (updated != 1) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "该申请已处理");
        }
    }

    private PageResult<ApplicationVo> page(ApplicationQuery query, Long applicantId) {
        LambdaQueryWrapper<AssetApplication> wrapper = new LambdaQueryWrapper<AssetApplication>()
                .eq(applicantId != null, AssetApplication::getApplicantId, applicantId)
                .eq(query.getStatus() != null, AssetApplication::getStatus, query.getStatus())
                .orderByDesc(AssetApplication::getId);
        if (applicantId == null && StringUtils.hasText(query.getApplicantKeyword())) {
            String keyword = query.getApplicantKeyword().trim();
            List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                            .and(item -> item.like(SysUser::getUsername, keyword)
                                    .or()
                                    .like(SysUser::getNickname, keyword)))
                    .stream()
                    .map(SysUser::getId)
                    .toList();
            if (userIds.isEmpty()) {
                return new PageResult<>(query.getPage(), query.getSize(), 0, List.of());
            }
            wrapper.in(AssetApplication::getApplicantId, userIds);
        }
        Page<AssetApplication> page = applicationMapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper);
        Map<Long, Asset> assets = loadAssets(page.getRecords());
        Map<Long, String> userNames = loadUserNames(page.getRecords());
        List<ApplicationVo> records = page.getRecords().stream()
                .map(application -> toVo(application, assets, userNames))
                .toList();
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    private int updateApplicationAudit(
            AssetApplication application,
            ApplicationStatus status,
            Long auditorId,
            String comment,
            LocalDateTime auditedAt
    ) {
        return applicationMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AssetApplication>()
                .eq(AssetApplication::getId, application.getId())
                .eq(AssetApplication::getStatus, ApplicationStatus.WAITING)
                .set(AssetApplication::getStatus, status)
                .set(AssetApplication::getAuditorId, auditorId)
                .set(AssetApplication::getAuditComment, trimToNull(comment))
                .set(AssetApplication::getAuditedAt, auditedAt));
    }

    private AssetApplication findApplication(Long id) {
        AssetApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "申请不存在");
        }
        return application;
    }

    private Asset findAsset(Long id) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资产不存在");
        }
        return asset;
    }

    private SysUser findUser(String username) {
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return user;
    }

    private Map<Long, Asset> loadAssets(Collection<AssetApplication> applications) {
        List<Long> assetIds = applications.stream().map(AssetApplication::getAssetId).distinct().toList();
        if (assetIds.isEmpty()) {
            return Map.of();
        }
        return assetMapper.selectByIds(assetIds).stream().collect(Collectors.toMap(Asset::getId, Function.identity()));
    }

    private Map<Long, String> loadUserNames(Collection<AssetApplication> applications) {
        List<Long> userIds = applications.stream()
                .flatMap(application -> java.util.stream.Stream.of(application.getApplicantId(), application.getAuditorId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));
    }

    private ApplicationVo toVo(AssetApplication application, Map<Long, Asset> assets, Map<Long, String> userNames) {
        Asset asset = assets.get(application.getAssetId());
        return new ApplicationVo(
                application.getId(), application.getAssetId(),
                asset == null ? null : asset.getAssetNo(),
                asset == null ? null : asset.getName(),
                asset == null ? null : asset.getCategory(),
                application.getApplicantId(), userNames.get(application.getApplicantId()),
                application.getReason(), application.getStatus(),
                application.getAuditorId(), userNames.get(application.getAuditorId()),
                application.getAuditComment(), application.getAuditedAt(), application.getCreatedAt()
        );
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
