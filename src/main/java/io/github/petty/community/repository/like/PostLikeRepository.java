package io.github.petty.community.repository.like;

import io.github.petty.community.entity.like.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, UUID userId);

    long countByPostId(Long postId);

    void deleteByPostIdAndUserId(Long postId, UUID userId);
}