package br.com.code.billys.DAO;

import br.com.code.billys.model.Usuarios;
import jakarta.enterprise.context.RequestScoped; // Ou @ViewScoped (muito comum em JSF)
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.io.Serializable;

@Named
@RequestScoped // Ciclo de vida: dura apenas uma requisição
public class UsuarioBean implements Serializable {

    // A MÁGICA: O servidor injeta o EntityManager pronto pra uso.
    // Não precisa de Factory, nem de create, nem de close().
    @PersistenceContext(unitName = "billyPU")
    private EntityManager em;

    private List<Usuarios> listaUsuarios;

    // É boa prática carregar os dados no início ou sob demanda, não direto no get
    public List<Usuarios> getUsuarios() {
        if (listaUsuarios == null) {
            listaUsuarios = em.createQuery("SELECT u FROM Usuarios u", Usuarios.class).getResultList();
        }
        return listaUsuarios;
    }
}