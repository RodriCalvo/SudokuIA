package IA.Sudoku.repository;

import IA.Sudoku.model.Celda;
import IA.Sudoku.model.Sudoku;
import IA.Sudoku.model.SudokuNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SudokuRepository {
    private final Map<Long, Sudoku> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    // Optional Neo4j repository - will be null if SDN is not configured
    @Autowired(required = false)
    private Neo4jSudokuNodeRepository neoRepo;

    public Long save(Sudoku sudoku) {
        Long id = idGen.getAndIncrement();
        store.put(id, sudoku);

        // also persist to Neo4j if available (store Sudoku -> Celda relationships)
        if (neoRepo != null) {
            try {
                int[][] grid = sudoku.getGrid();
                List<Celda> cells = new ArrayList<>();
                for (int r = 0; r < grid.length; r++) {
                    for (int c = 0; c < grid[r].length; c++) {
                        int v = grid[r][c];
                        Celda cell = new Celda(r, c, v);
                        cells.add(cell);
                    }
                }
                SudokuNode node = new SudokuNode(id, cells);
                neoRepo.save(node);
            } catch (Exception e) {
                // ignore Neo4j persistence errors to avoid breaking solve flow
                e.printStackTrace();
            }
        }

        return id;
    }

    public Sudoku findById(Long id) {
        Sudoku s = store.get(id);
        if (s != null) return s;

        // fallback: try to read from Neo4j if available
        if (neoRepo != null) {
            return neoRepo.findByAppId(id).map(node -> {
                int[][] grid = new int[9][9];
                List<Celda> cells = node.getCells();
                if (cells != null) {
                    for (Celda cell : cells) {
                        int r = cell.getFila();
                        int c = cell.getColumna();
                        int v = cell.getValor();
                        if (r >= 0 && r < 9 && c >= 0 && c < 9) grid[r][c] = v;
                    }
                }
                return new Sudoku(grid);
            }).orElse(null);
        }

        return null;
    }
}