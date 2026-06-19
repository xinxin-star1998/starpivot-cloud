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

/**
 * 全局异常处理：业务异常统一返回 HTTP 200 + Result 业务码，便于前端拦截器解析。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(cn.org.starpivot.common.exception.BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(cn.org.starpivot.common.exception.BizException ex) {
        int code = ex.getCode() != null ? ex.getCode() : ErrorCode.BIZ_ERROR;
        return ResponseEntity.ok(Result.error(code, ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.ok(Result.error(ErrorCode.PERMISSION_DENIED, "没有权限，请联系管理员授权"));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException ex) {
        int code = ex.getCode() != null ? ex.getCode() : 500;
        return ResponseEntity.ok(Result.error(code, ex.getMessage()));
    }

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
        return ResponseEntity.ok(Result.badRequest(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("系统繁忙，请稍后重试"));
    }
}
