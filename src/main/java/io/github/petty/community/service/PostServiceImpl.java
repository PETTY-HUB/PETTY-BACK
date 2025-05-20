package io.github.petty.community.service;

import io.github.petty.community.dto.*;
import io.github.petty.community.entity.Post;
import io.github.petty.community.entity.PostLike;
import io.github.petty.community.enums.PetType;
import io.github.petty.community.repository.PostLikeRepository;
import io.github.petty.community.repository.PostRepository;
import io.github.petty.users.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostImageService postImageService;
    private final PostLikeRepository postLikeRepository;

    @Override
    public Long save(PostRequest request, Users user) {
        Post post = new Post();
        post.setUser(user);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setPostType(request.getPostType());
        post.setPetName(request.getPetName());
        if (request.getPetType() != null && !request.getPetType().isBlank()) {
            post.setPetType(PetType.valueOf(request.getPetType()));
        } else {
            post.setPetType(null);
        }
        post.setRegion(request.getRegion());
        post.setIsResolved(request.getIsResolved());
        post.setLikeCount(0);
        postRepository.save(post);

        postImageService.saveImages(post, request.getImages());

        return post.getId();
    }

    @Override
    @Transactional
    public void update(Long id, PostRequest request, Users user) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        System.out.println("🛠 기존 Post: " + post);
        if (!post.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setPetName(request.getPetName());
        if (request.getPetType() != null && !request.getPetType().isBlank()) {
            post.setPetType(PetType.valueOf(request.getPetType()));
        } else {
            post.setPetType(null); // 또는 PetType.OTHER 로 기본값 지정도 가능
        }
        post.setRegion(request.getRegion());
        post.setIsResolved(request.getIsResolved());

        if (request.getImages() != null) {
            postImageService.updateImagesFromRequest(post, request.getImages());
        }
        System.out.println("🔧 수정 후 Post: " + post);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public int toggleLike(Long postId, Users user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        Optional<PostLike> existing = postLikeRepository.findByPostAndUser(post, user);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            PostLike like = new PostLike(post, user);
            postLikeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
        }

        return post.getLikeCount();
    }

    @Override
    public void delete(Long id, Users user) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        postLikeRepository.deleteAllByPost(post);
        postRepository.delete(post);
    }

    @Override
    public Page<?> findAllByType(String type, Pageable pageable) {
        Post.PostType postType = Post.PostType.valueOf(type.toUpperCase());
        Page<Post> posts = postRepository.findAllByPostTypeOrderByCreatedAtDesc(postType, pageable);

        return switch (postType) {
            case REVIEW -> posts.map(post -> PostReviewListResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .petName(post.getPetName())
                    .petType(post.getPetType() != null ? post.getPetType().getLabel() : null)
                    .region(post.getRegion())
                    .writer(post.getUser().getDisplayName())
                    .imageUrl(post.getImages().isEmpty() ? null : post.getImages().get(0).getImageUrl())
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .createdAt(post.getCreatedAt())
                    .build());
            case QNA -> posts.map(post -> PostQnaListResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .petType(post.getPetType() != null ? post.getPetType().getLabel() : null)
                    .isResolved(Boolean.TRUE.equals(post.getIsResolved()))
                    .writer(post.getUser().getDisplayName())
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .createdAt(post.getCreatedAt())
                    .build());
            case SHOWOFF -> posts.map(post -> PostShowoffListResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .petType(post.getPetType() != null ? post.getPetType().getLabel() : null)
                    .writer(post.getUser().getDisplayName())
                    .imageUrl(post.getImages().isEmpty() ? null : post.getImages().get(0).getImageUrl())
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .createdAt(post.getCreatedAt())
                    .build());
        };
    }

    @Override
    public PostDetailResponse findById(Long id) {
        Post post = postRepository.findWithUserAndImagesById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        List<PostImageResponse> imageResponses = post.getImages().stream()
                .map(img -> new PostImageResponse(img.getId(), img.getImageUrl(), img.getOrdering()))
                .toList();

        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getUser().getDisplayName())
                .petType(post.getPetType() != null ? post.getPetType().getLabel() : null)
                .petName(post.getPetName())
                .region(post.getRegion())
                .isResolved(post.getIsResolved())
                .likeCount(post.getLikeCount())
                .postType(post.getPostType().name())
                .commentCount(post.getCommentCount())
                .images(imageResponses)
                .createdAt(post.getCreatedAt())
                .build();
    }
}