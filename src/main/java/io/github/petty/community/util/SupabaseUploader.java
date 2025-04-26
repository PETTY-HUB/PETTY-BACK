package io.github.petty.community.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SupabaseUploader {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    public String upload(MultipartFile file) throws IOException {
        log.info("Uploading file: {}", file.getOriginalFilename());
        log.info("Supabase URL: {}", supabaseUrl);
        log.info("Bucket name: {}", bucketName);

        // 1. 고유한 파일 이름 생성
        String rawFilename = UUID.randomUUID() + "." + getExtension(file.getOriginalFilename());
        String encodedFilename = URLEncoder.encode(rawFilename, StandardCharsets.UTF_8);

        // 실제 업로드 시엔 인코딩된 이름 사용
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + encodedFilename;

        // 업로드 성공 후 접근용 URL - 인코딩된 파일명 그대로 사용해야 함
        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + encodedFilename;

        // 2. 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Authorization", "Bearer " + supabaseKey);

        // 3. 요청 본문
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        try {
            // 4. 업로드 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                log.info("Upload successful: {}", publicUrl);
                return publicUrl;
            } else {
                log.error("Upload failed with status: {}, response: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("이미지 업로드 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error uploading to Supabase: ", e);
            throw new IOException("Supabase 업로드 중 오류 발생: " + e.getMessage());
        }
    }

    private String getExtension(String originalFilename) {
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }

    public void delete(String imageUrl) {
        String filename = extractFilename(imageUrl);
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filename;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Supabase 삭제 실패: {} - {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Error deleting from Supabase: ", e);
        }
    }

    private String extractFilename(String imageUrl) {
        // https://teuocihdergygykvrqck.supabase.co/storage/v1/object/public/post-images/파일명.png
        return imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
    }
}