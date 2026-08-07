USE eams;

CREATE TABLE IF NOT EXISTS asset (
                                     id BIGINT NOT NULL AUTO_INCREMENT COMMENT '资产ID',
                                     asset_no VARCHAR(50) NOT NULL COMMENT '资产编号',
    name VARCHAR(100) NOT NULL COMMENT '资产名称',
    category VARCHAR(30) NOT NULL COMMENT '资产分类',
    price DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '购入价格',
    purchase_date DATE NOT NULL COMMENT '购入日期',
    status VARCHAR(20) NOT NULL DEFAULT 'FREE' COMMENT 'FREE/USING/REPAIR/SCRAP',
    current_user_id BIGINT NULL COMMENT '当前使用人ID',
    image_url VARCHAR(500) NULL COMMENT '图片地址',
    remark VARCHAR(500) NULL COMMENT '备注',
    scrap_reason VARCHAR(500) NULL COMMENT '报废原因',
    scrapped_at DATETIME NULL COMMENT '报废时间',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_asset_asset_no (asset_no),
    KEY idx_asset_status (status),
    KEY idx_asset_category (category),
    KEY idx_asset_current_user_id (current_user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci COMMENT='固定资产';
