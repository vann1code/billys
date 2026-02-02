package br.com.code.billys.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "prescricoes")
public class Prescricao implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuarios medico;

    @Column(name = "data_prescricao")
    private LocalDateTime dataPrescricao;

    private String observacoes;

    // A MÁGICA: Uma lista de itens dentro da receita
    @OneToMany(mappedBy = "prescricao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemPrescricao> itens = new ArrayList<>();

    public Prescricao() {
        this.dataPrescricao = LocalDateTime.now();
    }

    // Metodo auxiliar para adicionar itens (Facilita nossa vida)
    public void adicionarItem(ItemPrescricao item) {
        item.setPrescricao(this); // Amarra o item nesta receita
        this.itens.add(item);
    }

    public void removerItem(ItemPrescricao item) {
        this.itens.remove(item);
        item.setPrescricao(null);
    }

}