package io.github.petty.community.form;

public record CommentForm(
        String content
) {
    public static CommentForm empty() {
        return new CommentForm("");
    }
}

