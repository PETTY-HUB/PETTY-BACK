package io.github.petty.community.repository.post;

import io.github.petty.community.entity.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByPostType(String type, Pageable pageable);

    Optional<Post> findById(Long id); // JpaRepository에 기본 포함되어 있지만 명시 가능

    boolean existsByIdAndUser_Id(Long postId, UUID userId);
}
