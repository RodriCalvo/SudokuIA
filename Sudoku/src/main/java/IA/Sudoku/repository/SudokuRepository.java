package IA.Sudoku.repository;

import IA.Sudoku.model.Sudoku;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SudokuRepository {
    private final Map<Long, Sudoku> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Long save(Sudoku sudoku) {
        Long id = idGen.getAndIncrement();
        store.put(id, sudoku);
        return id;
    }

    public Sudoku findById(Long id) {
        return store.get(id);
    }
}