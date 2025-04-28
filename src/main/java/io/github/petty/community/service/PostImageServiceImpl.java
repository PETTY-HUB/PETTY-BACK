package io.github.petty.community.service;

import io.github.petty.community.dto.PostImageRequest;
import io.github.petty.community.dto.PostImageResponse;
import io.github.petty.community.entity.Post;
import io.github.petty.community.entity.PostImage;
import io.github.petty.community.repository.PostImageRepository;
import io.github.petty.community.util.SupabaseUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostImageServiceImpl implements PostImageService {

    private final PostImageRepository postImageRepository;
    private final SupabaseUploader supabaseUploader;

    @Override
    public void saveImages(Post post, List<PostImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) return;

        for (PostImageRequest dto : imageRequests) {
            PostImage image = PostImage.builder()
                    .imageUrl(dto.getImageUrl())
                    .ordering(dto.getOrdering())
                    .post(post)
                    .build();
            postImageRepository.save(image);
        }
    }

    @Override
    public void deleteImagesByPostId(Long postId) {
        postImageRepository.deleteByPostId(postId);
    }

    @Override
    public void deleteImage(Long imageId) {
        postImageRepository.deleteById(imageId);
    }

    @Override
    public List<PostImageResponse> findImageResponsesByPostId(Long postId) {
        return postImageRepository.findByPostIdOrderByOrderingAsc(postId).stream()
                .map(img -> new PostImageResponse(img.getId(), img.getImageUrl(), img.getOrdering()))
                .toList();
    }

    @Override
    @Transactional
    public void reorderImages(List<Long> orderedImageIds) {
        for (int i = 0; i < orderedImageIds.size(); i++) {
            Long imageId = orderedImageIds.get(i);
            PostImage image = postImageRepository.findById(imageId)
                    .orElseThrow(() -> new IllegalArgumentException("이미지 ID가 잘못되었습니다."));
            image.setOrdering(i);  // 새로운 순서로 업데이트
        }
    }

    @Override
    @Transactional
    public void updateImagesFromRequest(Post post, List<PostImageRequest> imageRequests) {
        for (PostImageRequest dto : imageRequests) {
            if (Boolean.TRUE.equals(dto.getIsDeleted())) {
                if (dto.getId() != null) {
                    PostImage image = postImageRepository.findById(dto.getId())
                            .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다."));
                    postImageRepository.deleteById(dto.getId());
                    supabaseUploader.delete(image.getImageUrl()); // ✅ Supabase에서 삭제
                }
            } else if (dto.getId() != null) {
                PostImage image = postImageRepository.findById(dto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다."));
                image.setImageUrl(dto.getImageUrl());
                image.setOrdering(dto.getOrdering());
            } else {
                PostImage newImage = new PostImage();
                newImage.setImageUrl(dto.getImageUrl());
                newImage.setOrdering(dto.getOrdering());
                newImage.setPost(post);
                postImageRepository.save(newImage);
            }
        }
    }
}

