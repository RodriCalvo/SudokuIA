package IA.Sudoku.service;

import IA.Sudoku.model.Sudoku;
import IA.Sudoku.repository.SudokuRepository;
import org.springframework.stereotype.Service;

@Service
public class SudokuService {
    private final AlgoritmoBacktracking backtracking;
    private final AlgoritmoDFS dfs;
    private final AlgoritmoBFS bfs;
    private final AlgoritmoGreedy greedy;
    private final AlgoritmoDP dp;
    private final AlgoritmoBranchBound branchBound;
    private final SudokuRepository repository;

    public SudokuService(AlgoritmoBacktracking backtracking,
                         AlgoritmoDFS dfs,
                         AlgoritmoBFS bfs,
                         AlgoritmoGreedy greedy,
                         AlgoritmoDP dp,
                         AlgoritmoBranchBound branchBound,
                         SudokuRepository repository) {
        this.backtracking = backtracking;
        this.dfs = dfs;
        this.bfs = bfs;
        this.greedy = greedy;
        this.dp = dp;
        this.branchBound = branchBound;
        this.repository = repository;
    }

    public boolean solve(Sudoku sudoku, String algoritmo) {
        boolean result;
        switch ((algoritmo == null) ? "backtracking" : algoritmo.toLowerCase()) {
            case "dfs": result = dfs.solve(sudoku); break;
            case "bfs": result = bfs.solve(sudoku); break;
            case "greedy": result = greedy.solve(sudoku); break;
            case "dp": result = dp.solve(sudoku); break;
            case "branchbound": result = branchBound.solve(sudoku); break;
            case "backtracking":
            default: result = backtracking.solve(sudoku); break;
        }
        if (result) repository.save(sudoku);
        return result;
    }

    // Overloads con traza
    public SolveTrace solveWithTrace(Sudoku sudoku, String algoritmo) {
        SolveTrace trace = new SolveTrace();
        boolean result;
        switch ((algoritmo == null) ? "backtracking" : algoritmo.toLowerCase()) {
            case "dfs": result = dfs.solve(sudoku, trace); break;
            case "bfs": result = bfs.solve(sudoku, trace); break;
            case "greedy": result = greedy.solve(sudoku, trace); break;
            case "dp": result = dp.solve(sudoku, trace); break;
            case "branchbound": result = branchBound.solve(sudoku, trace); break;
            case "backtracking":
            default: result = backtracking.solve(sudoku); trace.setExplanation("Backtracking clásico"); break;
        }
        if (result) repository.save(sudoku);
        // Si resolvió, llenar filled/partial
        if (trace.getPartialGrid() == null) trace.setPartialGrid(sudoku.getGrid());
        int filled = 0; for (int r=0;r<9;r++) for (int c=0;c<9;c++) if (sudoku.getGrid()[r][c]!=0) filled++;
        trace.setFilledCells(filled);
        if (!result && trace.getReason() == null) trace.setReason("Algoritmo se detuvo sin encontrar solución completa");
        return trace;
    }
}