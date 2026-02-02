package br.com.code.billys.controller;

import br.com.code.billys.model.Usuarios;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped // O Bean vive enquanto você estiver na mesma tela
@Getter
@Setter
public class UsuarioBean implements Serializable {

    @PersistenceContext(unitName = "billyPU")
    private EntityManager em;

    private List<Usuarios> listaUsuarios;

    // Este objeto vai receber os dados da tela
    private Usuarios usuario = new Usuarios();

    // Variável para capturar o ID que vem da URL
    private Long idSelecionado;

    // -- AÇÕES --

    // O metodo salvar agora serve pros dois (Insert e Update)
    @Transactional
    public String salvar() {
        try {
            if (usuario.getId() == null) {
                em.persist(usuario); // Cria novo
            } else {
                em.merge(usuario);   // Atualiza existente
            }

            // Limpa tudo
            usuario = new Usuarios();
            idSelecionado = null;
            listaUsuarios = null;

            return "usuario?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Metodo chamado quando a tela de cadastro abrir
    public void carregarCadastro() {
        if (idSelecionado != null) {
            // Se tem ID, busca no banco e preenche o formulário (Modo Edição)
            this.usuario = em.find(Usuarios.class, idSelecionado);
        } else {
            // Se não tem ID, limpa o objeto (Modo Criação)
            this.usuario = new Usuarios();
        }
    }

    @Transactional
    public void excluir(Usuarios u) {
        try {
            // 1. Buscamos a referência atualizada do objeto no banco pelo ID
            Usuarios usuarioParaRemover = em.find(Usuarios.class, u.getId());

            // 2. Removemos
            em.remove(usuarioParaRemover);

            // 3. Limpamos a lista para forçar o JSF a buscar os dados atualizados
            listaUsuarios = null;

            // Opcional: Adicionar mensagem de sucesso
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Removido com sucesso!"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<Usuarios> getUsuarios() {
        if (listaUsuarios == null) {
            listaUsuarios = em.createQuery("SELECT u FROM Usuarios u", Usuarios.class).getResultList();
        }
        return listaUsuarios;
    }
}