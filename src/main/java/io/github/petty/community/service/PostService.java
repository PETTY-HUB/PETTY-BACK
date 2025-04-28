package io.github.petty.community.service;

import io.github.petty.community.dto.*;
import io.github.petty.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    Long save(PostRequest request, Users user);
    void update(Long id, PostRequest request, Users user);
    void delete(Long id, Users user);
    Page<?> findAllByType(String type, Pageable pageable);
    PostDetailResponse findById(Long id);
    int toggleLike(Long postId, Users user);
}