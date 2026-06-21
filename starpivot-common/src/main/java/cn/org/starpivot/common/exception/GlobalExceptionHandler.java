package cn.org.starpivot.common.exception;

import cn.org.starpivot.common.domain.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理：根据异常类型返回合适的 HTTP 状态码，便于前端拦截器解析
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常 - 返回 HTTP 200 + 业务码
     */
    @ExceptionHandler(cn.org.starpivot.common.exception.BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(cn.org.starpivot.common.exception.BizException ex) {
        int code = ex.getCode() != null ? ex.getCode() : ErrorCode.BIZ_ERROR;
        // 对于认证相关的业务异常，返回对应的 HTTP 状态码
        if (code == ErrorCode.UNAUTHORIZED) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(code, ex.getMessage()));
        }
        if (code == ErrorCode.FORBIDDEN || code == ErrorCode.PERMISSION_DENIED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(code, ex.getMessage()));
        }
        if (code == ErrorCode.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(code, ex.getMessage()));
        }
        return ResponseEntity.ok(Result.error(code, ex.getMessage()));
    }

    /**
     * 处理权限拒绝异常 - 返回 HTTP 403
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(ErrorCode.PERMISSION_DENIED, "没有权限，请联系管理员授权"));
    }

    /**
     * 处理认证异常 - 返回 HTTP 401
     */
    @ExceptionHandler(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<Result<Void>> handleAuthenticationCredentialsNotFoundException(
            org.springframework.security.authentication.AuthenticationCredentialsNotFoundException ex) {
        log.warn("Authentication credentials not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(ErrorCode.UNAUTHORIZED, "未授权，请先登录"));
    }

    /**
     * 处理另一种业务异常 - 返回 HTTP 200 + 业务码
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException ex) {
        int code = ex.getCode() != null ? ex.getCode() : ErrorCode.BIZ_ERROR;
        return ResponseEntity.ok(Result.error(code, ex.getMessage()));
    }

    /**
     * 处理参数校验异常 - 返回 HTTP 400
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<Result<Void>> handleValidationException(Exception ex) {
        String message = "参数校验失败";
        if (ex instanceof MethodArgumentNotValidException manv
                && manv.getBindingResult().getFieldError() != null) {
            message = manv.getBindingResult().getFieldError().getDefaultMessage();
        } else if (ex instanceof BindException be && be.getFieldError() != null) {
            message = be.getFieldError().getDefaultMessage();
        } else if (ex instanceof ConstraintViolationException cve && !cve.getConstraintViolations().isEmpty()) {
            message = cve.getConstraintViolations().iterator().next().getMessage();
        }
        log.warn("Validation error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.badRequest(message));
    }

    /**
     * 处理资源未找到异常 - 返回 HTTP 404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("Resource not found: {}", ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.notFound("请求的资源不存在"));
    }

    /**
     * 处理其他未知异常 - 返回 HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("系统繁忙，请稍后重试"));
    }
}