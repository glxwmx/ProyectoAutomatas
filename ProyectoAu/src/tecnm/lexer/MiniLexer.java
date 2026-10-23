package tecnm.lexer;

import java.io.*;
import java.util.*;

public class MiniLexer {

    private String contenido;
    private int posicion = 0;
    private int lineaActual = 1;
    private int columnaActual = 1;

    private final HashMap<String, Simbolo> tablaSimbolos = new HashMap<>();
    private final ErrorHandler errorHandler = new ErrorHandler();

    private static final Set<String> PALABRAS_RESERVADAS =
            new HashSet<>(Arrays.asList(
                    "if", "else", "then", "while"
            ));

    public MiniLexer(String rutaArchivo) throws IOException {
        contenido = leerArchivo(rutaArchivo);
    }

    // Lee el archivo completo
    private String leerArchivo(String ruta) throws IOException {
        StringBuilder texto = new StringBuilder();

        try (BufferedReader lector =
                new BufferedReader(new FileReader(ruta))) {

            String linea;

            while ((linea = lector.readLine()) != null) {
                texto.append(linea).append('\n');
            }
        }

        return texto.toString();
    }

    // Devuelve el carácter actual
    private char peek() {
        if (posicion >= contenido.length()) {
            return '\0';
        }

        return contenido.charAt(posicion);
    }

    // Consume un carácter y actualiza las coordenadas
    private char avanzar() {
        if (posicion >= contenido.length()) {
            return '\0';
        }

        char caracter = contenido.charAt(posicion++);

        if (caracter == '\n') {
            lineaActual++;
            columnaActual = 1;
        } else if (caracter == '\t') {
            columnaActual += 4;
        } else {
            columnaActual++;
        }

        return caracter;
    }

    // Ignora los espacios y saltos de línea
    private void ignorarEspacios() {
        while (posicion < contenido.length()
                && Character.isWhitespace(peek())) {
            avanzar();
        }
    }

    // Ignora los comentarios de una línea y de varias líneas
    private boolean ignorarComentarios() {
        if (peek() != '/') {
            return false;
        }

        if (posicion + 1 >= contenido.length()) {
            return false;
        }

        char siguiente = contenido.charAt(posicion + 1);

        // Comentario de una línea
        if (siguiente == '/') {
            while (posicion < contenido.length()
                    && peek() != '\n') {
                avanzar();
            }

            return true;
        }

        // Comentario de varias líneas
        if (siguiente == '*') {
            avanzar();
            avanzar();

            while (posicion < contenido.length()) {
                if (peek() == '*'
                        && posicion + 1 < contenido.length()
                        && contenido.charAt(posicion + 1) == '/') {

                    avanzar();
                    avanzar();
                    return true;
                }

                avanzar();
            }

            return true;
        }

        return false;
    }

    // Registra un identificador en la tabla de símbolos
    private String registrarIdentificador(
            String nombre, int linea, int columna) {

        if (!tablaSimbolos.containsKey(nombre)) {
            Simbolo simbolo = new Simbolo(
                    nombre, "TOKEN_IDENTIFIER", linea, columna);

            tablaSimbolos.put(nombre, simbolo);

            return " -> [Registrado en Tabla de Simbolos]";
        }

        return " -> [Referenciado en Tabla de Simbolos]";
    }

    // Analiza identificadores y palabras reservadas
    private Token leerIdentificador(int linea, int columna) {
        StringBuilder palabra = new StringBuilder();

        while (Character.isLetterOrDigit(peek())
                || peek() == '_') {
            palabra.append(avanzar());
        }

        String lexema = palabra.toString();

        if (PALABRAS_RESERVADAS.contains(lexema)) {
            return new Token(
                    Token.Tipo.TOKEN_RESERVED,
                    lexema, linea, columna);
        }

        Token token = new Token(
                Token.Tipo.TOKEN_IDENTIFIER,
                lexema, linea, columna);

        token.setmAd(
                registrarIdentificador(lexema, linea, columna));

        return token;
    }

    // Analiza números enteros y decimales
    private Token leerNumero(int linea, int columna) {
        StringBuilder numero = new StringBuilder();
        int puntos = 0;

        while (Character.isDigit(peek()) || peek() == '.') {
            if (peek() == '.') {
                puntos++;
            }

            numero.append(avanzar());

            if (puntos > 1) {
                while (Character.isDigit(peek()) || peek() == '.') {
                    numero.append(avanzar());
                }

                errorHandler.rErrorLexico(
                        linea, columna, numero.toString());

                recuperarError();
                return null;
            }
        }

        Token.Tipo tipo = puntos == 1
                ? Token.Tipo.TOKEN_FLOAT_LITERAL
                : Token.Tipo.TOKEN_INT_LITERAL;

        return new Token(tipo, numero.toString(), linea, columna);
    }

    // Analiza cadenas entre comillas dobles
    private Token leerCadena(int linea, int columna) {
        StringBuilder cadena = new StringBuilder();
        cadena.append(avanzar());

        boolean cerrada = false;

        while (posicion < contenido.length()) {
            char caracter = peek();

            if (caracter == '\n' || caracter == '\r') {
                break;
            }

            cadena.append(avanzar());

            if (caracter == '"') {
                cerrada = true;
                break;
            }
        }

        if (!cerrada) {
            errorHandler.rErrorLexico(
                    linea, columna, cadena.toString());

            recuperarError();
            return null;
        }

        return new Token(
                Token.Tipo.TOKEN_STRING_LITERAL,
                cadena.toString(), linea, columna);
    }

    // Analiza operadores relacionales, lógicos y de asignación
    private Token leerOperador(int linea, int columna) {
        char caracter = avanzar();
        String operador = String.valueOf(caracter);

        if (caracter == '=') {
            if (peek() == '=') {
                avanzar();
                operador = "==";

                return new Token(
                        Token.Tipo.TOKEN_REL_OP,
                        operador, linea, columna);
            }

            return new Token(
                    Token.Tipo.TOKEN_ASSIGN_OP,
                    operador, linea, columna);
        }

        if (caracter == '>' || caracter == '<') {
            if (peek() == '=') {
                operador += avanzar();
            }

            return new Token(
                    Token.Tipo.TOKEN_REL_OP,
                    operador, linea, columna);
        }

        if (caracter == '&' && peek() == '&') {
            avanzar();

            return new Token(
                    Token.Tipo.TOKEN_LOGIC_OP,
                    "&&", linea, columna);
        }

        if (caracter == '|' && peek() == '|') {
            avanzar();

            return new Token(
                    Token.Tipo.TOKEN_LOGIC_OP,
                    "||", linea, columna);
        }

        if (caracter == '!') {
            return new Token(
                    Token.Tipo.TOKEN_LOGIC_OP,
                    operador, linea, columna);
        }

        if (caracter == '+' || caracter == '-'
                || caracter == '*' || caracter == '/') {
            return new Token(
                    Token.Tipo.TOKEN_ARITH_OP,
                    operador, linea, columna);
        }

        return null;
    }

    // Analiza paréntesis, llaves, comas y punto y coma
    private Token leerSimbolo(int linea, int columna) {
        char caracter = peek();

        if ("(){};,".indexOf(caracter) >= 0) {
            avanzar();

            return new Token(
                    Token.Tipo.TOKEN_SPECIAL_SYMBOL,
                    String.valueOf(caracter), linea, columna);
        }

        return null;
    }

    // Omite los caracteres problemáticos hasta un punto de recuperación
    private void recuperarError() {
        while (posicion < contenido.length()) {
            char caracter = peek();

            if (caracter == ';' || caracter == ' '
                    || caracter == '\n' || caracter == '\t'
                    || caracter == '\r') {
                break;
            }

            avanzar();
        }
    }

    // Obtiene el siguiente token del archivo
    public Token siguienteToken() {
        while (posicion < contenido.length()) {
            ignorarEspacios();

            if (posicion >= contenido.length()) {
                break;
            }

            if (ignorarComentarios()) {
                continue;
            }

            char caracter = peek();
            int linea = lineaActual;
            int columna = columnaActual;

            if (Character.isLetter(caracter) || caracter == '_') {
                return leerIdentificador(linea, columna);
            }

            if (Character.isDigit(caracter)) {
                return leerNumero(linea, columna);
            }

            if (caracter == '"') {
                return leerCadena(linea, columna);
            }

            if ("=><!&|+-*/".indexOf(caracter) >= 0) {
                Token token = leerOperador(linea, columna);

                if (token != null) {
                    return token;
                }
            }

            Token simbolo = leerSimbolo(linea, columna);

            if (simbolo != null) {
                return simbolo;
            }

            char desconocido = avanzar();

            errorHandler.rErrorLexico(
                    linea, columna, String.valueOf(desconocido));

            recuperarError();
            return null;
        }

        return new Token(
                Token.Tipo.TOKEN_EOF,
                "EOF", lineaActual, columnaActual);
    }

    public static void main(String[] args) {
        String archivo = args.length > 0
                ? args[0]
                : "src\\tecnm\\lexer\\test_suite.txt";

        System.out.printf(
                ">>> INICIANDO ANALISIS LEXICO: %s <<<%n",
                archivo);

        try {
            MiniLexer lexer = new MiniLexer(archivo);
            Token token;

            do {
                token = lexer.siguienteToken();

                if (token != null
                        && token.getTipo() != Token.Tipo.TOKEN_EOF) {

                    System.out.printf("[Linea %d, Columna %d] %s: '%s'", token.getLinea(),token.getColumna(),token.getTipo(),token.getLexema());

                    if (token.getmAd() != null) {
                        System.out.print(
                                token.getmAd());
                    }

                    System.out.println();
                }
            } while (token == null
                    || token.getTipo() != Token.Tipo.TOKEN_EOF);

            System.out.printf(
                    ">>> ANALISIS LEXICO FINALIZADO CON %d ERROR(ES) <<<%n",
                    lexer.errorHandler.getCntError());

        } catch (IOException e) {
            System.err.println(
                    "Error al leer el archivo: " + e.getMessage());
        }
    }
}
