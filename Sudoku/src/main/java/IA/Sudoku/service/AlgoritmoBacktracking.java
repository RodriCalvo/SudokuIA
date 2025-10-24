package IA.Sudoku.service;

import IA.Sudoku.model.Sudoku;
import org.springframework.stereotype.Service;

@Service
public class AlgoritmoBacktracking {

    public boolean solve(Sudoku sudoku) {
        int[][] grid = sudoku.getGrid();
        return backtrack(grid);
    }

    private boolean backtrack(int[][] grid) {
        int[] pos = findEmpty(grid);
        if (pos == null) return true; // solved
        int row = pos[0], col = pos[1];
        for (int num = 1; num <= 9; num++) {
            if (isValid(grid, row, col, num)) {
                grid[row][col] = num;
                if (backtrack(grid)) return true;
                grid[row][col] = 0;
            }
        }
        return false;
    }

    private int[] findEmpty(int[][] grid) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] == 0) return new int[]{r, c};
            }
        }
        return null;
    }

    public boolean isValid(int[][] grid, int row, int col, int num) {
        // row
        for (int c = 0; c < 9; c++) if (grid[row][c] == num) return false;
        // col
        for (int r = 0; r < 9; r++) if (grid[r][col] == num) return false;
        // block
        int br = (row / 3) * 3;
        int bc = (col / 3) * 3;
        for (int r = br; r < br + 3; r++) {
            for (int c = bc; c < bc + 3; c++) {
                if (grid[r][c] == num) return false;
            }
        }
        return true;
    }
}