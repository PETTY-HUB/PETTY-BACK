package io.github.petty.community.service.post;

import io.github.petty.community.entity.post.PostQna;

public interface PostQnaService {
    PostQna findByPostId(Long postId);
    void save(PostQna postQna);
}