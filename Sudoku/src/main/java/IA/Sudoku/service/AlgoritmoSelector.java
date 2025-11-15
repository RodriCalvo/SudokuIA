package IA.Sudoku.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AlgoritmoSelector {

    public SelectionResult selectBestAlgorithm(int[][] grid) {
        int emptyCells = countEmptyCells(grid);
        int totalCells = 81;
        double fillPercentage = ((totalCells - emptyCells) / (double) totalCells) * 100;
        
        int constraintLevel = analyzeConstraints(grid);
        
        String bestAlgorithm;
        String reason;
        Map<String, String> rejectedAlgorithms = new LinkedHashMap<>();
        
        // Análisis para seleccionar el mejor algoritmo
        if (emptyCells <= 10 && constraintLevel >= 7) {
            // Muy pocas celdas vacías y alta restricción
            bestAlgorithm = "greedy";
            reason = String.format(
                "Sudoku con %.1f%% completado y alta restricción (nivel %d/10). " +
                "Greedy es óptimo porque la mayoría de las celdas vacías tendrán única opción, " +
                "evitando búsqueda exhaustiva innecesaria.",
                fillPercentage, constraintLevel
            );
            
            rejectedAlgorithms.put("backtracking", "Innecesariamente complejo para un puzzle casi completo");
            rejectedAlgorithms.put("dfs", "Desperdicia recursos explorando ramas cuando hay opciones únicas");
            rejectedAlgorithms.put("bfs", "Consumiría mucha memoria para un problema que Greedy resuelve directamente");
            rejectedAlgorithms.put("dp", "Similar a Greedy pero con overhead adicional de máscaras de bits");
            rejectedAlgorithms.put("branchbound", "La poda sería mínima dado que Greedy ya encuentra la solución");
            
        } else if (emptyCells <= 20 && constraintLevel >= 6) {
            // Moderadamente lleno con buena restricción
            bestAlgorithm = "dp";
            reason = String.format(
                "Sudoku con %.1f%% completado y buena restricción (nivel %d/10). " +
                "DP con propagación de restricciones optimiza el llenado sin explorar ramas innecesarias, " +
                "usando máscaras de bits eficientemente.",
                fillPercentage, constraintLevel
            );
            
            rejectedAlgorithms.put("greedy", "Podría quedarse sin opciones únicas en celdas complejas");
            rejectedAlgorithms.put("backtracking", "No aprovecha la propagación de restricciones disponible");
            rejectedAlgorithms.put("dfs", "Menos eficiente que DP para propagar restricciones");
            rejectedAlgorithms.put("bfs", "Exploración en anchura excesiva cuando hay restricciones claras");
            rejectedAlgorithms.put("branchbound", "Más overhead que DP sin ventaja significativa");
            
        } else if (emptyCells <= 35) {
            // Complejidad media - balance entre completitud y restricción
            bestAlgorithm = "backtracking";
            reason = String.format(
                "Sudoku con %.1f%% completado (complejidad media). " +
                "Backtracking es el balance óptimo: completo, eficiente en memoria O(d), " +
                "y rápido para la mayoría de casos sin overhead innecesario.",
                fillPercentage
            );
            
            rejectedAlgorithms.put("greedy", "Incompleto - se quedaría sin movimientos en celdas ambiguas");
            rejectedAlgorithms.put("dp", "Incompleto - la propagación sola no garantiza resolución");
            rejectedAlgorithms.put("dfs", "Similar a backtracking pero con overhead de copiar estados completos");
            rejectedAlgorithms.put("bfs", "Consumo excesivo de memoria O(b^d) sin beneficio sobre backtracking");
            rejectedAlgorithms.put("branchbound", "Overhead de cola de prioridad sin mejora significativa");
            
        } else if (emptyCells <= 50) {
            // Alta complejidad - necesita estrategia inteligente
            bestAlgorithm = "branchbound";
            reason = String.format(
                "Sudoku con %.1f%% completado (alta complejidad). " +
                "Branch & Bound con heurística MRV (Minimum Remaining Values) poda eficientemente " +
                "el espacio de búsqueda, priorizando celdas con menos candidatos.",
                fillPercentage
            );
            
            rejectedAlgorithms.put("greedy", "Definitivamente incompleto - demasiadas celdas vacías");
            rejectedAlgorithms.put("dp", "Incompleto - requiere búsqueda para tantas celdas vacías");
            rejectedAlgorithms.put("backtracking", "Sin heurísticas, exploraría muchas ramas inútiles");
            rejectedAlgorithms.put("dfs", "Exploración ciega consume tiempo sin priorizar candidatos óptimos");
            rejectedAlgorithms.put("bfs", "Memoria prohibitiva O(b^d) con tantas celdas vacías");
            
        } else {
            // Muy alta complejidad (>50 celdas vacías)
            bestAlgorithm = "branchbound";
            reason = String.format(
                "Sudoku con %.1f%% completado (muy alta complejidad). " +
                "Branch & Bound es crítico: su poda agresiva y heurística MRV son necesarias " +
                "para evitar explosión combinatoria en espacio de búsqueda masivo.",
                fillPercentage
            );
            
            rejectedAlgorithms.put("greedy", "Completamente inviable - incompleto y se detendrá rápidamente");
            rejectedAlgorithms.put("dp", "Incompleto - imposible resolver solo con propagación");
            rejectedAlgorithms.put("backtracking", "Exploración sin poda tomaría tiempo exponencial excesivo");
            rejectedAlgorithms.put("dfs", "Sin poda ni heurísticas, tiempo de ejecución inaceptable");
            rejectedAlgorithms.put("bfs", "Consumo de memoria masivo - causaría OutOfMemoryError");
        }
        
        return new SelectionResult(
            bestAlgorithm,
            reason,
            rejectedAlgorithms,
            emptyCells,
            fillPercentage,
            constraintLevel
        );
    }
    
    private int countEmptyCells(int[][] grid) {
        int count = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] == 0) count++;
            }
        }
        return count;
    }
    
    private int analyzeConstraints(int[][] grid) {
        // Analiza qué tan restringido está el puzzle (0-10)
        int totalConstraints = 0;
        int emptyCells = 0;
        
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] == 0) {
                    emptyCells++;
                    int candidates = countCandidates(grid, r, c);
                    // Menos candidatos = más restricción
                    totalConstraints += (10 - candidates);
                }
            }
        }
        
        if (emptyCells == 0) return 10;
        return Math.min(10, totalConstraints / emptyCells);
    }
    
    private int countCandidates(int[][] grid, int row, int col) {
        int count = 0;
        for (int num = 1; num <= 9; num++) {
            if (isValid(grid, row, col, num)) count++;
        }
        return count;
    }
    
    private boolean isValid(int[][] grid, int row, int col, int num) {
        for (int c = 0; c < 9; c++) if (grid[row][c] == num) return false;
        for (int r = 0; r < 9; r++) if (grid[r][col] == num) return false;
        int br = (row / 3) * 3, bc = (col / 3) * 3;
        for (int r = br; r < br + 3; r++)
            for (int c = bc; c < bc + 3; c++)
                if (grid[r][c] == num) return false;
        return true;
    }
    
    public static class SelectionResult {
        private final String selectedAlgorithm;
        private final String reason;
        private final Map<String, String> rejectedAlgorithms;
        private final int emptyCells;
        private final double fillPercentage;
        private final int constraintLevel;
        
        public SelectionResult(String selectedAlgorithm, String reason, 
                             Map<String, String> rejectedAlgorithms,
                             int emptyCells, double fillPercentage, int constraintLevel) {
            this.selectedAlgorithm = selectedAlgorithm;
            this.reason = reason;
            this.rejectedAlgorithms = rejectedAlgorithms;
            this.emptyCells = emptyCells;
            this.fillPercentage = fillPercentage;
            this.constraintLevel = constraintLevel;
        }
        
        public String getSelectedAlgorithm() { return selectedAlgorithm; }
        public String getReason() { return reason; }
        public Map<String, String> getRejectedAlgorithms() { return rejectedAlgorithms; }
        public int getEmptyCells() { return emptyCells; }
        public double getFillPercentage() { return fillPercentage; }
        public int getConstraintLevel() { return constraintLevel; }
    }
}
