package io.github.petty.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostViewController {

    // 📌 후기 게시판
    @GetMapping("/posts/review")
    public String reviewListPage() {
        return "post-review-list";
    }

    @GetMapping("/posts/review/new")
    public String reviewFormPage() {
        return "post-review-form";
    }

    @GetMapping("/posts/review/edit")
    public String reviewEditPage() {
        return "edit-review";
    }

    // 📌 자랑 게시판
    @GetMapping("/posts/showoff")
    public String showoffListPage() {
        return "post-showoff-list";
    }

    @GetMapping("/posts/showoff/new")
    public String showoffFormPage() {
        return "post-showoff-form";
    }

    @GetMapping("/posts/showoff/edit")
    public String showoffEditPage() {
        return "edit-showoff";
    }

    // 📌 질문 게시판
    @GetMapping("/posts/qna")
    public String qnaListPage() {
        return "post-qna-list";
    }

    @GetMapping("/posts/qna/new")
    public String qnaFormPage() {
        return "post-qna-form";
    }

    @GetMapping("/posts/qna/edit")
    public String qnaEditPage() {
        return "edit-qna";
    }

    // 📌 상세페이지 (공통)
    @GetMapping("/posts/detail")
    public String detailPage() {
        return "post-detail";
    }
}

