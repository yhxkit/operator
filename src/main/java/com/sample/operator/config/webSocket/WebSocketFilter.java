package com.sample.operator.config.webSocket;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class WebSocketFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        req.getSession(); // 웹소켓에서 httpsession 정보를 얻어낼 수 있게 1회 호출
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
