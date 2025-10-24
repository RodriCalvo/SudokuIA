package IA.Sudoku.service;

import IA.Sudoku.model.Sudoku;
import org.springframework.stereotype.Service;
import java.util.ArrayDeque;

@Service
public class AlgoritmoDFS {
    public boolean solve(Sudoku sudoku) { return solve(sudoku, null); }

    public boolean solve(Sudoku sudoku, SolveTrace trace) {
        if (trace != null) trace.setExplanation("DFS iterativo: explora en profundidad con pila; memoria O(d). Completo pero puede tardar.");
        int[][] start = copyGrid(sudoku.getGrid());
        // Pila de estados (DFS iterativo)
        ArrayDeque<int[][]> stack = new ArrayDeque<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            int[][] grid = stack.pop();
            if (trace != null) {
                trace.incNodesExpanded();
                trace.setMaxDepth(stack.size());
            }
            int[] pos = findEmpty(grid);
            if (pos == null) { // resuelto
                sudoku.setGrid(copyGrid(grid));
                return true;
            }
            int r = pos[0], c = pos[1];
            // Ordenar candidatos descendente no importa para corrección
            for (int num = 9; num >= 1; num--) {
                if (isValid(grid, r, c, num)) {
                    int[][] next = copyGrid(grid);
                    next[r][c] = num;
                    stack.push(next);
                    if (trace != null) trace.addStep("Profundiza ("+r+","+c+")="+num);
                }
            }
        }
        return false;
    }

    // --- Utilitarios ---
    private int[] findEmpty(int[][] grid) {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (grid[r][c] == 0) return new int[]{r, c};
        return null;
    }

    private int[][] copyGrid(int[][] g) {
        int[][] out = new int[9][9];
        for (int i = 0; i < 9; i++) System.arraycopy(g[i], 0, out[i], 0, 9);
        return out;
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
}