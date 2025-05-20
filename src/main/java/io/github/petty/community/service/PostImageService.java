package io.github.petty.community.service;

import io.github.petty.community.dto.PostImageRequest;
import io.github.petty.community.dto.PostImageResponse;
import io.github.petty.community.entity.Post;

import java.util.List;

public interface PostImageService {

    // ✅ 이미지 저장 (신규 게시글 생성 시)
    void saveImages(Post post, List<PostImageRequest> imageRequests);

    // ✅ 이미지 삭제 (게시글 삭제 시)
    void deleteImagesByPostId(Long postId);

    // ✅ 이미지 하나 삭제
    void deleteImage(Long imageId);

    // ✅ 이미지 목록 조회 (상세 조회용)
    List<PostImageResponse> findImageResponsesByPostId(Long postId);

    // ✅ 이미지 순서 변경
    void reorderImages(List<Long> orderedImageIds);

    // ✅ 수정 요청 처리 (isDeleted, ordering 포함)
    void updateImagesFromRequest(Post post, List<PostImageRequest> imageRequests);
}
