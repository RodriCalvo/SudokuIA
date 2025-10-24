package IA.Sudoku.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.List;

@Node("Sudoku")
public class SudokuNode {
    @Id @GeneratedValue
    private Long id;

    // application-side id so we can map in-memory id -> graph node
    @Property("appId")
    private Long appId;

    @Relationship(type = "HAS_CELL", direction = Relationship.Direction.OUTGOING)
    private List<Celda> cells;

    public SudokuNode() {}

    public SudokuNode(Long appId, List<Celda> cells) {
        this.appId = appId;
        this.cells = cells;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAppId() { return appId; }
    public void setAppId(Long appId) { this.appId = appId; }

    public List<Celda> getCells() { return cells; }
    public void setCells(List<Celda> cells) { this.cells = cells; }
}
