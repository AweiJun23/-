package com.trace.file.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 本地文件存储（生产环境可替换为 OSS / MinIO）
 */
@Service
public class FileStoreService {

    private static final Logger log = LoggerFactory.getLogger(FileStoreService.class);

    private static final String ROOT = "./uploads";
    private final Map<String, FileEntry> index = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(ROOT));
    }

    public String save(String fileId, String originalName, byte[] content) throws IOException {
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String stored = fileId + ext;
        Path path = Paths.get(ROOT, stored);
        Files.write(path, content);
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        index.put(fileId, new FileEntry(content, contentType, originalName));
        return "/api/file/" + fileId;
    }

    public FileEntry load(String fileId) {
        return index.get(fileId);
    }

    public static class FileEntry {
        private final byte[] content;
        private final String contentType;
        private final String originalName;

        public FileEntry(byte[] content, String contentType, String originalName) {
            this.content = content;
            this.contentType = contentType;
            this.originalName = originalName;
        }

        public byte[] getContent() { return content; }
        public String getContentType() { return contentType; }
        public String getOriginalName() { return originalName; }
    }
}
