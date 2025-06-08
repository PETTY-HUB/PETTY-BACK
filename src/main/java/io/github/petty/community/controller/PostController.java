package io.github.petty.community.controller;

import io.github.petty.community.dto.PostDetailResponse;
import io.github.petty.community.dto.PostRequest;
import io.github.petty.community.entity.Post;
import io.github.petty.community.service.PostService;
import io.github.petty.users.dto.CustomUserDetails;
import io.github.petty.users.entity.Users;
import io.github.petty.users.repository.UsersRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UsersRepository usersRepository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid PostRequest request,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            Users user = usersRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(401).body("사용자를 찾을 수 없습니다.");
            }
            
            Long id = postService.save(request, user);
            return ResponseEntity.ok(Map.of("id", id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("게시글 작성에 실패했습니다.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody @Valid PostRequest request,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        Users user = usersRepository.findByUsername(username);
        postService.update(id, request, user);
        System.out.println("📥 요청 들어옴: " + request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        Users user = usersRepository.findByUsername(username);
        String postType = postService.delete(id, user); // 🔥 삭제된 게시글의 타입 반환
        return ResponseEntity.ok(Map.of("postType", postType));
    }

    @GetMapping
    public ResponseEntity<Page<?>> getAllByType(@RequestParam("type") Post.PostType type,
                                                @PageableDefault(size = 9) Pageable pageable) {
        Page<?> posts = postService.findAllByType(String.valueOf(type), pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> getPost(@PathVariable Long id) {
        PostDetailResponse post = postService.findById(id);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            Users user = usersRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(401).body("사용자를 찾을 수 없습니다.");
            }
            
            int newCount = postService.toggleLike(id, user);
            return ResponseEntity.ok(Map.of("likeCount", newCount));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("좋아요 처리에 실패했습니다.");
        }
    }
}
