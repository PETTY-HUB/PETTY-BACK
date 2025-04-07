@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class CommunityController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        return ResponseEntity.ok(postService.findAllPosts());
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequestDto dto,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            PostResponseDto createdPost = postService.createPost(dto, userDetails.getUsername());
            return ResponseEntity.ok(createdPost);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            postService.deletePost(postId, userDetails.getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }
}
