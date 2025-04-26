package io.github.petty.community.service;

import io.github.petty.community.dto.CommentRequest;
import io.github.petty.community.dto.CommentResponse;
import io.github.petty.users.entity.Users;

import java.util.List;

public interface CommentService {

    // 댓글 목록 조회
    List<CommentResponse> getComments(Long postId);

    // 댓글 작성
    Long addComment(Long postId, CommentRequest request, Users user);

    // 댓글 삭제
    void deleteComment(Long commentId, Users user);

    // 댓글 수정
    void updateComment(Long commentId, CommentRequest request, Users user);
}
