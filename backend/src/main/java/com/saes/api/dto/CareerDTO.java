package com.saes.api.dto;

import com.saes.api.model.Career;

public class CareerDTO {
    private Long id_carrera;
    private String clave;
    private String nombre;
    private Integer duracion_semestres;
    private Boolean activa;

    public CareerDTO() {
    }

    public CareerDTO(Career career) {
        this.id_carrera = career.getId();
        this.clave = career.getClave();
        this.nombre = career.getNombre();
        this.duracion_semestres = career.getDuracionSemestres();
        this.activa = career.getActiva();
    }

    public Career toEntity() {
        Career career = new Career();
        career.setId(this.id_carrera);
        career.setClave(this.clave);
        career.setNombre(this.nombre);
        career.setDuracionSemestres(this.duracion_semestres);
        career.setActiva(this.activa == null ? true : this.activa);
        return career;
    }

    public Long getId_carrera() {
        return id_carrera;
    }

    public void setId_carrera(Long id_carrera) {
        this.id_carrera = id_carrera;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getDuracion_semestres() {
        return duracion_semestres;
    }

    public void setDuracion_semestres(Integer duracion_semestres) {
        this.duracion_semestres = duracion_semestres;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
}
