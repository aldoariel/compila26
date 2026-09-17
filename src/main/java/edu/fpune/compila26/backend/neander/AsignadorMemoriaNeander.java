package edu.fpune.compila26.backend.neander;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.fpune.compila26.intermedio.Operando;
import edu.fpune.compila26.intermedio.TipoOperando;

public class AsignadorMemoriaNeander {
    public static final int INICIO_DATOS = 0x80;
    public static final int FIN_DATOS = 0xFF;

    private final Map<Operando, DatoNeander> asignaciones = new LinkedHashMap<>();
    private final Map<String, DatoNeander> temporalesInternos = new LinkedHashMap<>();
    private int siguienteDireccion = INICIO_DATOS;

    public int asignar(Operando operando) {
        if (operando == null) {
            throw new IllegalArgumentException("El operando es obligatorio.");
        }

        DatoNeander existente = asignaciones.get(operando);
        if (existente != null) {
            return existente.direccion();
        }
        validarEspacioDisponible();

        int direccion = siguienteDireccion++;
        asignaciones.put(operando, new DatoNeander(
                operando,
                direccion,
                valorInicial(operando)
        ));
        return direccion;
    }

    public int asignarTemporalInterno(String nombre) {
        DatoNeander existente = temporalesInternos.get(nombre);
        if (existente != null) {
            return existente.direccion();
        }

        validarEspacioDisponible();
        int direccion = siguienteDireccion++;
        temporalesInternos.put(nombre, new DatoNeander(null, direccion, null, true));
        return direccion;
    }

    public Integer obtenerDireccion(Operando operando) {
        DatoNeander dato = asignaciones.get(operando);
        return dato == null ? null : dato.direccion();
    }

    public Integer obtenerDireccionTemporalInterno(String nombre) {
        DatoNeander dato = temporalesInternos.get(nombre);
        return dato == null ? null : dato.direccion();
    }

    public List<DatoNeander> getDatos() {
        List<DatoNeander> datos = new ArrayList<>(asignaciones.values());
        datos.addAll(temporalesInternos.values());
        return datos;
    }

    private void validarEspacioDisponible() {
        if (siguienteDireccion > FIN_DATOS) {
            throw new MemoriaNeanderException("Memoria de datos NEANDER agotada.");
        }
    }

    private Integer valorInicial(Operando operando) {
        if (operando.tipo() != TipoOperando.CONSTANTE) {
            return null;
        }

        int valor;
        try {
            valor = Integer.parseInt(operando.valor());
        } catch (NumberFormatException exception) {
            throw new MemoriaNeanderException(
                    "Constante no válida para NEANDER: '" + operando.valor() + "'."
            );
        }

        if (valor < -128 || valor > 127) {
            throw new MemoriaNeanderException(
                    "Constante fuera del rango de 8 bits con signo: " + valor + "."
            );
        }
        return valor & 0xFF;
    }
}
