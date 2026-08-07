package org.example.eams.dto.asset;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.example.eams.enums.AssetStatus;

@Getter
@Setter
public class AssetQuery {
    @Min(value = 1, message = "页码不能小于1")
    private long page = 1;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private long size = 10;

    private String keyword;
    private String category;
    private AssetStatus status;
}
