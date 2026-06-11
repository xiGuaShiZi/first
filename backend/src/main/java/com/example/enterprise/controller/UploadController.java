package com.example.enterprise.controller;

import com.example.enterprise.common.Result;
import com.example.enterprise.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Map.entry;

/**
 * 文件上传控制器
 * <p>提供管理员图片上传和客户评价媒体上传接口，支持文件类型和大小校验</p>
 */
@RestController
@RequestMapping("/api")
public class UploadController {
    /** 允许的图片后缀 */
    private static final Set<String> IMAGE_SUFFIXES = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
    /** 允许的视频后缀 */
    private static final Set<String> VIDEO_SUFFIXES = Set.of(".mp4", ".webm", ".mov");
    /** 允许的图片Content-Type */
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    /** 允许的视频Content-Type */
    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of("video/mp4", "video/webm", "video/quicktime");
    /** Content-Type到文件后缀的映射，确保存储后缀与实际内容一致 */
    private static final Map<String, String> CONTENT_TYPE_TO_SUFFIX = Map.ofEntries(
            entry("image/jpeg", ".jpg"),
            entry("image/png", ".png"),
            entry("image/webp", ".webp"),
            entry("image/gif", ".gif"),
            entry("video/mp4", ".mp4"),
            entry("video/webm", ".webm"),
            entry("video/quicktime", ".mov")
    );
    /** 管理员图片上传最大大小：5MB */
    private static final long ADMIN_IMAGE_MAX_SIZE = 5 * 1024 * 1024;
    /** 评价媒体上传最大大小：50MB */
    private static final long REVIEW_MEDIA_MAX_SIZE = 50 * 1024 * 1024;

    /** 上传文件存储目录 */
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    /** 图片URL前缀 */
    @Value("${app.public-url-prefix:/uploads/}")
    private String publicUrlPrefix;

    /** 管理员图片上传，仅支持图片格式，最大5MB */
    @PostMapping("/admin/upload")
    public Result<Map<String, String>> adminImage(@RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(store(file, IMAGE_SUFFIXES, IMAGE_CONTENT_TYPES, ADMIN_IMAGE_MAX_SIZE, "仅支持 jpg、png、webp、gif 图片，且大小不超过 5MB"));
    }

    /** 客户评价媒体上传，支持图片和视频格式，最大50MB */
    @PostMapping("/user/review-media")
    public Result<Map<String, String>> reviewMedia(@RequestParam("file") MultipartFile file) throws IOException {
        Set<String> suffixes = new java.util.HashSet<>(IMAGE_SUFFIXES);
        suffixes.addAll(VIDEO_SUFFIXES);
        Set<String> contentTypes = new java.util.HashSet<>(IMAGE_CONTENT_TYPES);
        contentTypes.addAll(VIDEO_CONTENT_TYPES);
        return Result.success(store(file, suffixes, contentTypes, REVIEW_MEDIA_MAX_SIZE, "仅支持图片或 mp4、webm、mov 视频，且大小不超过 50MB"));
    }

    /** 用户通用图片上传，仅支持图片格式，最大5MB */
    @PostMapping("/user/upload")
    public Result<Map<String, String>> userImage(@RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(store(file, IMAGE_SUFFIXES, IMAGE_CONTENT_TYPES, ADMIN_IMAGE_MAX_SIZE, "仅支持 jpg、png、webp、gif 图片，且大小不超过 5MB"));
    }

    /** 商家证件图片上传，仅支持图片格式，最大5MB */
    @PostMapping("/merchant/upload")
    public Result<Map<String, String>> merchantDocument(@RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(store(file, IMAGE_SUFFIXES, IMAGE_CONTENT_TYPES, ADMIN_IMAGE_MAX_SIZE, "仅支持 jpg、png、webp、gif 图片，且大小不超过 5MB"));
    }

    /**
     * 通用文件存储逻辑：校验文件大小、类型、后缀，生成UUID文件名，防止路径遍历
     * @param file 上传文件
     * @param suffixes 允许的后缀集合
     * @param contentTypes 允许的Content-Type集合
     * @param maxSize 最大文件大小
     * @param errorMessage 校验失败时的错误提示
     * @return 包含文件URL的Map
     */
    private Map<String, String> store(MultipartFile file, Set<String> suffixes, Set<String> contentTypes, long maxSize, String errorMessage) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(errorMessage);
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentTypes.contains(contentType)) {
            throw new BusinessException(errorMessage);
        }

        String suffix = CONTENT_TYPE_TO_SUFFIX.getOrDefault(contentType, "");
        if (suffix.isEmpty() || !suffixes.contains(suffix)) {
            throw new BusinessException(errorMessage);
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + suffix;
        Path targetDir = Path.of(uploadDir).toAbsolutePath().normalize();
        Path targetFile = targetDir.resolve(filename).normalize();
        if (!targetFile.startsWith(targetDir)) {
            throw new BusinessException("上传路径不合法");
        }
        Files.createDirectories(targetDir);
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        return Map.of("url", publicUrlPrefix + filename);
    }
}
