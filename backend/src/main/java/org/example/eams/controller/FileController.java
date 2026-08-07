package org.example.eams.controller;

import lombok.RequiredArgsConstructor;
import org.example.eams.common.Result;
import org.example.eams.service.FileStorageService;
import org.example.eams.vo.FileUploadVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/images")
    public Result<FileUploadVo> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(fileStorageService.uploadImage(file));
    }
}
