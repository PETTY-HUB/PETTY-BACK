package io.github.petty.community.controller;

import io.github.petty.community.util.SupabaseUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class PostImageUploadController {

    private final SupabaseUploader supabaseUploader;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");

    // ✅ 단일 이미지 업로드
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다"));
        }

        // 파일 유효성 검사
        if (!isValidImage(file)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "유효하지 않은 파일",
                    "message", "5MB 이하의 jpg, jpeg, png, gif 파일만 업로드 가능합니다"
            ));
        }

        try {
            String imageUrl = supabaseUploader.upload(file);
            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (IOException e) {
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "업로드 실패", "message", e.getMessage()));
        }
    }

    // ✅ 다중 이미지 업로드
    @PostMapping("/upload/multi")
    public ResponseEntity<?> uploadMultipleImages(
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다"));
        }

        // 파일 수 제한
        if (files.size() > 5) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "이미지 제한 초과",
                    "message", "최대 5개의 이미지만 업로드 가능합니다"
            ));
        }

        // 모든 파일의 유효성 검사
        for (MultipartFile file : files) {
            if (!isValidImage(file)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "유효하지 않은 파일",
                        "message", "5MB 이하의 jpg, jpeg, png, gif 파일만 업로드 가능합니다"
                ));
            }
        }

        List<Map<String, Object>> imageResponses = new ArrayList<>();

        try {
            int order = 0;
            for (MultipartFile file : files) {
                String url = supabaseUploader.upload(file);

                Map<String, Object> imageMap = new HashMap<>();
                imageMap.put("imageUrl", url);
                imageMap.put("ordering", order++);
                imageMap.put("isDeleted", false);

                imageResponses.add(imageMap);
            }
            return ResponseEntity.ok(Map.of("images", imageResponses));
        } catch (IOException e) {
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "이미지 업로드 실패", "message", e.getMessage()));
        }
    }

    private boolean isValidImage(MultipartFile file) {
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}