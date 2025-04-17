package io.github.petty.community.service.post;

import io.github.petty.community.entity.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PostService {
    Page<Post> findAllByPostType(String type, Pageable pageable);
    Post findById(Long id);
    void save(Post post);
    void deleteById(Long id);
    boolean isOwner(Long postId, UUID userId);
}