package io.github.petty.community.service.post;

import io.github.petty.community.entity.post.PostShowoff;
import java.util.Optional;

public interface PostShowoffService {
    Optional<PostShowoff> findByPostId(Long postId);
    void save(PostShowoff postShowoff);
}
