package IA.Sudoku;

import IA.Sudoku.model.Sudoku;
import IA.Sudoku.service.SudokuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/sudoku")
public class SudokuController {

    private final SudokuService sudokuService;

    public SudokuController(SudokuService sudokuService) {
        this.sudokuService = sudokuService;
    }

    @PostMapping("/solve")
    public ResponseEntity<?> solve(@RequestBody SudokuRequest request,
                                   @RequestParam(value = "algoritmo", required = false) String algoritmo) {
        List<List<Integer>> in = request.getGrid();
        if (in == null || in.size() != 9) return ResponseEntity.badRequest().body("Grid inválido");

        int[][] grid = new int[9][9];
        for (int r = 0; r < 9; r++) {
            List<Integer> row = in.get(r);
            if (row == null || row.size() != 9) return ResponseEntity.badRequest().body("Grid inválido");
            for (int c = 0; c < 9; c++) grid[r][c] = row.get(c);
        }

    Sudoku model = new Sudoku(grid);

    // Medición de tiempo y memoria (aproximada)
    Runtime rt = Runtime.getRuntime();
    long beforeUsed = rt.totalMemory() - rt.freeMemory();
    long t0 = System.nanoTime();

    // Resolver con traza
    IA.Sudoku.service.SolveTrace trace = sudokuService.solveWithTrace(model, algoritmo);
    boolean solved = (trace.getFilledCells() == 81);

    long t1 = System.nanoTime();
    long afterUsed = rt.totalMemory() - rt.freeMemory();
    long elapsedMs = (t1 - t0) / 1_000_000L;
    double memMB = Math.max(0, afterUsed - beforeUsed) / (1024.0 * 1024.0);

        if (!solved) {
            String algoName = algoritmo == null ? "backtracking" : algoritmo.toLowerCase();
            java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("message", "No se pudo resolver con el algoritmo solicitado");
            body.put("algoritmo", algoName);
            body.put("runtimeMillis", elapsedMs);
            body.put("memoryMB", Math.round(memMB * 100.0) / 100.0);
            body.put("complexity", approxComplexity(algoName));
            body.put("explanation", trace.getExplanation());
            body.put("reason", trace.getReason());
            body.put("steps", trace.getSteps());
            body.put("nodesExpanded", trace.getNodesExpanded());
            body.put("maxDepth", trace.getMaxDepth());
            body.put("frontierPeak", trace.getFrontierPeak());
            body.put("filledCells", trace.getFilledCells());
            body.put("partialGrid", trace.getPartialGrid());
            return ResponseEntity.status(400).body(body);
        }

        // convertir int[][] a List<List<Integer>>
        int[][] solvedGrid = model.getGrid();
        List<List<Integer>> out = new ArrayList<>();
        for (int r = 0; r < 9; r++) {
            List<Integer> row = new ArrayList<>();
            for (int c = 0; c < 9; c++) row.add(solvedGrid[r][c]);
            out.add(row);
        }

        String algoName = algoritmo == null ? "backtracking" : algoritmo.toLowerCase();
        SudokuResponse resp = new SudokuResponse();
        resp.setAlgoritmo(algoName);
        resp.setGrid(out);
        resp.setRuntimeMillis(elapsedMs);
        resp.setMemoryMB(Math.round(memMB * 100.0) / 100.0);
        resp.setComplexity(approxComplexity(algoName));
        resp.setExplanation(trace.getExplanation());
        resp.setReason(trace.getReason());
        resp.setSteps(trace.getSteps());
        resp.setNodesExpanded(trace.getNodesExpanded());
        resp.setMaxDepth(trace.getMaxDepth());
        resp.setFrontierPeak(trace.getFrontierPeak());
        resp.setFilledCells(trace.getFilledCells());
        return ResponseEntity.ok(resp);
    }

    private String approxComplexity(String algoritmo) {
        if (algoritmo == null) return "Exponencial ~ O(b^d)";
        switch (algoritmo.toLowerCase()) {
            case "bfs": return "Exponencial en tiempo y memoria ~ O(b^d), memoria O(b^d)";
            case "dfs": return "Exponencial en tiempo ~ O(b^d), memoria O(d)";
            case "greedy": return "Polinómica por iteraciones locales; incompleto";
            case "dp": return "Polinómica por propagación (bitmasks); incompleto";
            case "branchbound": return "Exponencial amortiguado por poda; memoria depende de la frontera";
            case "backtracking":
            default: return "Exponencial ~ O(b^d), memoria O(d)";
        }
    }
}
