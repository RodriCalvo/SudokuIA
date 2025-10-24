package IA.Sudoku.model;

public class Celda {
    private int fila;
    private int columna;
    private int valor;

    public Celda() {}

    public Celda(int fila, int columna, int valor) {
        this.fila = fila;
        this.columna = columna;
        this.valor = valor;
    }

    public int getFila() { return fila; }
    public void setFila(int fila) { this.fila = fila; }
    public int getColumna() { return columna; }
    public void setColumna(int columna) { this.columna = columna; }
    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }
}