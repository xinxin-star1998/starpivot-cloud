package cn.org.starpivot.common.exception;

import cn.org.starpivot.common.domain.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBizException_unauthorizedReturns401() {
        ResponseEntity<Result<Void>> response =
                handler.handleBizException(new BizException(ErrorCode.UNAUTHORIZED, "未登录"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.UNAUTHORIZED, response.getBody().getCode());
    }

    @Test
    void handleIllegalArgumentException_returns400() {
        ResponseEntity<Result<Void>> response =
                handler.handleIllegalArgumentException(new IllegalArgumentException("参数无效"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.PARAM_ERROR, response.getBody().getCode());
        assertEquals("参数无效", response.getBody().getMessage());
    }

    @Test
    void handleBizException_paramInvalidReturns200WithErrorCode() {
        ResponseEntity<Result<Void>> response =
                handler.handleBizException(new BizException(ErrorCode.PARAM_INVALID, "ID不能为空"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ErrorCode.PARAM_INVALID, response.getBody().getCode());
    }
}
