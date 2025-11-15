package IA.Sudoku.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Cell")
public class Cell {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Property("row")
    private Integer row;  // 0-8
    
    @Property("col")
    private Integer col;  // 0-8
    
    @Property("value")
    private Integer value;  // 0-9 (0 = vacía)
    
    @Property("initialValue")
    private Integer initialValue;  // Valor original (0 si estaba vacía)
    
    @Property("region")
    private Integer region;  // 0-8 (región 3x3)
    
    @Property("sudokuId")
    private Long sudokuId;  // Referencia al Sudoku padre
    
    // Relaciones con celdas en la misma fila
    @Relationship(type = "SAME_ROW", direction = Relationship.Direction.OUTGOING)
    private Set<Cell> sameRowCells = new HashSet<>();
    
    // Relaciones con celdas en la misma columna
    @Relationship(type = "SAME_COL", direction = Relationship.Direction.OUTGOING)
    private Set<Cell> sameColCells = new HashSet<>();
    
    // Relaciones con celdas en la misma región 3x3
    @Relationship(type = "SAME_REGION", direction = Relationship.Direction.OUTGOING)
    private Set<Cell> sameRegionCells = new HashSet<>();
    
    public Cell() {}
    
    public Cell(Integer row, Integer col, Integer value, Integer initialValue, Long sudokuId) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.initialValue = initialValue;
        this.region = (row / 3) * 3 + (col / 3);
        this.sudokuId = sudokuId;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getRow() { return row; }
    public void setRow(Integer row) { this.row = row; }
    
    public Integer getCol() { return col; }
    public void setCol(Integer col) { this.col = col; }
    
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    
    public Integer getInitialValue() { return initialValue; }
    public void setInitialValue(Integer initialValue) { this.initialValue = initialValue; }
    
    public Integer getRegion() { return region; }
    public void setRegion(Integer region) { this.region = region; }
    
    public Long getSudokuId() { return sudokuId; }
    public void setSudokuId(Long sudokuId) { this.sudokuId = sudokuId; }
    
    public Set<Cell> getSameRowCells() { return sameRowCells; }
    public void setSameRowCells(Set<Cell> sameRowCells) { this.sameRowCells = sameRowCells; }
    
    public Set<Cell> getSameColCells() { return sameColCells; }
    public void setSameColCells(Set<Cell> sameColCells) { this.sameColCells = sameColCells; }
    
    public Set<Cell> getSameRegionCells() { return sameRegionCells; }
    public void setSameRegionCells(Set<Cell> sameRegionCells) { this.sameRegionCells = sameRegionCells; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return id != null && id.equals(cell.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
