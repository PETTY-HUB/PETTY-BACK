package io.github.petty.community.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "post_images")
@ToString(exclude = "post")
@EqualsAndHashCode(of = "id")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시글 연관관계 (필수)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // 이미지 순서 (0~4)
    @Builder.Default
    @Column(nullable = false)
    private Integer ordering = 0;

    // 유틸 메서드 (양방향 연관관계 설정)
    public void setPost(Post post) {
        this.post = post;
        if (!post.getImages().contains(this)) {
            post.getImages().add(this);
        }
    }
}
