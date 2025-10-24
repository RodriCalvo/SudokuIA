package IA.Sudoku;

import java.util.List;

public class SudokuResponse {
    private List<List<Integer>> grid;
    private String algoritmo;
    private long runtimeMillis;
    private double memoryMB;
    private String complexity;
    private List<String> steps;
    private String explanation;
    private String reason;
    private Integer nodesExpanded;
    private Integer maxDepth;
    private Integer frontierPeak;
    private Integer filledCells;

    public List<List<Integer>> getGrid() {
        return grid;
    }

    public void setGrid(List<List<Integer>> grid) {
        this.grid = grid;
    }

    public String getAlgoritmo() {
        return algoritmo;
    }

    public void setAlgoritmo(String algoritmo) {
        this.algoritmo = algoritmo;
    }

    public long getRuntimeMillis() {
        return runtimeMillis;
    }

    public void setRuntimeMillis(long runtimeMillis) {
        this.runtimeMillis = runtimeMillis;
    }

    public double getMemoryMB() {
        return memoryMB;
    }

    public void setMemoryMB(double memoryMB) {
        this.memoryMB = memoryMB;
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getNodesExpanded() { return nodesExpanded; }
    public void setNodesExpanded(Integer nodesExpanded) { this.nodesExpanded = nodesExpanded; }
    public Integer getMaxDepth() { return maxDepth; }
    public void setMaxDepth(Integer maxDepth) { this.maxDepth = maxDepth; }
    public Integer getFrontierPeak() { return frontierPeak; }
    public void setFrontierPeak(Integer frontierPeak) { this.frontierPeak = frontierPeak; }
    public Integer getFilledCells() { return filledCells; }
    public void setFilledCells(Integer filledCells) { this.filledCells = filledCells; }
}
