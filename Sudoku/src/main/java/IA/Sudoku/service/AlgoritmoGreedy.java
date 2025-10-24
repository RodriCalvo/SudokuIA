package IA.Sudoku.service;

import IA.Sudoku.model.Sudoku;
import org.springframework.stereotype.Service;

@Service
public class AlgoritmoGreedy {
    public boolean solve(Sudoku sudoku) { return solve(sudoku, null); }

    public boolean solve(Sudoku sudoku, SolveTrace trace) {
        if (trace != null) trace.setExplanation("Greedy: completa solo celdas con única opción (no explora ramas). Puede quedarse sin movimientos y no resolver.");
        // Estrategia codiciosa: completar únicamente celdas con opción única
        int[][] grid = sudoku.getGrid();
        boolean changed;
        do {
            changed = false;
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (grid[r][c] == 0) {
                        int last = 0, count = 0;
                        for (int num = 1; num <= 9; num++) {
                            if (isValid(grid, r, c, num)) {
                                last = num; count++;
                                if (count > 1) break;
                            }
                        }
                        if (count == 1) {
                            grid[r][c] = last;
                            changed = true;
                            if (trace != null) trace.addStep("Fija ("+r+","+c+")="+last+" por única opción");
                        }
                    }
                }
            }
        } while (changed);
        int filled = countFilled(grid);
        if (trace != null) {
            trace.setFilledCells(filled);
            trace.setPartialGrid(copyGrid(grid));
        }
        // Si quedan celdas vacías, el greedy no alcanza solución completa
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (grid[r][c] == 0) {
                    if (trace != null) trace.setReason("No hay más celdas con única opción (se requiere exploración)");
                    return false;
                }
        return true;
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

    private int countFilled(int[][] g){
        int k=0; for(int r=0;r<9;r++) for(int c=0;c<9;c++) if(g[r][c]!=0) k++; return k;
    }

    private int[][] copyGrid(int[][] g){
        int[][] out = new int[9][9];
        for(int i=0;i<9;i++) System.arraycopy(g[i],0,out[i],0,9);
        return out;
    }
}