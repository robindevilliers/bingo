package uk.co.malbec.bingo;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.co.malbec.bingo.persistence.UsersRepository;
import uk.co.malbec.bingo.present.response.ErrorCode;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SecurityFilter implements Filter {

    private List<String> protectedPaths = new ArrayList<>();

    {
        protectedPaths.add("/lobby");
        protectedPaths.add("/topup");
        protectedPaths.add("/play");
        protectedPaths.add("/poll-messages");
        protectedPaths.add("/send-message");

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        if (protectedPaths.contains(httpServletRequest.getRequestURI())){

            if (httpServletRequest.getSession().getAttribute("user") == null){

                Map<String, Object> packet = new HashMap<>();
                packet.put("errorCode", ErrorCode.CLIENT_NOT_AUTHORISED);
                packet.put("details", null);
                httpServletResponse.setStatus(400);
                httpServletResponse.setContentType("application/json; charset=utf-8");
                httpServletResponse.getWriter().println(new ObjectMapper().writeValueAsString(packet));

                return;
            }

        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}

