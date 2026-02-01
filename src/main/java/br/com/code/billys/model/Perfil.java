package br.com.code.billys.model;

import lombok.Getter;

@Getter
public enum Perfil {
    ADMIN("Administrador"),
    COMUM("Usuário Comum");

    private final String label;

    Perfil(String label) {
        this.label = label;
    }
}