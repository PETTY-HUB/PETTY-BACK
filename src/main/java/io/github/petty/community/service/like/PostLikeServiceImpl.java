package io.github.petty.community.service.like;

import io.github.petty.community.entity.like.PostLike;
import io.github.petty.community.entity.post.Post;
import io.github.petty.community.entity.user.User;
import io.github.petty.community.repository.like.PostLikeRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;

    public PostLikeServiceImpl(PostLikeRepository postLikeRepository) {
        this.postLikeRepository = postLikeRepository;
    }

    @Override
    public boolean hasUserLiked(Long postId, UUID userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Override
    public void removeLike(Long postId, UUID userId) {
        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    @Override
    public void addLike(Post post, User user) {
        PostLike like = new PostLike();
        like.setPost(post);
        like.setUser(user);
        postLikeRepository.save(like);
    }
}