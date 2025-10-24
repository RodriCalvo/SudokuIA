package IA.Sudoku.service;

import java.util.ArrayList;
import java.util.List;

public class SolveTrace {
    private final List<String> steps = new ArrayList<>();
    private int stepLimit = 200;
    private String explanation;
    private String reason; // por qué se detuvo si no resolvió
    private int nodesExpanded;
    private int maxDepth;
    private int frontierPeak;
    private int filledCells;
    private int[][] partialGrid;

    public void setStepLimit(int limit) { this.stepLimit = Math.max(0, limit); }
    public void addStep(String s) {
        if (steps.size() < stepLimit) steps.add(s);
    }

    public List<String> getSteps() { return steps; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public int getNodesExpanded() { return nodesExpanded; }
    public void incNodesExpanded() { this.nodesExpanded++; }
    public void addNodesExpanded(int n) { this.nodesExpanded += n; }
    public int getMaxDepth() { return maxDepth; }
    public void setMaxDepth(int maxDepth) { this.maxDepth = Math.max(this.maxDepth, maxDepth); }
    public int getFrontierPeak() { return frontierPeak; }
    public void setFrontierPeak(int frontierPeak) { this.frontierPeak = Math.max(this.frontierPeak, frontierPeak); }
    public int getFilledCells() { return filledCells; }
    public void setFilledCells(int filledCells) { this.filledCells = filledCells; }
    public int[][] getPartialGrid() { return partialGrid; }
    public void setPartialGrid(int[][] partialGrid) { this.partialGrid = partialGrid; }
}
