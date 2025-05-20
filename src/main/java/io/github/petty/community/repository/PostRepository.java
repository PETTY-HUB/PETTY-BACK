package io.github.petty.community.repository;

import io.github.petty.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"user", "images"})
    Page<Post> findAllByPostTypeOrderByCreatedAtDesc(Post.PostType postType, Pageable pageable);

    @EntityGraph(attributePaths = "images")
    Optional<Post> findById(Long id);

    @EntityGraph(attributePaths = {"user", "images"})
    Optional<Post> findWithUserAndImagesById(Long id);
}

