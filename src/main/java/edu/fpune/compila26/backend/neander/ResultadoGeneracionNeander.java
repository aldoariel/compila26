package edu.fpune.compila26.backend.neander;

import java.util.List;

public record ResultadoGeneracionNeander(
        List<InstruccionEnsambladorNeander> instrucciones,
        List<DatoNeander> datos,
        List<String> diagnosticos
) {
    public ResultadoGeneracionNeander {
        instrucciones = List.copyOf(instrucciones);
        datos = List.copyOf(datos);
        diagnosticos = List.copyOf(diagnosticos);
    }

    public boolean exitoso() {
        return diagnosticos.isEmpty();
    }

    public List<String> ensamblador() {
        return instrucciones.stream()
                .map(InstruccionEnsambladorNeander::formatear)
                .toList();
    }
}
