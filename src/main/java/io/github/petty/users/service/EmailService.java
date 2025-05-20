package io.github.petty.users.service;

import io.github.petty.users.entity.EmailVerification;
import io.github.petty.users.repository.EmailVerificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender javaMailSender;
    private final Random random = new Random();

    // 인증 코드 생성 + 저장 + 이메일 전송
    public boolean sendVerificationCode(String email) {
        String code = generateCode();

        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setCode(code);
        emailVerificationRepository.save(verification);

        try {
            sendEmail(email, code);
            return true;
        } catch (MessagingException e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            return false;
        }
    }

    // 4자리 랜덤 코드 생성
    private String generateCode() {
        return String.format("%06d", random.nextInt(1000000)); // 000000 ~ 999999
    }

    private void sendEmail(String toEmail, String code) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

        helper.setFrom("krpetty54@gmail.com");  // 보내는 사람 이메일 (본인의 실제 이메일 주소로 변경)
        helper.setTo(toEmail);
        helper.setSubject("[Petty] 이메일 인증 코드입니다.");

        // HTML 형식의 이메일 본문
        String htmlContent = "<div style='background-color:#ffffff;padding:40px;border-radius:8px;text-align:center;max-width:400px;width:100%;margin:0 auto'>" +
                "<a href='https://github.com/PETTY-HUB' style='display:block;margin-bottom:20px;text-decoration:none;color:#000000;width:5.5rem;font-weight:bold' target='_blank'>" +
                "<h2 style='color:#f39c12'>Petty</h2>" +
                "</a>" +
                "<div style='text-align:center'>" +
                "<p style='color:#212529;font-size:18px;font-weight:600'>인증 번호</p>" +
                "</div>" +
                "<div style='background-color:#f1f3f5;padding:20px;border-radius:8px;margin-bottom:30px;text-align:center'>" +
                "<p style='font-size:35px;font-weight:bold;line-height:1.5;color:#f39c12;margin:10px 0 0'>" + code + "</p>" +
                "</div>" +
                "<p style='display:inline-block;padding:15px 0;color:#888888;text-decoration:none;border-radius:8px;font-size:16px;word-break:keep-all;text-align:left'>" +
                "Petty의 더 많은 서비스를 이용하려면 이메일 인증이 필요해요.<br>" +
                "인증번호를 입력하고 인증을 완료해 주세요!<br>" +
                "(스팸함에 있을 수도 있으니 한 번 확인 부탁드려요!)" +
                "</p>" +
                "<div style='margin-top:20px; font-size:14px; color:#888;'>" +
                "이 메일은 5분간 유효합니다." +
                "</div>" +
                "</div>";

        helper.setText(htmlContent, true);

        javaMailSender.send(mimeMessage);
        log.info("이메일 ({})로 인증 코드 ({})를 성공적으로 전송했습니다.", toEmail, code);
    }

    // 인증 코드 확인
    @Transactional
    public boolean verifyCode(String email, String code) {
        EmailVerification latestVerification = emailVerificationRepository.findTopByEmailOrderByIdDesc(email);

        if (latestVerification != null
                && latestVerification.getCode().equals(code)
                && latestVerification.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5))) {

            emailVerificationRepository.deleteByEmail(email);
            log.info("이메일 ({}) 인증 성공 및 인증 데이터 삭제", email);
            return true;
        }

        log.warn("이메일 ({}) 인증 실패", email);
        return false;
    }
}