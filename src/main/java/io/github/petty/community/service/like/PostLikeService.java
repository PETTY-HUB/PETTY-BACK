package io.github.petty.community.service.like;

import io.github.petty.community.entity.post.Post;
import io.github.petty.community.entity.user.User;

import java.util.UUID;

public interface PostLikeService {
    boolean hasUserLiked(Long postId, UUID userId);
    long countLikes(Long postId);
    void removeLike(Long postId, UUID userId);
    void addLike(Post post, User user);
}