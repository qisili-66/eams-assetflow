package org.example.eams.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.example.eams.enums.ApplicationStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("asset_application")
public class AssetApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("asset_id")
    private Long assetId;

    @TableField("applicant_id")
    private Long applicantId;

    private String reason;
    private ApplicationStatus status;

    @TableField("auditor_id")
    private Long auditorId;

    @TableField("audit_comment")
    private String auditComment;

    @TableField("audited_at")
    private LocalDateTime auditedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
