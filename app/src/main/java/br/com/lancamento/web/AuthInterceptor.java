package br.com.lancamento.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String path = request.getRequestURI();
    if (path.startsWith("/login") || path.startsWith("/css") || path.startsWith("/js")) {
      return true;
    }

    HttpSession session = request.getSession(false);
    boolean loggedIn =
        session != null && session.getAttribute(AuthController.SESSION_USER) != null;
    if (loggedIn) {
      return true;
    }

    response.sendRedirect("/login");
    return false;
  }
}

