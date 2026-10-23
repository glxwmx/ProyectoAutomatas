package tecnm.lexer;

/**
 *
 * @author Gustavo M
 */
public class ErrorHandler {

    // Contador para los errores
    private int cntError = 0;

    // Cada que hay un error devuelve linea, columna y lexema
    public void rErrorLexico(int linea, int columna, String lexema) {
        System.out.printf("[Error Lexico] Linea %d, Columna %d: Lexema no reconocida '%s'%n", linea, columna, lexema);
        cntError++;// Incrementa el contador 
    }

    // Devuelve cuantos errores hay
    public int getCntError() {
        return cntError;
    }
    

}
