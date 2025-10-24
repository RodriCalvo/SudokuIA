package IA.Sudoku.repository;

import IA.Sudoku.model.SudokuNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface Neo4jSudokuNodeRepository extends Neo4jRepository<SudokuNode, Long> {
	Optional<SudokuNode> findByAppId(Long appId);
}
