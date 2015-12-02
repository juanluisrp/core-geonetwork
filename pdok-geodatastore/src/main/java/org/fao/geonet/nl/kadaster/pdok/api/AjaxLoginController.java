package org.fao.geonet.nl.kadaster.pdok.api;

import nl.kadaster.pdok.bussiness.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Created by juanluisrp on 01/12/2015.
 */
@Controller
@RequestMapping("/{lang}/ajaxLogin")
public class AjaxLoginController {
    @Autowired
    @Qualifier("authenticationManager")
    AuthenticationManager authenticationManager;

    @Autowired
    SecurityContextRepository repository;

    @RequestMapping(method = RequestMethod.GET)
    public void login() {

    }

    @RequestMapping(method=RequestMethod.POST)
    @ResponseBody
    public LoginResponse performLogin(
            @RequestParam("j_username") String username,
            @RequestParam("j_password") String password,
            HttpServletRequest request, HttpServletResponse response)
    {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, password);
        try {
            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            ServletResponse wrappedResponse = response;
            while (wrappedResponse instanceof HttpServletResponseWrapper) {
                if (wrappedResponse instanceof SaveContextOnUpdateOrErrorResponseWrapper) {
                    break;
                }
                wrappedResponse = ((HttpServletResponseWrapper) wrappedResponse).getResponse();
            }
            if (!(wrappedResponse instanceof SaveContextOnUpdateOrErrorResponseWrapper)) {

            }

            repository.saveContext(SecurityContextHolder.getContext(), request, (HttpServletResponse) wrappedResponse);
            return new LoginResponse(true, null);
        } catch (BadCredentialsException ex) {
            return new LoginResponse(false, "Bad Credentials");
        }
    }

}
