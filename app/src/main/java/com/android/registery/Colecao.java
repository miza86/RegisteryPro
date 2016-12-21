package com.android.registery;

import java.io.Serializable;

public class Colecao implements Serializable {
    private String titulo;
    private String imagemPath;
    private String hora;
    private String local;

    public Colecao(String titulo, String hora) {
        this.titulo = titulo;
        this.hora = hora;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getImagemPath() {
        return this.imagemPath;
    }

    public void setImagemPath(String imagemPath) {
        this.imagemPath = imagemPath;
    }

    public String getHora() {
        return this.hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getLocal() {
        return this.local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

}
