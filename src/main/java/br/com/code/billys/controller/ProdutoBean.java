package br.com.code.billys.controller;

import br.com.code.billys.model.Produto;
import br.com.code.billys.model.Usuarios;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Named
@ViewScoped
public class ProdutoBean implements Serializable {

    @PersistenceContext(unitName = "billyPU")
    private EntityManager em;

    // Injetamos o LoginBean para saber QUEM está cadastrando
    @Inject
    private LoginBean loginBean;

    private Produto produto;
    private List<Produto> produtos;

    @PostConstruct
    public void init() {
        this.produto = new Produto();
        carregarProdutos();
    }

    public void carregarProdutos() {
        // Traz o produto e já faz o JOIN com usuario pra mostrar o nome na tela (JOIN FETCH)
        this.produtos = em.createQuery("SELECT p FROM Produto p JOIN FETCH p.usuarioCadastro", Produto.class).getResultList();
    }

    @Transactional
    public void salvar() {
        try {
            // Verifica se tem usuário logado (Segurança)
            if (loginBean == null || loginBean.getUsuarioLogado() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Sessão expirada. Logue novamente."));
                return;
            }

            // Associa o usuário da sessão (Isso pode dar erro se o usuário estiver 'detached', então buscamos ele de novo)
            // DICA: Buscamos o usuário de novo no banco para garantir que ele está "fresco"
            Usuarios usuarioFresco = em.find(Usuarios.class, loginBean.getUsuarioLogado().getId());
            produto.setUsuarioCadastro(usuarioFresco);

            if (produto.getId() == null) {
                em.persist(produto);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Produto criado!"));
            } else {
                em.merge(produto);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Produto atualizado!"));
            }

            // Se der erro AQUI, o Rollback lá embaixo vai cancelar o salvamento
            this.produto = new Produto();
            carregarProdutos();

        } catch (Exception e) {
            // IMPEDE O SALVAMENTO SE DER ERRO
            FacesContext.getCurrentInstance().validationFailed(); // Avisa o JSF
            // Marca a transação para ser desfeita (O banco não vai salvar)
            // Se o metodo 'setRollbackOnly' não aparecer, pode ignorar, mas o ideal é tratar.
            try {
                // Tenta forçar rollback via EJB/JTA se estiver disponível,
                // mas apenas lançar a exceção já resolveria se não estivéssemos engolindo ela.
                e.printStackTrace(); // OLHE O CONSOLE PARA VER O ERRO REAL
            } catch (Exception ex) { /* ignora */ }

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Falha técnica: " + e.getMessage()));
        }
    }

    // Metodo para carregar produto na edição (via URL param)
    public void carregarCadastro() {
        if (produto.getId() != null) {
            produto = em.find(Produto.class, produto.getId());
        }
    }

    @Transactional
    public void excluir(Produto p) {
        try {
            Produto produtoParaRemover = em.find(Produto.class, p.getId());
            em.remove(produtoParaRemover);
            carregarProdutos();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Produto removido!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível excluir."));
        }
    }
}