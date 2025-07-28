package br.com.code.billys;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Start {
    public String getMensagem() {
        return "Billys!";
    }
}

