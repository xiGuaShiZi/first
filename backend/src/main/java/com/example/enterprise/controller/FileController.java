package com.example.enterprise.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件访问控制器
 * <p>替代 ResourceHandler，直接读取上传目录中的文件并返回给客户端。
 * 相比静态资源映射更可靠，避免了路径解析和平台兼容性问题。</p>
 */
@Controller
public class FileController {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    /** 匹配 /uploads/ 下的所有文件，包括带后缀的文件名 */
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        Path baseDir = Path.of(uploadDir).toAbsolutePath().normalize();
        Path file = baseDir.resolve(filename).normalize();

        // 防止路径遍历攻击
        if (!file.startsWith(baseDir)) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(file) || !Files.isReadable(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());
        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
