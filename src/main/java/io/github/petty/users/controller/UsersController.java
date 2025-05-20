package io.github.petty.users.controller;

import io.github.petty.users.dto.CustomUserDetails;
import io.github.petty.users.dto.JoinDTO;
import io.github.petty.users.dto.UserProfileEditDTO;
import io.github.petty.users.entity.Users;
import io.github.petty.users.service.JoinService;
import io.github.petty.users.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@Controller
public class UsersController {

    private final JoinService joinService;
    private final UserService userService;


    public UsersController(JoinService joinService, UserService userService) {
        this.joinService = joinService;
        this.userService = userService;
    }

    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("JoinDTO", new JoinDTO());
        return "join";
    }

    @PostMapping("/join")
        public String joinProcess(JoinDTO JoinDTO, RedirectAttributes redirectAttributes) {

            Boolean joinResult = joinService.joinProcess(JoinDTO);
            if (!joinResult) {
                redirectAttributes.addFlashAttribute("error", "이미 존재하는 계정입니다!");
                return "redirect:/";
            }
            return "redirect:/login";
        }

    @GetMapping("/login")
    public String loginForm() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        UUID currentUserId = userService.getCurrentUserId(principal);
        UserProfileEditDTO userProfile = userService.getUserById(currentUserId);

        model.addAttribute("userProfile", userProfile);

        return "profile_edit";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UserProfileEditDTO userProfileEditDTO,
                                RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        UUID currentUserId = userService.getCurrentUserId(principal);

        try {
            // 사용자 정보 수정
            userService.updateUserProfile(currentUserId, userProfileEditDTO);
            redirectAttributes.addFlashAttribute("successMessage", "프로필이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "프로필 수정 중 오류가 발생했습니다: " + e.getMessage());
        }

        // 수정 완료 후 메인 페이지로
        return "redirect:/";
    }
}


