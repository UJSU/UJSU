package ujsu;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public CustomAuthenticationFailureHandler() {
        super("/sign-in?error");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorMessage = "Ошибка авторизации.";
        if (exception instanceof BadCredentialsException)
            errorMessage = "Неверное имя пользователя или пароль.";
        request.getSession().setAttribute("errorMessage", errorMessage);
        getRedirectStrategy().sendRedirect(request, response, "/sign-in?error");
    }
}