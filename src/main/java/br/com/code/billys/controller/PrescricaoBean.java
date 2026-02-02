package br.com.code.billys.controller;

import br.com.code.billys.model.*;
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
public class PrescricaoBean implements Serializable {

    @PersistenceContext(unitName = "billyPU")
    private EntityManager em;

    @Inject
    private LoginBean loginBean; // Para saber qual médico está prescrevendo

    // O Objeto Principal
    private Prescricao prescricao;

    // O Item "Rascunho" (o remédio que estamos configurando agora antes de adicionar na lista)
    private ItemPrescricao item;

    // Auxiliares para os Combos (Select) da tela
    private Long pacienteIdSelecionado;
    private Long produtoIdSelecionado;

    private List<Paciente> listaPacientes;
    private List<Produto> listaProdutos;

    private List<Prescricao> historicoPrescricoes; // NOVA LISTA
    private Prescricao prescricaoSelecionada; // Para o Dialog de visualização

    @PostConstruct
    public void init() {
        this.prescricao = new Prescricao();
        this.item = new ItemPrescricao();
        carregarListas();
        carregarHistorico(); // CARREGA A LISTA GERAL
    }

    public void carregarListas() {
        // Buscamos todos para preencher os <p:selectOneMenu>
        this.listaPacientes = em.createQuery("SELECT p FROM Paciente p ORDER BY p.nome", Paciente.class).getResultList();
        this.listaProdutos = em.createQuery("SELECT p FROM Produto p WHERE p.estoque > 0 ORDER BY p.nome", Produto.class).getResultList();
    }

    public void carregarHistorico() {
        // Traz as prescrições ordenadas da mais recente para a mais antiga
        // JOIN FETCH para performance (traz paciente e médico junto)
        this.historicoPrescricoes = em.createQuery(
                "SELECT p FROM Prescricao p JOIN FETCH p.paciente JOIN FETCH p.medico ORDER BY p.dataPrescricao DESC",
                Prescricao.class).getResultList();
    }

    // --- A LÓGICA DE ITENS (Em Memória) ---

    public void adicionarItem() {
        try {
            if (produtoIdSelecionado == null) {
                mensagemErro("Selecione um medicamento.");
                return;
            }
            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                mensagemErro("Informe uma quantidade válida.");
                return;
            }

            // 1. Busca o produto completo no banco baseado no ID selecionado
            Produto produto = em.find(Produto.class, produtoIdSelecionado);

            // 2. Configura o item
            item.setProduto(produto);

            // 3. Adiciona na lista da prescrição (na memória, ainda não foi pro banco)
            prescricao.adicionarItem(item);

            // 4. Limpa o formulário do item para o próximo remédio
            item = new ItemPrescricao();
            produtoIdSelecionado = null; // Reseta o combo

        } catch (Exception e) {
            mensagemErro("Erro ao adicionar item.");
            e.printStackTrace();
        }
    }

    public void removerItem(ItemPrescricao itemParaRemover) {
        prescricao.removerItem(itemParaRemover);
    }

    @Transactional
    public String salvar() {
        try {
            // 1. Validações Básicas
            if (pacienteIdSelecionado == null) {
                mensagemErro("Selecione o paciente.");
                return null;
            }
            if (prescricao.getItens().isEmpty()) {
                mensagemErro("Adicione pelo menos um medicamento à receita.");
                return null;
            }

            // 2. VALIDAÇÃO DE ESTOQUE (A novidade é aqui!)
            // Antes de mexer em qualquer coisa, verificamos se tem estoque para TODOS os itens
            for (ItemPrescricao item : prescricao.getItens()) {
                Produto produtoDoBanco = em.find(Produto.class, item.getProduto().getId());

                if (produtoDoBanco.getEstoque() < item.getQuantidade()) {
                    mensagemErro("Estoque insuficiente para o medicamento: " + produtoDoBanco.getNome() +
                            ". Disponível: " + produtoDoBanco.getEstoque());
                    return null; // Para tudo e avisa o usuário
                }
            }

            // 3. Vincula dados
            Paciente paciente = em.find(Paciente.class, pacienteIdSelecionado);
            prescricao.setPaciente(paciente);

            Usuarios medico = loginBean.getUsuarioLogado();
            medico = em.find(Usuarios.class, medico.getId());
            prescricao.setMedico(medico);

            // 4. Se passou na validação do passo 2, agora sim baixamos o estoque e salvamos
            atualizarEstoque(); // Agora é seguro chamar
            em.persist(prescricao);

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Prescrição realizada!"));

            return "prescricao?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            mensagemErro("Erro técnico ao salvar prescrição: " + e.getMessage());
            return null;
        }
    }

    // Metodo auxiliar (agora só executa se tiver certeza que tem estoque)
    private void atualizarEstoque() {
        for (ItemPrescricao it : prescricao.getItens()) {
            Produto p = em.find(Produto.class, it.getProduto().getId());
            p.setEstoque(p.getEstoque() - it.getQuantidade());
            em.merge(p);
        }
    }

    // Metodo auxiliar para facilitar mandar msg
    private void mensagemErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }
}