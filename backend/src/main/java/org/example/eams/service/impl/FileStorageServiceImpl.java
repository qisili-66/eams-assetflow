package org.example.eams.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.eams.config.FileStorageProperties;
import org.example.eams.enums.ErrorCode;
import org.example.eams.exception.BusinessException;
import org.example.eams.service.FileStorageService;
import org.example.eams.vo.FileUploadVo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_SIZE = 5L * 1024 * 1024;

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png"
    );

    private final FileStorageProperties properties;

    @Override
    public FileUploadVo uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片不能超过5MB");
        }

        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null || !isImage(file)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持 JPG 或 PNG 图片");
        }

        LocalDate date = LocalDate.now();
        Path root = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
        Path dir = root.resolve(String.valueOf(date.getYear()))
                .resolve(String.format("%02d", date.getMonthValue()));
        String fileName = UUID.randomUUID() + "." + extension;

        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(fileName));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "图片保存失败");
        }

        String url = "/uploads/%d/%02d/%s".formatted(
                date.getYear(), date.getMonthValue(), fileName
        );
        return new FileUploadVo(fileName, url, file.getSize());
    }

    private boolean isImage(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            BufferedImage image = ImageIO.read(input);
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }
}
