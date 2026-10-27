package com.trace.file.controller;

import com.trace.common.result.R;
import com.trace.file.service.FileStoreService;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储接口（农事现场照片等）
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Resource
    private FileStoreService fileStoreService;

    @PostMapping("/upload")
    public R<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "bizType", defaultValue = "FARMING") String bizType) {
        try {
            String fileId = UUID.randomUUID().toString().replace("-", "");
            String url = fileStoreService.save(fileId, file.getOriginalFilename(), file.getBytes());
            FileUploadVO vo = new FileUploadVO();
            vo.setFileId(fileId);
            vo.setUrl(url);
            vo.setOriginalName(file.getOriginalFilename());
            return R.ok(vo);
        } catch (IOException e) {
            return R.fail("文件上传失败:" + e.getMessage());
        }
    }

    @GetMapping("/{fileId}")
    public org.springframework.http.ResponseEntity<byte[]> download(@PathVariable("fileId") String fileId) {
        FileStoreService.FileEntry entry = fileStoreService.load(fileId);
        if (entry == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Type", entry.getContentType())
                .body(entry.getContent());
    }

    public static class FileUploadVO {
        private String fileId;
        private String url;
        private String originalName;

        public String getFileId() { return fileId; }
        public void setFileId(String fileId) { this.fileId = fileId; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }
    }
}
