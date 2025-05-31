package io.github.petty.tour.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 사용자 정의 예외입니다.
 * 이 예외가 컨트롤러 외부로 전파되면 HTTP 404 Not Found 상태 코드가 응답됩니다.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND) // 이 예외 발생 시 HTTP 404 응답
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 지정된 메시지로 ResourceNotFoundException을 생성합니다.
     * @param message 예외 메시지
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 지정된 메시지와 원인(cause)으로 ResourceNotFoundException을 생성합니다.
     * @param message 예외 메시지
     * @param cause 예외의 원인
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}