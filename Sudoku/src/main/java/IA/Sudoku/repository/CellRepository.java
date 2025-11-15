package IA.Sudoku.repository;

import IA.Sudoku.model.Cell;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CellRepository extends Neo4jRepository<Cell, Long> {
    
    // Buscar todas las celdas de un Sudoku
    List<Cell> findBySudokuId(Long sudokuId);
    
    // Buscar celdas por fila
    @Query("MATCH (c:Cell) WHERE c.sudokuId = $sudokuId AND c.row = $row RETURN c ORDER BY c.col")
    List<Cell> findByRow(Long sudokuId, Integer row);
    
    // Buscar celdas por columna
    @Query("MATCH (c:Cell) WHERE c.sudokuId = $sudokuId AND c.col = $col RETURN c ORDER BY c.row")
    List<Cell> findByCol(Long sudokuId, Integer col);
    
    // Buscar celdas por región 3x3
    @Query("MATCH (c:Cell) WHERE c.sudokuId = $sudokuId AND c.region = $region RETURN c")
    List<Cell> findByRegion(Long sudokuId, Integer region);
    
    // Buscar vecinos de una celda (mismo row, col o región)
    @Query("MATCH (c:Cell)-[:SAME_ROW|SAME_COL|SAME_REGION]->(neighbor:Cell) " +
           "WHERE c.sudokuId = $sudokuId AND c.row = $row AND c.col = $col " +
           "RETURN DISTINCT neighbor")
    List<Cell> findNeighbors(Long sudokuId, Integer row, Integer col);
    
    // Validar si un valor está disponible para una celda
    @Query("MATCH (c:Cell)-[:SAME_ROW|SAME_COL|SAME_REGION]->(neighbor:Cell) " +
           "WHERE c.sudokuId = $sudokuId AND c.row = $row AND c.col = $col " +
           "AND neighbor.value = $value " +
           "RETURN count(neighbor) > 0")
    Boolean isValueConflicting(Long sudokuId, Integer row, Integer col, Integer value);
}
