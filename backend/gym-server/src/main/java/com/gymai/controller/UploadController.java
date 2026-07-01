package com.gymai.controller;

import com.gymai.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload.dir:}")
    private String uploadDir;

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件为空");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("仅支持图片格式");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("图片大小不能超过5MB");
            }

            Path basePath;
            if (uploadDir != null && !uploadDir.isEmpty()) {
                basePath = Paths.get(uploadDir);
            } else {
                basePath = Paths.get(System.getProperty("user.dir"), "uploads");
            }
            Path avatarPath = basePath.resolve("avatars");

            if (!Files.exists(avatarPath)) {
                Files.createDirectories(avatarPath);
            }

            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + ext;
            Path target = avatarPath.resolve(filename);
            file.transferTo(target.toFile());

            String url = "/uploads/avatars/" + filename;
            return Result.ok(url);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".png";
        return filename.substring(filename.lastIndexOf("."));
    }
}
