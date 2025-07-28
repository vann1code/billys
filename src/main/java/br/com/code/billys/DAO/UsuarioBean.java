package br.com.code.billys.DAO;

import br.com.code.billys.model.Usuarios;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

@Named
@RequestScoped
public class UsuarioBean {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("billyPU");
    private EntityManager em = emf.createEntityManager();

    public List<Usuarios> getUsuarios() {
        return em.createQuery("SELECT u FROM Usuarios u", Usuarios.class).getResultList();
    }

    // fechar o EntityManager se quiser (opcional)
    public void close() {
        if (em.isOpen()) em.close();
    }
}
