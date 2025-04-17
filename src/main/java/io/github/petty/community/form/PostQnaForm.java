package io.github.petty.community.form;

public record PostQnaForm(
        String title,
        String content,
        String petType
) {
    public static PostQnaForm empty() {
        return new PostQnaForm("", "", "");
    }
}
