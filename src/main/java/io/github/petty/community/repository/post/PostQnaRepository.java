package io.github.petty.community.repository.post;

import io.github.petty.community.entity.post.PostQna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostQnaRepository extends JpaRepository<PostQna, Long> {

    PostQna findByPostId(Long postId);
}