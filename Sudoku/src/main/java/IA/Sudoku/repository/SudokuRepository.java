package IA.Sudoku.repository;

import IA.Sudoku.model.Sudoku;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SudokuRepository extends Neo4jRepository<Sudoku, Long> {
    
    // Buscar por algoritmo
    List<Sudoku> findByAlgorithm(String algorithm);
    
    // Buscar solo resueltos
    List<Sudoku> findBySolvedTrue();
    
    // Buscar por dificultad
    List<Sudoku> findByDifficulty(String difficulty);
    
    // Buscar los más rápidos
    @Query("MATCH (s:Sudoku) WHERE s.solved = true RETURN s ORDER BY s.runtimeMs ASC LIMIT $limit")
    List<Sudoku> findFastestSolved(int limit);
    
    // Estadísticas por algoritmo
    @Query("MATCH (s:Sudoku) WHERE s.algorithm = $algorithm AND s.solved = true " +
           "RETURN avg(s.runtimeMs) as avgTime, count(s) as total")
    Object getAlgorithmStats(String algorithm);
}
