package com.example.studyroomserver.config;

// 把 com.studyroom 改成你自己的包名 com.example.studyroomserver
import com.example.studyroomserver.common.utils.JwtUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的token
        String token = request.getHeader("Authorization");

        // 2. 校验token是否存在
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401); // 未授权
            return false;
        }

        // 3. 解析token
        try {
            Long userId = jwtUtil.parseToken(token);
            // 把用户ID存入请求域，后续Controller可以直接获取
            request.setAttribute("userId", userId);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }
}