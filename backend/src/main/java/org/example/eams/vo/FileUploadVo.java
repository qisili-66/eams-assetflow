package org.example.eams.vo;

public record FileUploadVo(
        String fileName,
        String url,
        long size
) {
}
