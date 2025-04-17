package io.github.petty.community.service.post;

import io.github.petty.community.entity.post.PostReview;
import java.util.Optional;

public interface PostReviewService {
    Optional<PostReview> findByPostId(Long postId);
    void save(PostReview postReview);
}
