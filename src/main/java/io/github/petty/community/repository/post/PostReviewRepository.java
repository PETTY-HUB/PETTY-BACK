package io.github.petty.community.repository.post;

import io.github.petty.community.entity.post.PostReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostReviewRepository extends JpaRepository<PostReview, Long> {

    Optional<PostReview> findByPostId(Long postId);
}
