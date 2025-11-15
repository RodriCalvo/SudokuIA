package IA.Sudoku.service;

import IA.Sudoku.model.Cell;
import IA.Sudoku.model.Sudoku;
import IA.Sudoku.repository.CellRepository;
import IA.Sudoku.repository.SudokuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SudokuGraphService {
    
    private final SudokuRepository sudokuRepository;
    private final CellRepository cellRepository;
    
    public SudokuGraphService(SudokuRepository sudokuRepository, CellRepository cellRepository) {
        this.sudokuRepository = sudokuRepository;
        this.cellRepository = cellRepository;
    }
    
    /**
     * Guarda un Sudoku como grafo en Neo4j:
     * - 81 nodos Cell (uno por celda)
     * - Relaciones SAME_ROW, SAME_COL, SAME_REGION
     */
    @Transactional
    public Sudoku saveSudokuAsGraph(int[][] initialGrid, int[][] solvedGrid, 
                                     String algorithm, Double runtimeMs, Double memoryMB,
                                     String difficulty, Integer emptyCells) {
        
        // 1. Guardar nodo Sudoku (metadata)
        Sudoku sudoku = new Sudoku(initialGrid);
        sudoku.setSolvedGrid(solvedGrid);
        sudoku.setAlgorithm(algorithm);
        sudoku.setRuntimeMs(runtimeMs);
        sudoku.setMemoryMB(memoryMB);
        sudoku.setSolved(true);
        sudoku.setDifficulty(difficulty);
        sudoku.setEmptyCells(emptyCells);
        
        Sudoku savedSudoku = sudokuRepository.save(sudoku);
        Long sudokuId = savedSudoku.getId();
        
        // 2. Crear 81 nodos Cell
        Map<String, Cell> cellMap = new HashMap<>();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Cell cell = new Cell(r, c, solvedGrid[r][c], initialGrid[r][c], sudokuId);
                cellMap.put(r + "," + c, cell);
            }
        }
        
        // 3. Crear relaciones (restricciones del Sudoku)
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Cell currentCell = cellMap.get(r + "," + c);
                
                // Relaciones con misma fila
                for (int c2 = 0; c2 < 9; c2++) {
                    if (c2 != c) {
                        currentCell.getSameRowCells().add(cellMap.get(r + "," + c2));
                    }
                }
                
                // Relaciones con misma columna
                for (int r2 = 0; r2 < 9; r2++) {
                    if (r2 != r) {
                        currentCell.getSameColCells().add(cellMap.get(r2 + "," + c));
                    }
                }
                
                // Relaciones con misma región 3x3
                int regionRow = (r / 3) * 3;
                int regionCol = (c / 3) * 3;
                for (int r2 = regionRow; r2 < regionRow + 3; r2++) {
                    for (int c2 = regionCol; c2 < regionCol + 3; c2++) {
                        if (r2 != r || c2 != c) {
                            currentCell.getSameRegionCells().add(cellMap.get(r2 + "," + c2));
                        }
                    }
                }
            }
        }
        
        // 4. Guardar todas las celdas con sus relaciones
        cellRepository.saveAll(cellMap.values());
        
        return savedSudoku;
    }
    
    /**
     * Obtiene estadísticas del grafo de un Sudoku
     */
    public Map<String, Object> getSudokuGraphStats(Long sudokuId) {
        List<Cell> cells = cellRepository.findBySudokuId(sudokuId);
        
        long filledCells = cells.stream().filter(c -> c.getInitialValue() != 0).count();
        long emptyCells = cells.stream().filter(c -> c.getInitialValue() == 0).count();
        
        // Cada celda tiene 20 vecinos (8 en fila + 8 en columna + 4 en región que no están en fila/col)
        // Total aristas = 81 * 20 / 2 = 810 (dividido 2 porque son bidireccionales)
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("sudokuId", sudokuId);
        stats.put("totalCells", cells.size());
        stats.put("filledCells", filledCells);
        stats.put("emptyCells", emptyCells);
        stats.put("totalEdges", 810); // Constante para Sudoku 9x9
        stats.put("edgesPerCell", 20);
        
        return stats;
    }
    
    /**
     * Visualiza el Sudoku reconstruido desde el grafo
     */
    public int[][] getSudokuGridFromGraph(Long sudokuId) {
        List<Cell> cells = cellRepository.findBySudokuId(sudokuId);
        int[][] grid = new int[9][9];
        
        for (Cell cell : cells) {
            grid[cell.getRow()][cell.getCol()] = cell.getValue();
        }
        
        return grid;
    }
}
