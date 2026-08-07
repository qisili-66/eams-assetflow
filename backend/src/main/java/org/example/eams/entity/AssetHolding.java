package org.example.eams.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.example.eams.enums.HoldingStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("asset_holding")
public class AssetHolding {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("asset_id")
    private Long assetId;

    @TableField("user_id")
    private Long userId;

    @TableField("application_id")
    private Long applicationId;

    @TableField("received_at")
    private LocalDateTime receivedAt;

    @TableField("returned_at")
    private LocalDateTime returnedAt;

    @TableField("return_remark")
    private String returnRemark;

    private HoldingStatus status;
}
