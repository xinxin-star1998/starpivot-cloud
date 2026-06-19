package cn.org.starpivot.system.config;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.util.LogUtils;
import cn.org.starpivot.system.domain.entity.SysDept;
import cn.org.starpivot.system.domain.entity.SysOperLog;
import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.mapper.SysDeptMapper;
import cn.org.starpivot.system.mapper.SysUserMapper;
import cn.org.starpivot.system.service.AsyncOperLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private static final String ANONYMOUS_PRINCIPAL = "anonymousUser";

    private final AsyncOperLogService asyncOperLogService;
    private final SysDeptMapper sysDeptMapper;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(cn.org.starpivot.common.annotation.Log)")
    public void logPointcut() {
    }

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysOperLog operLog = new SysOperLog();
        Object result = null;
        Log logAnnotation = null;

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            logAnnotation = method.getAnnotation(Log.class);

            setBasicInfo(operLog, logAnnotation, method, request, joinPoint);

            result = joinPoint.proceed();
            setResponseInfo(operLog, result, logAnnotation);
            operLog.setStatus(0);
            return result;
        } catch (Exception e) {
            operLog.setStatus(1);
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                errorMsg = LogUtils.truncateString(errorMsg, 2000);
            }
            operLog.setErrorMsg(errorMsg);
            throw e;
        } finally {
            operLog.setCostTime(System.currentTimeMillis() - startTime);
            operLog.setOperTime(LocalDateTime.now());
            if (logAnnotation != null) {
                asyncOperLogService.saveOperLogAsync(operLog);
            }
        }
    }

    private void setBasicInfo(SysOperLog operLog, Log logAnnotation, Method method,
                              HttpServletRequest request, ProceedingJoinPoint joinPoint) {
        String title = logAnnotation.title();
        if (!StringUtils.hasText(title)) {
            title = joinPoint.getTarget().getClass().getSimpleName() + "." + method.getName();
        }
        operLog.setTitle(LogUtils.truncateString(title, 50));
        operLog.setBusinessType(logAnnotation.businessType().getValue());
        operLog.setOperatorType(logAnnotation.operatorType().ordinal());

        String className = joinPoint.getTarget().getClass().getName();
        operLog.setMethod(LogUtils.truncateString(className + "." + method.getName() + "()", 200));

        if (request != null) {
            operLog.setRequestMethod(LogUtils.truncateString(request.getMethod(), 10));
            operLog.setOperUrl(LogUtils.truncateString(request.getRequestURI(), 255));
            operLog.setOperIp(LogUtils.truncateString(LogUtils.getClientIp(request), 128));
            operLog.setOperLocation(LogUtils.truncateString(LogUtils.getLoginLocation(operLog.getOperIp()), 255));
        }

        setOperatorInfo(operLog);

        if (logAnnotation.isSaveRequestData()) {
            setRequestParams(operLog, joinPoint);
        }
    }

    private void setOperatorInfo(SysOperLog operLog) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null || auth instanceof AnonymousAuthenticationToken) {
                operLog.setOperName(ANONYMOUS_PRINCIPAL);
                operLog.setOperatorType(0);
                return;
            }
            Object principal = auth.getPrincipal();
            if (principal instanceof LoginUser loginUser) {
                operLog.setOperName(LogUtils.truncateString(loginUser.getUsername(), 50));
                operLog.setOperatorType(1);
                if (loginUser.getUserId() != null) {
                    SysUser user = sysUserMapper.selectById(loginUser.getUserId());
                    if (user != null && user.getDeptId() != null) {
                        SysDept dept = sysDeptMapper.selectById(user.getDeptId());
                        if (dept != null) {
                            operLog.setDeptName(LogUtils.truncateString(dept.getDeptName(), 50));
                        }
                    }
                }
                return;
            }
            String name = principal instanceof String ? (String) principal : "未知";
            operLog.setOperName(name);
            operLog.setOperatorType(0);
        } catch (Exception e) {
            log.warn("获取操作人员信息失败", e);
            operLog.setOperName("未知");
            operLog.setOperatorType(0);
        }
    }

    private void setRequestParams(SysOperLog operLog, ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return;
            }
            Object[] filteredArgs = Arrays.stream(args)
                    .filter(arg -> arg != null)
                    .filter(arg -> !(arg instanceof MultipartFile))
                    .filter(arg -> !(arg instanceof ServletRequest))
                    .filter(arg -> !(arg instanceof ServletResponse))
                    .toArray();
            if (filteredArgs.length == 0) {
                return;
            }
            Object toSerialize = filteredArgs.length == 1 ? filteredArgs[0] : filteredArgs;
            String params = objectMapper.writeValueAsString(toSerialize);
            operLog.setOperParam(LogUtils.truncateString(LogUtils.desensitizeParam(params), 2000));
        } catch (Exception e) {
            log.warn("记录请求参数失败", e);
            operLog.setOperParam("参数解析失败");
        }
    }

    private void setResponseInfo(SysOperLog operLog, Object result, Log logAnnotation) {
        if (!logAnnotation.isSaveResponseData() || result == null) {
            return;
        }
        try {
            operLog.setJsonResult(LogUtils.truncateString(LogUtils.toJsonString(result), 2000));
        } catch (Exception e) {
            log.warn("记录响应结果失败", e);
            operLog.setJsonResult("响应解析失败");
        }
    }
}
