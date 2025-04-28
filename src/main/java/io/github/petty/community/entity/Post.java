package io.github.petty.community.entity;

import io.github.petty.community.enums.PetType;
import io.github.petty.users.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import io.github.petty.community.entity.PostImage;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
public class Post {

    public enum PostType {
        QNA, REVIEW, SHOWOFF
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // 공통 필드
    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostType postType = PostType.REVIEW;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private int likeCount = 0;
    private int commentCount = 0;

    // 게시판 타입별 확장 필드

    // 후기 전용
    @Column(length = 100)
    private String region;

    @Column(length = 20)
    private String petName;

    // 공통
    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type", length = 20)
    private PetType petType;

    // QnA 전용
    @Column(name = "is_resolved")
    private Boolean isResolved = false;
}
