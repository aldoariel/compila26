package edu.fpune.compila26.semantico;


public class PruebaTablaSimbolos {

    public static void main(String[] args) {

        TablaSimbolos tabla = new TablaSimbolos();

        System.out.println("=================================");
        System.out.println("     PRUEBA TABLA DE SIMBOLOS");
        System.out.println("=================================");

        System.out.println();

        System.out.println("Declarando variables...");

        System.out.println(
                "x -> " + tabla.declarar("x", "NUMERO")
        );

        System.out.println(
                "y -> " + tabla.declarar("y", "NUMERO")
        );

        System.out.println();

        System.out.println("¿x está declarada?");
        System.out.println(tabla.estaDeclarado("x"));

        System.out.println();

        System.out.println("Tipo de x:");
        System.out.println(tabla.obtenerTipo("x"));

        System.out.println();

        System.out.println("Intentando declarar x nuevamente:");
        System.out.println(
                tabla.declarar("x", "NUMERO")
        );

        System.out.println();

        System.out.println("Cantidad de símbolos:");
        System.out.println(tabla.cantidad());

        System.out.println();

        System.out.println("Símbolos:");
        for (var simbolo : tabla.getSimbolos()) {
            System.out.println("  " + simbolo);
        }
    }
}