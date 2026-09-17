package edu.fpune.compila26.backend.neander;

import java.util.ArrayList;
import java.util.List;

import edu.fpune.compila26.intermedio.InstruccionIntermedia;
import edu.fpune.compila26.intermedio.Operando;
import edu.fpune.compila26.intermedio.TipoOperando;

public class GeneradorNeander {
    private static final Operando CONSTANTE_UNO = Operando.constante("1");
    private static final String TEMPORAL_NEGADO = "operando_negado";

    public ResultadoGeneracionNeander generar(List<InstruccionIntermedia> codigoIntermedio) {
        List<InstruccionIntermedia> instruccionesIntermedias =
                codigoIntermedio == null ? List.of() : codigoIntermedio;
        AsignadorMemoriaNeander memoria = new AsignadorMemoriaNeander();

        try {
            asignarMemoria(instruccionesIntermedias, memoria);
            if (contieneResta(instruccionesIntermedias)) {
                memoria.asignar(CONSTANTE_UNO);
                memoria.asignarTemporalInterno(TEMPORAL_NEGADO);
            }
        } catch (MemoriaNeanderException exception) {
            return error(exception.getMessage());
        }

        List<InstruccionEnsambladorNeander> instrucciones = new ArrayList<>();
        for (InstruccionIntermedia instruccion : instruccionesIntermedias) {
            String diagnostico = traducir(instruccion, memoria, instrucciones);
            if (diagnostico != null) {
                return error(diagnostico);
            }
        }

        instrucciones.add(InstruccionEnsambladorNeander.sinDireccion(InstruccionNeander.HLT));
        return new ResultadoGeneracionNeander(
                instrucciones,
                memoria.getDatos(),
                List.of()
        );
    }

    private boolean contieneResta(List<InstruccionIntermedia> instrucciones) {
        return instrucciones.stream()
                .filter(instruccion -> instruccion != null)
                .anyMatch(instruccion -> instruccion.operacion()
                        == edu.fpune.compila26.intermedio.OperacionIntermedia.RESTAR);
    }

    private void asignarMemoria(
            List<InstruccionIntermedia> instrucciones,
            AsignadorMemoriaNeander memoria) {
        for (TipoOperando tipo : List.of(
                TipoOperando.VARIABLE,
                TipoOperando.TEMPORAL,
                TipoOperando.CONSTANTE)) {
            for (InstruccionIntermedia instruccion : instrucciones) {
                if (instruccion == null) {
                    continue;
                }
                asignarSiCorresponde(instruccion.destino(), tipo, memoria);
                asignarSiCorresponde(instruccion.operando1(), tipo, memoria);
                asignarSiCorresponde(instruccion.operando2(), tipo, memoria);
            }
        }
    }

    private void asignarSiCorresponde(
            Operando operando,
            TipoOperando tipo,
            AsignadorMemoriaNeander memoria) {
        if (operando != null && operando.tipo() == tipo) {
            memoria.asignar(operando);
        }
    }

    private String traducir(
            InstruccionIntermedia instruccion,
            AsignadorMemoriaNeander memoria,
            List<InstruccionEnsambladorNeander> salida) {
        if (instruccion == null || instruccion.operacion() == null) {
            return "Instrucción intermedia inválida.";
        }

        return switch (instruccion.operacion()) {
            case ASIGNAR -> {
                salida.add(conDireccion(
                        InstruccionNeander.LDA,
                        memoria,
                        instruccion.operando1()
                ));
                salida.add(conDireccion(
                        InstruccionNeander.STA,
                        memoria,
                        instruccion.destino()
                ));
                yield null;
            }
            case SUMAR -> {
                salida.add(conDireccion(
                        InstruccionNeander.LDA,
                        memoria,
                        instruccion.operando1()
                ));
                salida.add(conDireccion(
                        InstruccionNeander.ADD,
                        memoria,
                        instruccion.operando2()
                ));
                salida.add(conDireccion(
                        InstruccionNeander.STA,
                        memoria,
                        instruccion.destino()
                ));
                yield null;
            }
            case RESTAR -> {
                salida.add(conDireccion(InstruccionNeander.LDA, memoria, instruccion.operando2()));
                salida.add(InstruccionEnsambladorNeander.sinDireccion(InstruccionNeander.NOT));
                salida.add(conDireccion(InstruccionNeander.ADD, memoria, CONSTANTE_UNO));
                salida.add(conDireccionTemporalInterno(InstruccionNeander.STA, memoria));
                salida.add(conDireccion(InstruccionNeander.LDA, memoria, instruccion.operando1()));
                salida.add(conDireccionTemporalInterno(InstruccionNeander.ADD, memoria));
                salida.add(conDireccion(InstruccionNeander.STA, memoria, instruccion.destino()));
                yield null;
            }
            case MULTIPLICAR, DIVIDIR ->
                    "Operación " + instruccion.operacion()
                            + " todavía no soportada por el backend NEANDER.";
        };
    }

    private InstruccionEnsambladorNeander conDireccion(
            InstruccionNeander instruccion,
            AsignadorMemoriaNeander memoria,
            Operando operando) {
        Integer direccion = memoria.obtenerDireccion(operando);
        if (direccion == null) {
            throw new MemoriaNeanderException(
                    "El operando no tiene una dirección asignada: " + operando + "."
            );
        }
        return InstruccionEnsambladorNeander.conDireccion(instruccion, direccion);
    }

    private InstruccionEnsambladorNeander conDireccionTemporalInterno(
            InstruccionNeander instruccion,
            AsignadorMemoriaNeander memoria) {
        Integer direccion = memoria.obtenerDireccionTemporalInterno(TEMPORAL_NEGADO);
        if (direccion == null) {
            throw new MemoriaNeanderException("El temporal interno no tiene dirección asignada.");
        }
        return InstruccionEnsambladorNeander.conDireccion(instruccion, direccion);
    }

    private ResultadoGeneracionNeander error(String diagnostico) {
        return new ResultadoGeneracionNeander(
                List.of(),
                List.of(),
                List.of(diagnostico)
        );
    }
}
