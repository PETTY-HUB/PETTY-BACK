package io.github.petty.community.service.post;

import io.github.petty.community.entity.post.PostQna;
import io.github.petty.community.repository.post.PostQnaRepository;
import org.springframework.stereotype.Service;

@Service
public class PostQnaServiceImpl implements PostQnaService {

    private final PostQnaRepository postQnaRepository;

    public PostQnaServiceImpl(PostQnaRepository postQnaRepository) {
        this.postQnaRepository = postQnaRepository;
    }

    @Override
    public PostQna findByPostId(Long postId) {
        return postQnaRepository.findByPostId(postId);
    }

    @Override
    public void save(PostQna postQna) {
        postQnaRepository.save(postQna);
    }
}