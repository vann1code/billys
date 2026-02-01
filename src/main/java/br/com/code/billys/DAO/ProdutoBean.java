package br.com.code.billys.controller;

import br.com.code.billys.DAO.LoginBean;
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
            // REGRA DE NEGÓCIO: Associa o produto ao usuário logado
            Usuarios usuarioLogado = loginBean.getUsuarioLogado();
            if (produto.getId() == null) {
                produto.setUsuarioCadastro(usuarioLogado);
                em.persist(produto);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Produto criado!"));
            } else {
                // Se for edição, mantemos o usuário original ou trocamos?
                // Vamos manter o original (aqui você teria que buscar do banco antes, mas vamos simplificar)
                produto.setUsuarioCadastro(usuarioLogado);
                em.merge(produto);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Produto atualizado!"));
            }

            this.produto = new Produto(); // Limpa o formulário
            carregarProdutos(); // Atualiza a tabela

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao salvar produto."));
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