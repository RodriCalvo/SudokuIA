package IA.Sudoku.service;

import IA.Sudoku.model.Sudoku;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AlgoritmoBranchBound {
    // Búsqueda con ramificación y poda usando cota: número de candidatos mínimos (poda si 0)
    public boolean solve(Sudoku sudoku) { return solve(sudoku, null); }

    public boolean solve(Sudoku sudoku, SolveTrace trace) {
        if (trace != null) trace.setExplanation("Branch&Bound: usa MRV y poda por 0 candidatos; explora por mejor cota (menos vacías).");
        int[][] start = copyGrid(sudoku.getGrid());
        PriorityQueue<State> pq = new PriorityQueue<>(Comparator.comparingInt(s -> s.priority));
        pq.add(new State(start, heuristic(start)));

        while (!pq.isEmpty()) {
            State cur = pq.poll();
            int[][] grid = cur.grid;
            if (trace != null) {
                trace.incNodesExpanded();
                trace.setFrontierPeak(Math.max(trace.getFrontierPeak(), pq.size()));
            }
            if (isSolved(grid)) {
                sudoku.setGrid(copyGrid(grid));
                return true;
            }
            // Seleccionar celda con menos candidatos (si 0 => poda)
            int[] pos = null; List<Integer> cand = null; int best = Integer.MAX_VALUE;
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (grid[r][c] == 0) {
                        List<Integer> tmp = getCandidates(grid, r, c);
                        int k = tmp.size();
                        if (k == 0) { // Poda por cota
                            cand = null; pos = null; best = 0; break;
                        }
                        if (k < best) { best = k; pos = new int[]{r, c}; cand = tmp; if (best == 1) break; }
                    }
                }
                if (best == 0 || best == 1) break;
            }
            if (best == 0 || pos == null) {
                if (trace != null) trace.addStep("Poda por 0 candidatos");
                continue; // poda
            }

            // Ramificar
            int r = pos[0], c = pos[1];
            for (int num : cand) {
                int[][] next = copyGrid(grid);
                next[r][c] = num;
                int h = heuristic(next);
                if (h == Integer.MAX_VALUE) continue; // inconsistente
                pq.add(new State(next, h));
                if (trace != null) trace.addStep("Ramifica ("+r+","+c+")="+num+" con prioridad "+h);
            }
        }
        return false;
    }

    // Heurística/cota: si alguna celda tiene 0 candidatos => inconsistente; prioridad = celdas vacías
    private int heuristic(int[][] grid) {
        int empties = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] == 0) {
                    empties++;
                    if (getCandidates(grid, r, c).isEmpty()) return Integer.MAX_VALUE;
                }
            }
        }
        return empties;
    }

    private List<Integer> getCandidates(int[][] grid, int row, int col) {
        List<Integer> list = new ArrayList<>(9);
        for (int num = 1; num <= 9; num++) if (isValid(grid, row, col, num)) list.add(num);
        return list;
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

    private boolean isSolved(int[][] grid) {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++) if (grid[r][c] == 0) return false;
        return true;
    }

    private int[][] copyGrid(int[][] g) {
        int[][] out = new int[9][9];
        for (int i = 0; i < 9; i++) System.arraycopy(g[i], 0, out[i], 0, 9);
        return out;
    }

    private static class State {
        final int[][] grid; final int priority;
        State(int[][] g, int p) { this.grid = g; this.priority = p; }
    }
}