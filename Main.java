import java.util.Locale;
import java.util.Scanner;


class Servidor {
    String nombre;          
    String origen;          
    String critico;        
    int intentosTotales;    
    int intentosFallidos;   

    boolean datosValidos() {
        // Regla 1: el nombre debe tener al menos un carácter que no sea espacio.
        // trim() elimina los espacios de los extremos; si queda vacío, no es válido.
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        // Regla 2: origen "interno" o "externo"; criticidad "si" o "no".
        if (origen == null || critico == null) {
            return false;
        }
        boolean origenOk = origen.trim().equalsIgnoreCase("interno")
                || origen.trim().equalsIgnoreCase("externo");
        boolean criticoOk = critico.trim().equalsIgnoreCase("si")
                || critico.trim().equalsIgnoreCase("no");
        if (!origenOk || !criticoOk) {
            return false;
        }

        // Regla 3: total > 0 y 0 <= fallidos <= total (extremos incluidos).
        if (intentosTotales <= 0) {
            return false;
        }
        if (intentosFallidos < 0 || intentosFallidos > intentosTotales) {
            return false;
        }

        return true; 
    }

    String evaluarEstado() {
        // Primero se valida; si falla, no se clasifica el riesgo.
        if (!datosValidos()) {
            return "DATOS INVALIDOS";
        }

        // Normalizamos los textos una sola vez para simplificar las condiciones.
        boolean esExterno = origen.trim().equalsIgnoreCase("externo");
        boolean esCritico = critico.trim().equalsIgnoreCase("si");

        // Orden 1: BLOQUEADO (máxima prioridad)
        if (intentosFallidos >= 5
                || (esCritico && esExterno && intentosFallidos >= 3)) {
            return "BLOQUEADO";
        }

        // Orden 2: ALERTA (solo se llega aquí si no es BLOQUEADO)
        if (intentosFallidos >= 3 // 3 o 4 fallos (5+ ya habría bloqueado)
                || (esExterno && intentosFallidos >= 2)) {
            return "ALERTA";
        }

        // Orden 3: NORMAL
        return "NORMAL";
    }

    /** Porcentaje de fallos sobre el total. Solo debe usarse con datos válidos. */
    double porcentajeFallos() {
        // (double) evita la división entera, que truncaría el resultado.
        return (double) intentosFallidos * 100 / intentosTotales;
    }

    /** Convierte el estado en un nivel numérico para poder comparar servidores. */
    int nivelPrioridad() {
        String estado = evaluarEstado();
        if (estado.equals("BLOQUEADO")) {
            return 3;
        }
        if (estado.equals("ALERTA")) {
            return 2;
        }
        if (estado.equals("NORMAL")) {
            return 1;
        }
        return 0; // DATOS INVALIDOS
    }

    /** Imprime los datos, el porcentaje (si procede) y el estado. */
    void mostrarResumen() {
        String nombreLimpio = nombre.trim();
        if (datosValidos()) {
            System.out.printf(Locale.US, "%s | %d/%d | %.1f%% | %s%n",
                    nombreLimpio, intentosFallidos, intentosTotales,
                    porcentajeFallos(), evaluarEstado());
        } else {
            // Con datos inválidos NO se calcula el porcentaje.
            System.out.printf("%s | %d/%d | N/D | %s%n",
                    nombreLimpio, intentosFallidos, intentosTotales,
                    evaluarEstado());
        }
    }
}

public class Main {

    static Servidor leerServidor(Scanner sc, int numero) {
        Servidor s = new Servidor();
        System.out.println("--- Servidor " + numero + " ---");

        System.out.print("Nombre: ");
        s.nombre = sc.nextLine();

        System.out.print("Origen (interno/externo): ");
        s.origen = sc.nextLine();

        System.out.print("Critico (si/no): ");
        s.critico = sc.nextLine();

        // Se lee como texto y se convierte a entero: así se evita el problema
        // del salto de línea pendiente que deja nextInt().
        System.out.print("Intentos totales: ");
        s.intentosTotales = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Intentos fallidos: ");
        s.intentosFallidos = Integer.parseInt(sc.nextLine().trim());

        return s;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Dos objetos independientes de tipo Servidor
        Servidor a = leerServidor(sc, 1);
        Servidor b = leerServidor(sc, 2);

        System.out.println();
        System.out.println("=======REPORTE DE SEGURIDAD=======");
        a.mostrarResumen();
        b.mostrarResumen();

        if (!a.datosValidos() || !b.datosValidos()) {
            System.out.println("COMPARACION NO DISPONIBLE");
        } else if (a.nivelPrioridad() == b.nivelPrioridad()) {
            System.out.println("IGUAL PRIORIDAD");
        } else if (a.nivelPrioridad() > b.nivelPrioridad()) {
            System.out.println("ATENCION PRIORITARIA: " + a.nombre.trim());
        } else {
            System.out.println("ATENCION PRIORITARIA: " + b.nombre.trim());
        }

        sc.close();
    }
}

/*
 * Preguntas:
 *
 * 1. ¿Por qué dos objetos de la misma clase pueden almacenar valores
 *    diferentes en sus atributos?
 *    Porque la clase es solo los atributos que existen, y cada
 *    objeto creado con new reserva su propia zona de memoria 
 *
 * 2. ¿Qué diferencia existe entre "Servidor b = a;" y
 *    "Servidor b = new Servidor();"?
 *    Servidor b = a: no crea ningún objeto nuevo, copia la referencia, así
 *    que a y b apuntan al mismo objeto y un cambio hecho desde b se ve desde a.
 *    Servidor b = new Servidor(): crea un objeto nuevo e independiente, con
 *    sus propios atributos
 *
 * 3. ¿Por qué es importante comprobar datosValidos() antes de calcular el
 *    porcentaje?
 *    Porque con datos inválidos el cálculo le falta sentido o falla, si
 *    intentosTotales es 0 habría una división por cero, y si los fallidos son
 *    negativos o mayores que el total el porcentaje sería incoherente
 *    (negativo o superior al 100%).
 */