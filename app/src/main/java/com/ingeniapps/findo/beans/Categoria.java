package com.ingeniapps.findo.beans;

/**
 * Created by Ingenia Applications on 16/11/2017.
 */

public class Categoria
{
    private String codCategoria;
    private boolean isSelected=false;
    private String nomCategoria;
    private String type;

    public String getImaCategoria() {
        return imaCategoria;
    }

    public void setImaCategoria(String imaCategoria) {
        this.imaCategoria = imaCategoria;
    }

    private String imaCategoria;


    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }



    public String getCodCategoria() {
        return codCategoria;
    }

    public void setCodCategoria(String codCategoria) {
        this.codCategoria = codCategoria;
    }

    public String getNomCategoria() {
        return nomCategoria;
    }

    public void setNomCategoria(String nomCategoria) {
        this.nomCategoria = nomCategoria;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    public Categoria()
    {

    }
}
