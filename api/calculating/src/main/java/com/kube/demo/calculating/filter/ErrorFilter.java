package com.kube.demo.calculating.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@Component
public class ErrorFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("client:{}, URL:{}", request.getLocalAddr(), ((HttpServletRequest) request).getRequestURI());
        chain.doFilter(request, response);
    }
}
