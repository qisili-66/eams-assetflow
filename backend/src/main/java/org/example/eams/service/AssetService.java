package org.example.eams.service;

import org.example.eams.dto.asset.AssetQuery;
import org.example.eams.dto.asset.AssetSaveRequest;
import org.example.eams.dto.asset.ScrapAssetRequest;
import org.example.eams.dto.holding.ReturnAssetRequest;
import org.example.eams.vo.AssetVo;
import org.example.eams.vo.PageResult;

import java.util.List;

public interface AssetService {

    PageResult<AssetVo> page(AssetQuery query);

    AssetVo getById(Long id);

    Long create(AssetSaveRequest req);

    void update(Long id, AssetSaveRequest req);

    void delete(Long id);

    void scrap(Long id, ScrapAssetRequest req);

    List<AssetVo> myAssets(String username);

    void returnAsset(Long id, ReturnAssetRequest req, String username, boolean isAdmin);
}
