package br.com.code.billys.controller;

import br.com.code.billys.model.Paciente;
import jakarta.annotation.PostConstruct;
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

@Getter
@Setter
@Named
@ViewScoped
public class PacienteBean implements Serializable {

    @PersistenceContext(unitName = "billyPU")
    private EntityManager em;

    private Paciente paciente;
    private List<Paciente> pacientes;

    @PostConstruct
    public void init() {
        this.paciente = new Paciente();
        carregarPacientes();
    }

    public void carregarPacientes() {
        this.pacientes = em.createQuery("SELECT p FROM Paciente p ORDER BY p.nome", Paciente.class).getResultList();
    }

    @Transactional
    public String salvar() {
        try {
            if (paciente.getId() == null) {
                em.persist(paciente);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Paciente cadastrado!"));
            } else {
                em.merge(paciente);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Paciente atualizado!"));
            }
            this.paciente = new Paciente();

            // REDIRECIONAMENTO: Volta para a tela de listagem
            return "paciente?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao salvar."));
            return null; // Fica na mesma tela se der erro
        }
    }

    // Carregar na edição
    public void carregarCadastro() {
        if (paciente.getId() != null) {
            this.paciente = em.find(Paciente.class, paciente.getId());
        }
    }

    @Transactional
    public void excluir(Paciente p) {
        try {
            Paciente remover = em.find(Paciente.class, p.getId());
            em.remove(remover);
            carregarPacientes();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sucesso", "Paciente removido!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não é possível excluir este paciente."));
        }
    }

}