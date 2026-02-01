package br.com.code.billys.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome não pode estar em branco")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido (ex: joao@gmail.com)")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 4, message = "A senha deve ter no mínimo 4 caracteres")
    @Column(name = "senha")
    private String senha;

    @Enumerated(EnumType.STRING) // Grava "ADMIN" ou "COMUM" no banco
    @Column(nullable = false)
    private Perfil perfil;
}
