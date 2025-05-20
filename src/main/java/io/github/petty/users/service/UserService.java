package io.github.petty.users.service;

import io.github.petty.users.dto.CustomUserDetails;
import io.github.petty.users.dto.UserProfileEditDTO;
import io.github.petty.users.entity.Users;
import io.github.petty.users.oauth2.CustomOAuth2User;
import io.github.petty.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;

    public UUID getCurrentUserId(Object principal) {
        if (principal == null) {
            return null;
        }

        // CustomUserDetails 처리 (일반 로그인)
        if (principal instanceof CustomUserDetails) {
            // CustomUserDetails에서 username 얻기
            String username = ((CustomUserDetails) principal).getUsername();
            // username으로 사용자 찾기
            Users user = usersRepository.findByUsername(username);
            return user != null ? user.getId() : null;
        }

        // CustomOAuth2User 처리 (소셜 로그인)
        if (principal instanceof CustomOAuth2User) {
            // OAuth2에서 username 얻기
            String username = ((CustomOAuth2User) principal).getName();
            // username으로 사용자 찾기
            Users user = usersRepository.findByUsername(username);
            return user != null ? user.getId() : null;
        }

        // Users 객체 직접 처리
        if (principal instanceof Users) {
            return ((Users) principal).getId();
        }

        return null;
    }






    // 사용자 정보 가져오기
    public UserProfileEditDTO getUserById(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new UserProfileEditDTO(user.getDisplayName(), user.getPhone());

    }

    // 사용자 정보 수정
    public Users updateUserProfile(UUID userId, UserProfileEditDTO userProfileEditDTO) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setDisplayName(userProfileEditDTO.getDisplayName());
        user.setPhone(userProfileEditDTO.getPhone());

        return usersRepository.save(user);
    }
}
