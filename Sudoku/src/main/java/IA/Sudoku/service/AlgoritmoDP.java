package IA.Sudoku.service;

import IA.Sudoku.model.Sudoku;
import org.springframework.stereotype.Service;

@Service
public class AlgoritmoDP {
    public boolean solve(Sudoku sudoku) { return solve(sudoku, null); }

    public boolean solve(Sudoku sudoku, SolveTrace trace) {
        if (trace != null) trace.setExplanation("DP (propagación): usa máscaras para descartar candidatos y completar únicas opciones. No explora múltiples ramas.");
        // Programación dinámica/propagación de restricciones mediante bitmasks
        int[][] grid = sudoku.getGrid();
        int[] rowMask = new int[9];
        int[] colMask = new int[9];
        int[] boxMask = new int[9];

        // Inicializar máscaras (bit 0..8 representa números 1..9)
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int v = grid[r][c];
                if (v != 0) {
                    int bit = 1 << (v - 1);
                    rowMask[r] |= bit;
                    colMask[c] |= bit;
                    boxMask[boxIndex(r, c)] |= bit;
                }
            }
        }

    boolean changed;
        do {
            changed = false;
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (grid[r][c] == 0) {
                        int used = rowMask[r] | colMask[c] | boxMask[boxIndex(r, c)];
                        int candidates = (~used) & 0x1FF; // 9 bits
                        if (candidates == 0) return false; // inconsistencia => poda
                        if ((candidates & (candidates - 1)) == 0) { // solo 1 candidato
                            int num = Integer.numberOfTrailingZeros(candidates) + 1;
                            grid[r][c] = num;
                            int bit = 1 << (num - 1);
                            rowMask[r] |= bit;
                            colMask[c] |= bit;
                            boxMask[boxIndex(r, c)] |= bit;
                            changed = true;
                            if (trace != null) trace.addStep("Fija ("+r+","+c+")="+num+" por máscara única");
                        }
                    }
                }
            }
        } while (changed);

        // Si no hay más cambios y hay celdas vacías, este método DP no puede concluir
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (grid[r][c] == 0) {
                    if (trace != null) {
                        int filled = countFilled(grid);
                        int remaining = 81 - filled;
                        trace.setReason(String.format(
                            "❌ DP incompleto (%d celdas sin resolver). " +
                            "La propagación de restricciones mediante bitmasks eliminó candidatos imposibles, " +
                            "pero las celdas restantes tienen múltiples candidatos válidos. " +
                            "DP puro no adivina ni hace backtracking: solo deduce por lógica. " +
                            "Se requiere un algoritmo de búsqueda para probar combinaciones.",
                            remaining
                        ));
                        trace.setFilledCells(filled);
                        trace.setPartialGrid(copyGrid(grid));
                    }
                    return false;
                }
        if (trace != null) {
            trace.setFilledCells(81);
            trace.setPartialGrid(copyGrid(grid));
        }
        return true;
    }

    private int boxIndex(int r, int c) { return (r / 3) * 3 + (c / 3); }

    private int countFilled(int[][] g){ int k=0; for(int r=0;r<9;r++) for(int c=0;c<9;c++) if(g[r][c]!=0) k++; return k; }
    private int[][] copyGrid(int[][] g){ int[][] o=new int[9][9]; for(int i=0;i<9;i++) System.arraycopy(g[i],0,o[i],0,9); return o; }
}