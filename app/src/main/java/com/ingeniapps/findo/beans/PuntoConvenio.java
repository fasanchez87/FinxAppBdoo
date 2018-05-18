package com.ingeniapps.findo.beans;

/**
 * Created by Ingenia Applications on 21/11/2017.
 */

public class PuntoConvenio
{
    private String imaProveedor;
    private String nomProveedor;
    private String codPunto;
    private String codProveedor;
    private String codCiudad;
    private String serPunto;
    private String dirPunto;
    private String latPunto;
    private String descPunto;
    private String lonPunto;
    private String conPunto;
    private String telPunto;
    private String corPunto;
    private String obsCiudad;
    private String type;
    private String distPunto;
    private String codTipo;//Cajero / Convenio
    private String nomCajero;

    public String getHorPunto() {
        return horPunto;
    }

    public void setHorPunto(String horPunto) {
        this.horPunto = horPunto;
    }

    private String horPunto;


    public String getNomCajero() {
        return nomCajero;
    }

    public void setNomCajero(String nomCajero) {
        this.nomCajero = nomCajero;
    }



    public String getCodTipo() {
        return codTipo;
    }

    public void setCodTipo(String codTipo) {
        this.codTipo = codTipo;
    }


    public PuntoConvenio(String type)
    {
        super();
        this.type = type;
    }

    public PuntoConvenio()
    {
    }

    public String getNomCiudad() {
        return nomCiudad;
    }

    public void setNomCiudad(String nomCiudad) {
        this.nomCiudad = nomCiudad;
    }

    private String nomCiudad;

    public String getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(String calificacion) {
        this.calificacion = calificacion;
    }

    private String calificacion;

    public String getTimePunto() {
        return timePunto;
    }

    public void setTimePunto(String timePunto) {
        this.timePunto = timePunto;
    }

    private String timePunto;


    public String getDistPunto() {
        return distPunto;
    }

    public void setDistPunto(String distPunto) {
        this.distPunto = distPunto;
    }


    public String getImaProveedor() {
        return imaProveedor;
    }

    public void setImaProveedor(String imaProveedor) {
        this.imaProveedor = imaProveedor;
    }

    public String getNomProveedor() {
        return nomProveedor;
    }

    public void setNomProveedor(String nomProveedor) {
        this.nomProveedor = nomProveedor;
    }

    public String getCodPunto() {
        return codPunto;
    }

    public void setCodPunto(String codPunto) {
        this.codPunto = codPunto;
    }

    public String getCodProveedor() {
        return codProveedor;
    }

    public void setCodProveedor(String codProveedor) {
        this.codProveedor = codProveedor;
    }

    public String getCodCiudad() {
        return codCiudad;
    }

    public void setCodCiudad(String codCiudad) {
        this.codCiudad = codCiudad;
    }

    public String getSerPunto() {
        return serPunto;
    }

    public void setSerPunto(String serPunto) {
        this.serPunto = serPunto;
    }

    public String getDirPunto() {
        return dirPunto;
    }

    public void setDirPunto(String dirPunto) {
        this.dirPunto = dirPunto;
    }

    public String getLatPunto() {
        return latPunto;
    }

    public void setLatPunto(String latPunto) {
        this.latPunto = latPunto;
    }

    public String getDescPunto() {
        return descPunto;
    }

    public void setDescPunto(String descPunto) {
        this.descPunto = descPunto;
    }

    public String getLonPunto() {
        return lonPunto;
    }

    public void setLonPunto(String lonPunto) {
        this.lonPunto = lonPunto;
    }

    public String getConPunto() {
        return conPunto;
    }

    public void setConPunto(String conPunto) {
        this.conPunto = conPunto;
    }

    public String getTelPunto() {
        return telPunto;
    }

    public void setTelPunto(String telPunto) {
        this.telPunto = telPunto;
    }

    public String getCorPunto() {
        return corPunto;
    }

    public void setCorPunto(String corPunto) {
        this.corPunto = corPunto;
    }

    public String getObsCiudad() {
        return obsCiudad;
    }

    public void setObsCiudad(String obsCiudad) {
        this.obsCiudad = obsCiudad;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
