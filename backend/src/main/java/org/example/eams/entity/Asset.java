package org.example.eams.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.example.eams.enums.AssetStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("asset")
public class Asset {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("asset_no")
    private String assetNo;

    private String name;
    private String category;
    private BigDecimal price;

    @TableField("purchase_date")
    private LocalDate purchaseDate;

    private AssetStatus status;

    @TableField("current_user_id")
    private Long currentUserId;

    @TableField("image_url")
    private String imageUrl;

    private String remark;

    @TableField("scrap_reason")
    private String scrapReason;

    @TableField("scrapped_at")
    private LocalDateTime scrappedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
