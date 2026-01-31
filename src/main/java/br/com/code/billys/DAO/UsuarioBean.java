package br.com.code.billys.DAO;

import br.com.code.billys.model.Usuarios;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped // O Bean vive enquanto você estiver na mesma tela
public class UsuarioBean implements Serializable {

    @PersistenceContext(unitName = "billyPU")
    private EntityManager em;

    private List<Usuarios> listaUsuarios;

    // Este objeto vai receber os dados da tela
    private Usuarios usuario = new Usuarios();

    // -- AÇÕES --

    @Transactional // O Container abre e fecha a transação pra você
    public String salvar() {
        try {
            // Persiste o objeto no banco
            em.persist(usuario);

            // Limpa o objeto para um próximo cadastro (opcional)
            usuario = new Usuarios();

            // Recarrega a lista para mostrar o novo usuário se voltar pra tabela
            listaUsuarios = null;

            // Retorna a string de navegação para voltar à lista (redirecionando)
            // O "?faces-redirect=true" é um padrão para limpar a URL do navegador
            return "usuario?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace(); // Em produção, usaríamos mensagens de erro na tela
            return null; // Fica na mesma tela se der erro
        }
    }

    // -- GETTERS E SETTERS --

    public List<Usuarios> getUsuarios() {
        if (listaUsuarios == null) {
            listaUsuarios = em.createQuery("SELECT u FROM Usuarios u", Usuarios.class).getResultList();
        }
        return listaUsuarios;
    }

    public Usuarios getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }
}