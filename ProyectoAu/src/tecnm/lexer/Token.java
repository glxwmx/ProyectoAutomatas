package tecnm.lexer;

/**
 *
 * @author Gustavo M
 */
public class Token {

    /* Categoriza las palabras reservadas(if, else, while), identificadores(contador, x), enteros(20)
        numeros flotantes(3.14), cadenas literales("Hola"), operadores(+, -, *, /) 
        relacionales(>=, <=, ==, !=), asignacion(=), logicos(&&, ||, !) y simbolos especiales((), {}, [], ;, ,)
    */
    public enum Tipo {
        TOKEN_RESERVED,
        TOKEN_IDENTIFIER,
        TOKEN_INT_LITERAL,
        TOKEN_FLOAT_LITERAL,
        TOKEN_STRING_LITERAL,
        TOKEN_ARITH_OP,
        TOKEN_REL_OP,
        TOKEN_ASSIGN_OP,
        TOKEN_LOGIC_OP,
        TOKEN_SPECIAL_SYMBOL,
        TOKEN_EOF
    }

    private Tipo tipo;
    private String lexema;
    private int linea;
    private int columna;

    // Constructor 
    public Token(Tipo tipo, String lexema, int linea, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
    }

    // Comentario adicional
    private String mAd = null;

    // Getters y Setter 
    public Tipo getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public String getmAd() {
        return mAd;
    }

    public void setmAd(String mAd) {
        this.mAd = mAd;
    }

    // Reescritura del metodo toString() para dar precision al error
    @Override
    public String toString() {
        return String.format("[Linea %d, Columna %d] %s: '%s'", linea, columna, tipo, lexema);

    }

}
