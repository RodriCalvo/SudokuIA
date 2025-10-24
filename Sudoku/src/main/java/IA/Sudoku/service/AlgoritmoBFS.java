package IA.Sudoku.service;

import IA.Sudoku.model.Sudoku;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AlgoritmoBFS {

    public boolean solve(Sudoku sudoku) { return solve(sudoku, null); }

    public boolean solve(Sudoku sudoku, SolveTrace trace) {
        if (trace != null) trace.setExplanation("BFS: explora en anchura el espacio de estados; prioridad por MRV. Memoria puede crecer rápidamente.");
        int[][] start = copyGrid(sudoku.getGrid());
        if (isSolved(start)) return true;

        // Cola de estados (BFS)
        ArrayDeque<int[][]> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            int[][] grid = queue.poll();
            if (trace != null) {
                trace.incNodesExpanded();
                trace.setFrontierPeak(Math.max(trace.getFrontierPeak(), queue.size()));
            }

            // Si ya está resuelto, copiar a la instancia y terminar
            if (isSolved(grid)) {
                sudoku.setGrid(copyGrid(grid));
                return true;
            }

            // Elegimos una celda vacía (heurística MRV para reducir el branching)
            int[] pos = selectCellWithFewestCandidates(grid);
            if (pos == null) continue; // sin vacías pero no marcado solved
            int r = pos[0], c = pos[1];
            List<Integer> candidates = getCandidates(grid, r, c);
            if (candidates.isEmpty()) {
                // Estado inválido: no se expande
                if (trace != null) trace.addStep("Poda por 0 candidatos en ("+r+","+c+")");
                continue;
            }

            // Expande en anchura
            for (int num : candidates) {
                int[][] next = copyGrid(grid);
                next[r][c] = num;
                queue.add(next);
                if (trace != null) trace.addStep("Expande ("+r+","+c+")="+num+" (BFS)");
            }
        }
        return false;
    }

    // --- Utilitarios locales ---
    private boolean isSolved(int[][] grid) {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (grid[r][c] == 0) return false;
        return true;
    }

    private int[][] copyGrid(int[][] g) {
        int[][] out = new int[9][9];
        for (int i = 0; i < 9; i++) System.arraycopy(g[i], 0, out[i], 0, 9);
        return out;
    }

    private int[] selectCellWithFewestCandidates(int[][] grid) {
        int bestCount = Integer.MAX_VALUE;
        int[] best = null;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] == 0) {
                    int count = getCandidates(grid, r, c).size();
                    if (count < bestCount) {
                        bestCount = count;
                        best = new int[]{r, c};
                        if (bestCount == 1) return best; // óptimo
                    }
                }
            }
        }
        return best;
    }

    private List<Integer> getCandidates(int[][] grid, int row, int col) {
        List<Integer> list = new ArrayList<>(9);
        for (int num = 1; num <= 9; num++) if (isValid(grid, row, col, num)) list.add(num);
        return list;
    }

    private boolean isValid(int[][] grid, int row, int col, int num) {
        // fila
        for (int c = 0; c < 9; c++) if (grid[row][c] == num) return false;
        // columna
        for (int r = 0; r < 9; r++) if (grid[r][col] == num) return false;
        // subcuadro 3x3
        int br = (row / 3) * 3, bc = (col / 3) * 3;
        for (int r = br; r < br + 3; r++)
            for (int c = bc; c < bc + 3; c++)
                if (grid[r][c] == num) return false;
        return true;
    }
}