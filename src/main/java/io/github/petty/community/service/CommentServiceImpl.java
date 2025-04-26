package io.github.petty.community.service;

import io.github.petty.community.dto.CommentRequest;
import io.github.petty.community.dto.CommentResponse;
import io.github.petty.community.entity.Comment;
import io.github.petty.community.entity.Post;
import io.github.petty.community.repository.CommentRepository;
import io.github.petty.community.repository.PostRepository;
import io.github.petty.users.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    public List<CommentResponse> getComments(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostIdWithUser(postId);
        return comments.stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .writer(comment.getUser().getDisplayName())  // 안전하게 호출됨
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Long addComment(Long postId, CommentRequest request, Users user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(request.getContent());

        post.setCommentCount(post.getCommentCount() + 1); // 댓글 수 증가

        return commentRepository.save(comment).getId();
    }

    @Override
    public void deleteComment(Long commentId, Users user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        Post post = comment.getPost();
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1)); // 댓글 수 감소

        commentRepository.delete(comment);
    }

    @Override
    public void updateComment(Long commentId, CommentRequest request, Users user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        comment.setContent(request.getContent());
        commentRepository.save(comment);
    }
}
