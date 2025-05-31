package io.github.petty.tour.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 애플리케이션 전역의 API 예외를 처리하는 핸들러 클래스입니다.
 * 지정된 컨트롤러 패키지 내에서 발생하는 예외들을 중앙에서 관리합니다.
 */
@Slf4j
@RestControllerAdvice("io.github.petty.tour.controller") // API 컨트롤러 패키지로 범위 제한
public class GlobalExceptionHandler {

  /**
   * API 에러 응답을 위한 표준화된 JSON 본문을 생성합니다.
   *
   * @param status HTTP 상태 객체
   * @param message 클라이언트에게 전달할 에러 메시지
   * @param path 요청된 URI 경로
   * @return 에러 상세 정보를 담은 Map 객체
   */
  private Map<String, Object> createErrorResponseBody(HttpStatus status, String message, String path) {
    Map<String, Object> errorDetails = new HashMap<>();
    errorDetails.put("timestamp", LocalDateTime.now().toString()); // 에러 발생 시각
    errorDetails.put("status", status.value());                // HTTP 상태 코드 (숫자)
    errorDetails.put("error", status.getReasonPhrase());       // HTTP 상태 메시지 (영문)
    errorDetails.put("message", message);                      // 상세 에러 내용
    errorDetails.put("path", path);                            // 요청 경로
    return errorDetails;
  }

  /**
   * {@link ResourceNotFoundException} 발생 시 처리합니다. (HTTP 404 Not Found)
   * 요청한 리소스를 찾을 수 없을 때 이 핸들러가 호출됩니다.
   *
   * @param ex 발생한 ResourceNotFoundException 객체
   * @param request 현재 웹 요청 객체
   * @return HTTP 404 상태 코드와 에러 상세 정보(JSON)를 담은 {@link ResponseEntity}
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
          ResourceNotFoundException ex, WebRequest request) {
    String requestPath = request.getDescription(false).replace("uri=", "");
    log.warn("요청 리소스 없음 (404): {} (경로: {})", ex.getMessage(), requestPath);
    Map<String, Object> errorBody = createErrorResponseBody(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            requestPath
    );
    return new ResponseEntity<>(errorBody, HttpStatus.NOT_FOUND);
  }

  /**
   * 요청 파라미터의 타입이 일치하지 않을 때 발생하는 {@link MethodArgumentTypeMismatchException}을 처리합니다. (HTTP 400 Bad Request)
   * 예: 정수형 파라미터에 문자열이 전달된 경우.
   *
   * @param ex 발생한 MethodArgumentTypeMismatchException 객체
   * @param request 현재 웹 요청 객체
   * @return HTTP 400 상태 코드와 에러 상세 정보(JSON)를 담은 {@link ResponseEntity}
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(
          MethodArgumentTypeMismatchException ex, WebRequest request) {
    String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "알 수 없는 타입";
    String message = String.format("요청 파라미터 '%s'의 값이 유효하지 않습니다. 전달된 값: '%s', 필요한 타입: '%s'.",
            ex.getName(), ex.getValue(), requiredType);
    String requestPath = request.getDescription(false).replace("uri=", "");
    log.warn("메서드 인자 타입 불일치 (400): {} (경로: {})", message, requestPath);
    Map<String, Object> errorBody = createErrorResponseBody(
            HttpStatus.BAD_REQUEST,
            message,
            requestPath
    );
    return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
  }

  /**
   * 필수 요청 파라미터가 누락되었을 때 발생하는 {@link MissingServletRequestParameterException}을 처리합니다. (HTTP 400 Bad Request)
   *
   * @param ex 발생한 MissingServletRequestParameterException 객체
   * @param request 현재 웹 요청 객체
   * @return HTTP 400 상태 코드와 에러 상세 정보(JSON)를 담은 {@link ResponseEntity}
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameter(
          MissingServletRequestParameterException ex, WebRequest request) {
    String message = String.format("필수 요청 파라미터 '%s'(타입: %s)가 누락되었습니다.", ex.getParameterName(), ex.getParameterType());
    String requestPath = request.getDescription(false).replace("uri=", "");
    log.warn("필수 요청 파라미터 누락 (400): {} (경로: {})", message, requestPath);
    Map<String, Object> errorBody = createErrorResponseBody(
            HttpStatus.BAD_REQUEST,
            message,
            requestPath
    );
    return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
  }


  /**
   * 메서드 인자가 부적절하거나 일관되지 않을 때 발생하는 {@link IllegalArgumentException}을 처리합니다. (HTTP 400 Bad Request)
   *
   * @param ex 발생한 IllegalArgumentException 객체
   * @param request 현재 웹 요청 객체
   * @return HTTP 400 상태 코드와 에러 상세 정보(JSON)를 담은 {@link ResponseEntity}
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
          IllegalArgumentException ex, WebRequest request) {
    String requestPath = request.getDescription(false).replace("uri=", "");
    log.warn("부적절한 인자 전달 (400): {} (경로: {})", ex.getMessage(), requestPath);
    Map<String, Object> errorBody = createErrorResponseBody(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            requestPath
    );
    return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
  }

  /**
   * 위에서 명시적으로 처리되지 않은 모든 기타 예외를 처리합니다. (HTTP 500 Internal Server Error)
   * 이는 애플리케이션의 예기치 않은 오류에 대한 최종 방어선 역할을 합니다.
   *
   * @param ex 발생한 Exception 객체 (모든 예외의 상위 타입)
   * @param request 현재 웹 요청 객체
   * @return HTTP 500 상태 코드와 일반화된 에러 메시지(JSON)를 담은 {@link ResponseEntity}
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGlobalException(
          Exception ex, WebRequest request) {
    String requestPath = request.getDescription(false).replace("uri=", "");
    log.error("처리되지 않은 내부 서버 오류 발생 (500): {} (경로: {})", ex.getMessage(), requestPath, ex);
    Map<String, Object> errorBody = createErrorResponseBody(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "요청 처리 중 예상치 못한 오류가 발생했습니다. 관리자에게 문의하거나 잠시 후 다시 시도해 주십시오.",
            requestPath
    );
    return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}