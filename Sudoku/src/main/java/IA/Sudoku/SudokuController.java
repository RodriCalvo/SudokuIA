package IA.Sudoku;

import IA.Sudoku.model.Sudoku;
import IA.Sudoku.repository.SudokuRepository;
import IA.Sudoku.service.SudokuService;
import IA.Sudoku.service.AlgoritmoSelector;
import IA.Sudoku.service.SudokuGraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/sudoku")
public class SudokuController {

    private final SudokuService sudokuService;
    private final AlgoritmoSelector algoritmoSelector;
    private final SudokuRepository sudokuRepository;
    private final SudokuGraphService sudokuGraphService;

    public SudokuController(SudokuService sudokuService, 
                          AlgoritmoSelector algoritmoSelector,
                          SudokuRepository sudokuRepository,
                          SudokuGraphService sudokuGraphService) {
        this.sudokuService = sudokuService;
        this.algoritmoSelector = algoritmoSelector;
        this.sudokuRepository = sudokuRepository;
        this.sudokuGraphService = sudokuGraphService;
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

    @PostMapping("/solve/auto")
    public ResponseEntity<?> solveAuto(@RequestBody SudokuRequest request) {
        List<List<Integer>> in = request.getGrid();
        if (in == null || in.size() != 9) return ResponseEntity.badRequest().body("Grid inválido");

        int[][] grid = new int[9][9];
        for (int r = 0; r < 9; r++) {
            List<Integer> row = in.get(r);
            if (row == null || row.size() != 9) return ResponseEntity.badRequest().body("Grid inválido");
            for (int c = 0; c < 9; c++) grid[r][c] = row.get(c);
        }

        // Ejecutar TODOS los algoritmos y comparar resultados
        String[] algoritmos = {"backtracking", "dfs", "bfs", "greedy", "dp", "branchbound"};
        List<java.util.Map<String, Object>> allResults = new ArrayList<>();
        
        String bestAlgorithm = null;
        long bestTimeNanos = Long.MAX_VALUE;
        java.util.Map<String, Object> bestResult = null;

        for (String algo : algoritmos) {
            Sudoku modelCopy = new Sudoku(copyGrid(grid));
            
            Runtime rt = Runtime.getRuntime();
            long beforeUsed = rt.totalMemory() - rt.freeMemory();
            long t0 = System.nanoTime();
            
            IA.Sudoku.service.SolveTrace trace = new IA.Sudoku.service.SolveTrace();
            boolean solved = false;
            
            try {
                // Timeout de 30 segundos por algoritmo
                trace = sudokuService.solveWithTrace(modelCopy, algo);
                solved = (trace.getFilledCells() == 81);
            } catch (OutOfMemoryError e) {
                trace.setReason("Algoritmo consumió demasiada memoria (OutOfMemoryError)");
                trace.setExplanation("Memoria insuficiente para este puzzle con " + algo);
            } catch (Exception e) {
                trace.setReason("Error durante ejecución: " + e.getMessage());
                trace.setExplanation("Excepción en " + algo);
            }
            
            long t1 = System.nanoTime();
            long afterUsed = rt.totalMemory() - rt.freeMemory();
            long elapsedNanos = (t1 - t0);
            double elapsedMs = elapsedNanos / 1_000_000.0; // Mantener decimales
            double memMB = Math.max(0, afterUsed - beforeUsed) / (1024.0 * 1024.0);

            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("algorithm", algo);
            result.put("solved", solved);
            result.put("runtimeNanos", elapsedNanos);
            result.put("runtimeMs", Math.round(elapsedMs * 1000.0) / 1000.0); // 3 decimales
            result.put("memoryMB", Math.round(memMB * 100.0) / 100.0);
            result.put("complexity", approxComplexity(algo));
            result.put("nodesExpanded", trace.getNodesExpanded());
            result.put("maxDepth", trace.getMaxDepth());
            result.put("frontierPeak", trace.getFrontierPeak());
            result.put("filledCells", trace.getFilledCells());
            result.put("explanation", trace.getExplanation());
            
            if (solved) {
                result.put("status", "✅ Resuelto");
                // Guardar grid resuelto
                int[][] solvedGrid = modelCopy.getGrid();
                List<List<Integer>> out = new ArrayList<>();
                for (int r = 0; r < 9; r++) {
                    List<Integer> row = new ArrayList<>();
                    for (int c = 0; c < 9; c++) row.add(solvedGrid[r][c]);
                    out.add(row);
                }
                result.put("grid", out);
                
                // Actualizar mejor si es más rápido
                if (elapsedNanos < bestTimeNanos) {
                    bestTimeNanos = elapsedNanos;
                    bestAlgorithm = algo;
                    bestResult = result;
                }
            } else {
                result.put("status", "❌ No completado");
                result.put("reason", trace.getReason());
            }
            
            allResults.add(result);
        }

        // Si ninguno resolvió
        if (bestAlgorithm == null) {
            java.util.Map<String, Object> errorBody = new java.util.LinkedHashMap<>();
            errorBody.put("message", "Ningún algoritmo pudo resolver el Sudoku");
            errorBody.put("allResults", allResults);
            return ResponseEntity.status(400).body(errorBody);
        }

        // Analizar por qué cada algoritmo no fue elegido
        AlgoritmoSelector.SelectionResult selection = algoritmoSelector.selectBestAlgorithm(grid);
        java.util.Map<String, String> reasons = new java.util.LinkedHashMap<>();
        
        for (java.util.Map<String, Object> result : allResults) {
            String algo = (String) result.get("algorithm");
            if (!algo.equals(bestAlgorithm)) {
                if (!(Boolean) result.get("solved")) {
                    reasons.put(algo, "No pudo resolver el puzzle (incompleto)");
                } else {
                    double timeMs = (Double) result.get("runtimeMs");
                    double bestMs = bestTimeNanos / 1_000_000.0;
                    reasons.put(algo, String.format("Más lento (%.3f ms vs %.3f ms del mejor)", timeMs, bestMs));
                }
            }
        }

        // Respuesta final con toda la información
        double bestTimeMs = bestTimeNanos / 1_000_000.0;
        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("solved", true);
        response.put("grid", bestResult.get("grid"));
        response.put("selectedAlgorithm", bestAlgorithm);
        response.put("selectionReason", String.format(
            "Elegido por ser el más rápido: %.3f ms. %s",
            bestTimeMs,
            selection.getReason()
        ));
        response.put("rejectedAlgorithms", reasons);
        response.put("allResults", allResults);
        response.put("analysis", createAnalysisMap(selection));
        response.put("runtimeMillis", Math.round(bestTimeMs * 1000.0) / 1000.0);
        response.put("memoryMB", bestResult.get("memoryMB"));
        response.put("complexity", bestResult.get("complexity"));
        response.put("explanation", bestResult.get("explanation"));
        response.put("nodesExpanded", bestResult.get("nodesExpanded"));
        response.put("maxDepth", bestResult.get("maxDepth"));
        response.put("frontierPeak", bestResult.get("frontierPeak"));
        response.put("filledCells", bestResult.get("filledCells"));

        // 🔥 Guardar en Neo4j como GRAFO (81 nodos Cell + relaciones)
        try {
            int[][] solvedGrid = convertToGridArray(bestResult.get("grid"));
            String difficulty = determineDifficulty(selection.getEmptyCells());
            
            Sudoku saved = sudokuGraphService.saveSudokuAsGraph(
                grid,                                      // Grid inicial
                solvedGrid,                                // Grid resuelto
                bestAlgorithm,                             // Algoritmo
                bestTimeMs,                                // Runtime
                (Double) bestResult.get("memoryMB"),     // Memoria
                difficulty,                                // Dificultad
                selection.getEmptyCells()                  // Celdas vacías
            );
            
            response.put("savedId", saved.getId());
            response.put("graphInfo", "Guardado como grafo: 81 nodos Cell + 810 aristas de restricciones");
        } catch (Exception e) {
            // Si falla Neo4j, no bloqueamos la respuesta
            response.put("saveError", "No se pudo guardar en Neo4j: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
    
    private int[][] convertToGridArray(Object gridObj) {
        @SuppressWarnings("unchecked")
        List<List<Integer>> gridList = (List<List<Integer>>) gridObj;
        int[][] arr = new int[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                arr[r][c] = gridList.get(r).get(c);
            }
        }
        return arr;
    }
    
    private String determineDifficulty(int emptyCells) {
        if (emptyCells < 30) return "Fácil";
        if (emptyCells < 45) return "Medio";
        return "Difícil";
    }
    
    // 📊 Endpoints de consulta Neo4j
    
    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        try {
            List<Sudoku> all = sudokuRepository.findBySolvedTrue();
            return ResponseEntity.ok(all);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("Neo4j no disponible: " + e.getMessage());
        }
    }
    
    @GetMapping("/history/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return sudokuRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(503).body("Neo4j no disponible: " + e.getMessage());
        }
    }
    
    @GetMapping("/stats/algorithm/{name}")
    public ResponseEntity<?> getAlgorithmStats(@PathVariable String name) {
        try {
            List<Sudoku> results = sudokuRepository.findByAlgorithm(name);
            if (results.isEmpty()) {
                return ResponseEntity.ok(java.util.Map.of(
                    "algorithm", name,
                    "total", 0,
                    "message", "No hay registros para este algoritmo"
                ));
            }
            
            double avgTime = results.stream()
                .mapToDouble(s -> s.getRuntimeMs() != null ? s.getRuntimeMs() : 0.0)
                .average()
                .orElse(0.0);
            
            double avgMemory = results.stream()
                .mapToDouble(s -> s.getMemoryMB() != null ? s.getMemoryMB() : 0.0)
                .average()
                .orElse(0.0);
            
            java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
            stats.put("algorithm", name);
            stats.put("total", results.size());
            stats.put("avgRuntimeMs", Math.round(avgTime * 1000.0) / 1000.0);
            stats.put("avgMemoryMB", Math.round(avgMemory * 100.0) / 100.0);
            stats.put("fastest", results.stream()
                .min((a, b) -> Double.compare(
                    a.getRuntimeMs() != null ? a.getRuntimeMs() : Double.MAX_VALUE,
                    b.getRuntimeMs() != null ? b.getRuntimeMs() : Double.MAX_VALUE
                ))
                .map(Sudoku::getRuntimeMs)
                .orElse(null)
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("Neo4j no disponible: " + e.getMessage());
        }
    }
    
    @GetMapping("/stats/fastest")
    public ResponseEntity<?> getFastest(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Sudoku> fastest = sudokuRepository.findFastestSolved(limit);
            return ResponseEntity.ok(fastest);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("Neo4j no disponible: " + e.getMessage());
        }
    }
    
    @GetMapping("/stats/difficulty/{level}")
    public ResponseEntity<?> getByDifficulty(@PathVariable String level) {
        try {
            List<Sudoku> results = sudokuRepository.findByDifficulty(level);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("Neo4j no disponible: " + e.getMessage());
        }
    }
    
    // 📊 Endpoints para consultar el GRAFO de celdas
    
    @GetMapping("/graph/{sudokuId}")
    public ResponseEntity<?> getSudokuGraph(@PathVariable Long sudokuId) {
        try {
            java.util.Map<String, Object> stats = sudokuGraphService.getSudokuGraphStats(sudokuId);
            int[][] grid = sudokuGraphService.getSudokuGridFromGraph(sudokuId);
            
            stats.put("grid", grid);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("Neo4j no disponible: " + e.getMessage());
        }
    }

    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, 9);
        }
        return copy;
    }

    private java.util.Map<String, Object> createAnalysisMap(AlgoritmoSelector.SelectionResult selection) {
        java.util.Map<String, Object> analysis = new java.util.LinkedHashMap<>();
        analysis.put("emptyCells", selection.getEmptyCells());
        analysis.put("fillPercentage", Math.round(selection.getFillPercentage() * 100.0) / 100.0);
        analysis.put("constraintLevel", selection.getConstraintLevel());
        return analysis;
    }
}
