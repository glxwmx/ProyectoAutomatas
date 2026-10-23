package tecnm.lexer;

/**
 *
 * @author Gustavo M
 */
public class Simbolo {

    private String lexema;
    private String tipoToken;
    private int lineaInicial;
    private int columnaInicial;

    // Contructor 
    public Simbolo(String lexema, String tipoToken, int lineaInicial, int columnaInicial) {
        this.lexema = lexema;
        this.tipoToken = tipoToken;
        this.lineaInicial = lineaInicial;
        this.columnaInicial = columnaInicial;
    }

    // Metodos Getters para devolver los datos
    public String getLexema() {
        return lexema;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public int getLineaInicial() {
        return lineaInicial;
    }

    public int getColumnaInicial() {
        return columnaInicial;
    }

}
