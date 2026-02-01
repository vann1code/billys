package br.com.code.billys.DAO;

import br.com.code.billys.model.Usuarios;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Named
@SessionScoped // VITAL: Mantém os dados vivos entre navegações diferentes
public class LoginBean implements Serializable {

    @PersistenceContext(unitName = "billyPU")
    private EntityManager em;

    // Campos para a tela de login
    private String email;
    private String senha;

    // Guarda o usuário que logou com sucesso
    private Usuarios usuarioLogado;

    public String logar() {
        try {
            // Busca usuário por Email e Senha
            // OBS: Em produção real, a senha estaria criptografada (Hash), mas vamos simplificar
            Usuarios user = em.createQuery("SELECT u FROM Usuarios u WHERE u.email = :email AND u.senha = :senha", Usuarios.class)
                    .setParameter("email", email)
                    .setParameter("senha", senha)
                    .getSingleResult();

            if (user != null) {
                this.usuarioLogado = user;
                // Redireciona para a home (menu.xhtml)
                return "/menu?faces-redirect=true";
            }
        } catch (Exception e) {
            // Se não achar (NoResultException) ou der erro
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Usuário ou senha inválidos!"));
        }
        return null; // Fica na mesma tela
    }

    public String deslogar() {
        // Mata a sessão inteira
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login?faces-redirect=true";
    }
}