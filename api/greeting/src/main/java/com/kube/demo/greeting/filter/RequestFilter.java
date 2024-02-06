package com.kube.demo.greeting.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@Component
public class RequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("client:{}, URL:{}", request.getLocalAddr(), ((HttpServletRequest) request).getRequestURI());
        chain.doFilter(request, response);
    }
}
