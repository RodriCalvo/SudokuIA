package IA.Sudoku.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Celda")
public class Celda {
    @Id @GeneratedValue
    private Long id;

    @Property("fila")
    private int fila;

    @Property("columna")
    private int columna;

    @Property("valor")
    private int valor;

    public Celda() {}

    public Celda(int fila, int columna, int valor) {
        this.fila = fila;
        this.columna = columna;
        this.valor = valor;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getFila() { return fila; }
    public void setFila(int fila) { this.fila = fila; }
    public int getColumna() { return columna; }
    public void setColumna(int columna) { this.columna = columna; }
    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }
}