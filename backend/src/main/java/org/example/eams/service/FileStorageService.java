package org.example.eams.service;

import org.example.eams.vo.FileUploadVo;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadVo uploadImage(MultipartFile file);
}
