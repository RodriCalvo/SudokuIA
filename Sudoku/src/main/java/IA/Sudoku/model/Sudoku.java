package IA.Sudoku.model;

public class Sudoku {
    private int[][] grid;

    public Sudoku() {
        this.grid = new int[9][9];
    }

    public Sudoku(int[][] grid) {
        if (grid == null) this.grid = new int[9][9];
        else this.grid = grid;
    }

    public int[][] getGrid() { return grid; }
    public void setGrid(int[][] grid) { this.grid = grid; }
}