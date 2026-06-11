package com.example.enterprise.config;

import com.example.enterprise.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
/**
 * 操作日志拦截器
 * <p>拦截请求并记录操作耗时，在请求完成后交给OperationLogService记录日志</p>
 */
@Component
@RequiredArgsConstructor
public class OperationLogInterceptor implements HandlerInterceptor {
    /** 操作日志服务 */
    private final OperationLogService operationLogService;

    /**
     * 在请求处理之前记录开始时间
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("operationLogStart", System.currentTimeMillis());
        return true;
    }

    /**
     * 在请求处理完成后记录操作日志
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        Object startValue = request.getAttribute("operationLogStart");
        long start = startValue instanceof Long value ? value : System.currentTimeMillis();
        operationLogService.record(request, response.getStatus(), System.currentTimeMillis() - start, exception);
    }
}
