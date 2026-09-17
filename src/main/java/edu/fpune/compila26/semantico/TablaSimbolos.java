package edu.fpune.compila26.semantico;


import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TablaSimbolos {

    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    public boolean declarar(String nombre, String tipo) {

        if (simbolos.containsKey(nombre)) {
            return false;
        }

        simbolos.put(nombre, new Simbolo(nombre, tipo));
        return true;
    }

    public boolean estaDeclarado(String nombre) {
        return simbolos.containsKey(nombre);
    }

    public Simbolo obtener(String nombre) {
        return simbolos.get(nombre);
    }

    public String obtenerTipo(String nombre) {

        Simbolo simbolo = simbolos.get(nombre);

        if (simbolo == null) {
            return null;
        }

        return simbolo.getTipo();
    }

    public Collection<Simbolo> getSimbolos() {
        return simbolos.values();
    }

    public int cantidad() {
        return simbolos.size();
    }

    public void limpiar() {
        simbolos.clear();
    }
}