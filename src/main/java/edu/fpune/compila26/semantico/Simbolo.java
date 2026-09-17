package edu.fpune.compila26.semantico;



public class Simbolo {

    private String nombre;
    private String tipo;

    public Simbolo() {
    }

    public Simbolo(String nombre, String tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return nombre + " : " + tipo;
    }
}