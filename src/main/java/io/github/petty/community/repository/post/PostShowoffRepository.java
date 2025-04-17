package io.github.petty.community.repository.post;

import io.github.petty.community.entity.post.PostShowoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostShowoffRepository extends JpaRepository<PostShowoff, Long> {

    Optional<PostShowoff> findByPostId(Long postId);
}