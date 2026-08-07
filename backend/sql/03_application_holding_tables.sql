USE eams;

CREATE TABLE IF NOT EXISTS asset_application (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    reason VARCHAR(500) NOT NULL COMMENT '申请原因',
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/PASS/REJECT/CANCELLED',
    auditor_id BIGINT NULL COMMENT '审核人ID',
    audit_comment VARCHAR(500) NULL COMMENT '审核意见',
    audited_at DATETIME NULL COMMENT '审核时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_application_asset_status (asset_id, status),
    KEY idx_application_applicant_id (applicant_id),
    KEY idx_application_status_created_at (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产申请';

CREATE TABLE IF NOT EXISTS asset_holding (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    user_id BIGINT NOT NULL COMMENT '使用人ID',
    application_id BIGINT NULL COMMENT '来源申请ID',
    received_at DATETIME NOT NULL COMMENT '领用时间',
    returned_at DATETIME NULL COMMENT '归还时间',
    return_remark VARCHAR(500) NULL COMMENT '归还备注',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/RETURNED',
    PRIMARY KEY (id),
    KEY idx_holding_user_status (user_id, status),
    KEY idx_holding_asset_status (asset_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产领用记录';
