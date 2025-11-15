package IA.Sudoku.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.LocalDateTime;

@Node("Sudoku")
public class Sudoku {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Property("initialGrid")
    private String initialGrid; // Serializado como string "1,2,0,4..."
    
    @Property("solvedGrid")
    private String solvedGrid;
    
    @Property("algorithm")
    private String algorithm;
    
    @Property("runtimeMs")
    private Double runtimeMs;
    
    @Property("memoryMB")
    private Double memoryMB;
    
    @Property("solved")
    private Boolean solved;
    
    @Property("createdAt")
    private LocalDateTime createdAt;
    
    @Property("difficulty")
    private String difficulty;
    
    @Property("emptyCells")
    private Integer emptyCells;
    
    // Grid en memoria (no se guarda en Neo4j)
    private transient int[][] grid;

    public Sudoku() {
        this.grid = new int[9][9];
        this.createdAt = LocalDateTime.now();
    }

    public Sudoku(int[][] grid) {
        if (grid == null) this.grid = new int[9][9];
        else this.grid = grid;
        this.initialGrid = gridToString(grid);
        this.createdAt = LocalDateTime.now();
    }

    // Convertir grid 9x9 a String "1,2,0,4,..."
    private String gridToString(int[][] g) {
        if (g == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                sb.append(g[r][c]);
                if (r < 8 || c < 8) sb.append(",");
            }
        }
        return sb.toString();
    }
    
    // Convertir String "1,2,0,4,..." a grid 9x9
    private int[][] stringToGrid(String s) {
        if (s == null || s.isEmpty()) return new int[9][9];
        int[][] g = new int[9][9];
        String[] parts = s.split(",");
        int idx = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (idx < parts.length) {
                    g[r][c] = Integer.parseInt(parts[idx++]);
                }
            }
        }
        return g;
    }

    public int[][] getGrid() { 
        if (grid == null && initialGrid != null) {
            grid = stringToGrid(initialGrid);
        }
        return grid; 
    }
    
    public void setGrid(int[][] grid) { 
        this.grid = grid;
        this.initialGrid = gridToString(grid);
    }
    
    public void setSolvedGrid(int[][] grid) {
        this.solvedGrid = gridToString(grid);
    }
    
    public int[][] getSolvedGridArray() {
        return stringToGrid(solvedGrid);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getInitialGrid() { return initialGrid; }
    public void setInitialGrid(String initialGrid) { this.initialGrid = initialGrid; }
    
    public String getSolvedGrid() { return solvedGrid; }
    public void setSolvedGrid(String solvedGrid) { this.solvedGrid = solvedGrid; }
    
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    
    public Double getRuntimeMs() { return runtimeMs; }
    public void setRuntimeMs(Double runtimeMs) { this.runtimeMs = runtimeMs; }
    
    public Double getMemoryMB() { return memoryMB; }
    public void setMemoryMB(Double memoryMB) { this.memoryMB = memoryMB; }
    
    public Boolean getSolved() { return solved; }
    public void setSolved(Boolean solved) { this.solved = solved; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public Integer getEmptyCells() { return emptyCells; }
    public void setEmptyCells(Integer emptyCells) { this.emptyCells = emptyCells; }
}
