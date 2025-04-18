package com.example.projectdemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthorizationInterceptor authorizationInterceptor;
    private final TempPasswordInterceptor tempPasswordInterceptor;
    private final LoginTrackerInterceptor loginTrackerInterceptor;
    private final SidebarInterceptor sidebarInterceptor;

    @Autowired
    public WebMvcConfig(
            AuthorizationInterceptor authorizationInterceptor,
            TempPasswordInterceptor tempPasswordInterceptor,
            LoginTrackerInterceptor loginTrackerInterceptor,
            SidebarInterceptor sidebarInterceptor) {
        this.authorizationInterceptor = authorizationInterceptor;
        this.tempPasswordInterceptor = tempPasswordInterceptor;
        this.loginTrackerInterceptor = loginTrackerInterceptor;
        this.sidebarInterceptor = sidebarInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 로그인 추적 인터셉터 등록 (모든 API 요청)
        registry.addInterceptor(loginTrackerInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/api/auth/reset-password");

        // 임시 비밀번호 상태 체크 인터셉터 등록
        registry.addInterceptor(tempPasswordInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/change-password",
                        "/api/auth/reset-password", "/api/auth/refresh-token");

        // 권한 체크 인터셉터 등록 (관리자 권한 필요한 API)
        registry.addInterceptor(authorizationInterceptor)
                .addPathPatterns("/api/admin/**");

        // 사이드바 데이터 제공 인터셉터 등록 (게시판 및 관리자 페이지 관련 요청에 적용)
        registry.addInterceptor(sidebarInterceptor)
                .addPathPatterns("/board/**", "/main/**", "/**"); // 관리자 페이지 링크를 표시하기 위해 모든 페이지에 적용
    }
}