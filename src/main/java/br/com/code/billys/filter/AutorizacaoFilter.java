package br.com.code.billys.filter;

import br.com.code.billys.model.Usuarios;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

// @WebFilter("*.xhtml"): Intercepta TUDO que for página JSF
@WebFilter("*.xhtml")
public class AutorizacaoFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Pega a sessão atual (sem criar uma nova se não existir)
        HttpSession session = request.getSession(false);

        // Onde o usuário quer ir?
        String requestURI = request.getRequestURI();

        // Verifica se é a página de login ou recursos estáticos (CSS/JS/Imagens do Primefaces)
        // Se não deixar passar "jakarta.faces.resource", o sistema fica sem estilo!
        boolean isLoginRequest = requestURI.contains("/login.xhtml");
        boolean isResourceRequest = requestURI.contains("jakarta.faces.resource");

        // Tenta recuperar o usuário da sessão (que gravamos no LoginBean)
        Usuarios usuarioLogado = (session != null) ? (Usuarios) session.getAttribute("usuarioLogado") : null;

        // LÓGICA DO PORTEIRO:
        if (isLoginRequest || isResourceRequest) {
            // Se quer ir pro login ou carregar CSS, deixa passar
            chain.doFilter(req, res);
        } else if (usuarioLogado != null) {
            // Se já tem usuário logado, deixa passar para qualquer página
            chain.doFilter(req, res);
        } else {
            // Se não tá logado e tentou acessar página interna -> Chuta pro Login
            response.sendRedirect(request.getContextPath() + "/login.xhtml");
        }
    }
}