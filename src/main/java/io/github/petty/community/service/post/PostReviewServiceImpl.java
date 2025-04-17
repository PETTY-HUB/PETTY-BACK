package io.github.petty.community.service.post;

import io.github.petty.community.entity.post.PostReview;
import io.github.petty.community.repository.post.PostReviewRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostReviewServiceImpl implements PostReviewService {

    private final PostReviewRepository postReviewRepository;

    public PostReviewServiceImpl(PostReviewRepository postReviewRepository) {
        this.postReviewRepository = postReviewRepository;
    }

    @Override
    public Optional<PostReview> findByPostId(Long postId) {
        return postReviewRepository.findByPostId(postId);
    }

    @Override
    public void save(PostReview postReview) {
        postReviewRepository.save(postReview);
    }
}
