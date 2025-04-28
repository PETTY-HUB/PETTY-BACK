package io.github.petty.community.dto;

import io.github.petty.community.entity.Post;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostRequest {
    private String title;
    private String content;
    private Post.PostType postType;
    private String petName;
    private String petType;
    private String region;
    private Boolean isResolved;
    private List<PostImageRequest> images;
}
