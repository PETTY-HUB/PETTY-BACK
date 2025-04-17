package io.github.petty.community.service.post;

import io.github.petty.community.entity.post.PostShowoff;
import io.github.petty.community.repository.post.PostShowoffRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostShowoffServiceImpl implements PostShowoffService {

    private final PostShowoffRepository postShowoffRepository;

    public PostShowoffServiceImpl(PostShowoffRepository postShowoffRepository) {
        this.postShowoffRepository = postShowoffRepository;
    }

    @Override
    public Optional<PostShowoff> findByPostId(Long postId) {
        return postShowoffRepository.findByPostId(postId);
    }

    @Override
    public void save(PostShowoff postShowoff) {
        postShowoffRepository.save(postShowoff);
    }
}