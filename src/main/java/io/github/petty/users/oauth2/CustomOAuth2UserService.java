package io.github.petty.users.oauth2;

import io.github.petty.users.Role;
import io.github.petty.users.entity.Users;
import io.github.petty.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UsersRepository usersRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // OAuth2 서비스 ID (github, google, naver 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // OAuth2 로그인 진행 시 키가 되는 필드 값(PK)
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // Github 유저 정보 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // GitHub 이메일 및 ID 추출
        String email = extractEmail(registrationId, attributes);
        String providerId = attributes.get(userNameAttributeName).toString();

        // 기존 회원인지 확인하고 없으면 회원가입 진행
        Users user = saveOrUpdate(email, registrationId, providerId, attributes);

        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
                attributes,
                userNameAttributeName,
                user.getUsername(),
                registrationId,
                providerId
        );
    }

    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        if ("github".equals(registrationId)) {
            if (attributes.containsKey("email") && attributes.get("email") != null) {
                return (String) attributes.get("email");
            }
            // 이메일이 null인 경우 대체 로직 (GitHub ID + @github.com)
            return attributes.get("login") + "@github.com";
        }
        return null; // 다른 OAuth2 제공자 추가 시 확장
    }

    // 유저 정보 저장 또는 업데이트
    private Users saveOrUpdate(String email, String provider, String providerId, Map<String, Object> attributes) {
        Users user = usersRepository.findByUsername(email);

        if (user == null) {
            // 새 사용자 생성
            user = new Users();
            user.setUsername(email);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setPassword(UUID.randomUUID().toString()); // 임의의 패스워드 설정
            user.setRole(Role.ROLE_USER.name());

            // GitHub 사용자명을 displayName으로 사용
            if (attributes.containsKey("name") && attributes.get("name") != null) {
                user.setDisplayName((String) attributes.get("name"));
            } else if (attributes.containsKey("login")) {
                user.setDisplayName((String) attributes.get("login"));
            } else {
                user.setDisplayName(email.split("@")[0]);
            }

            return usersRepository.save(user);
        }

        // 기존 사용자 업데이트 로직 (필요한 경우)
        return user;
    }
}