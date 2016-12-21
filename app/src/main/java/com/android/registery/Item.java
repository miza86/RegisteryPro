package com.android.registery;

import java.io.Serializable;

public class Item implements Serializable {
    private String titulo;
    private String imagemPath;
    private String dataImagem;
    private String local;
    private String obs;
    private String audio;

    public Item(String titulo, String imagemPath, String dataImagem, String local, String obs, String audio) {
        this.titulo = titulo;
        this.imagemPath = imagemPath;
        this.dataImagem = dataImagem;
        this.local = local;
        this.obs = obs;
        this.audio = audio;
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

    public String getDataImagem() {
        return this.dataImagem;
    }

    public void setDataImagem(String dataImagem) {
        this.dataImagem = dataImagem;
    }

    public String getLocalImagem() {
        return this.local;
    }

    public String getObs() {
        return this.obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public String getAudio() {
        return this.audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}
