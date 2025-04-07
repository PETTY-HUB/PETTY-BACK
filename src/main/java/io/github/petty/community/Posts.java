@Service
@RequiredArgsConstructor
public class Posts {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<PostResponseDto> findAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    public PostResponseDto createPost(PostRequestDto dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Post post = new Post(dto.getTitle(), dto.getContent(), dto.getImageUrl(), user);
        Post saved = postRepository.save(post);
        return PostResponseDto.from(saved);
    }

    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));
        if (!post.getUser().getUsername().equals(username)) {
            throw new RuntimeException("삭제 권한 없음");
        }
        postRepository.delete(post);
    }
}
