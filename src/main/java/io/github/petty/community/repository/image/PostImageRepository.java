package io.github.petty.community.repository.image;

import io.github.petty.community.entity.image.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostIdOrderByOrderingAsc(Long postId);

    @Transactional
    void deleteByPostId(Long postId);
}