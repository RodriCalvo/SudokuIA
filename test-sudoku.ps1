# Script para probar el solucionador de Sudoku
# Asegúrate de que el servidor esté corriendo primero

Write-Host "=== SOLUCIONADOR DE SUDOKU - PRUEBA ===" -ForegroundColor Cyan
Write-Host ""

# Sudoku de ejemplo (0 = celda vacía)
$body = @{
    grid = @(
        @(5,3,0,0,7,0,0,0,0),
        @(6,0,0,1,9,5,0,0,0),
        @(0,9,8,0,0,0,0,6,0),
        @(8,0,0,0,6,0,0,0,3),
        @(4,0,0,8,0,3,0,0,1),
        @(7,0,0,0,2,0,0,0,6),
        @(0,6,0,0,0,0,2,8,0),
        @(0,0,0,4,1,9,0,0,5),
        @(0,0,0,0,8,0,0,7,9)
    )
} | ConvertTo-Json

Write-Host "Sudoku original:" -ForegroundColor Yellow
Write-Host "5 3 _ | _ 7 _ | _ _ _"
Write-Host "6 _ _ | 1 9 5 | _ _ _"
Write-Host "_ 9 8 | _ _ _ | _ 6 _"
Write-Host "------+-------+------"
Write-Host "8 _ _ | _ 6 _ | _ _ 3"
Write-Host "4 _ _ | 8 _ 3 | _ _ 1"
Write-Host "7 _ _ | _ 2 _ | _ _ 6"
Write-Host "------+-------+------"
Write-Host "_ 6 _ | _ _ _ | 2 8 _"
Write-Host "_ _ _ | 4 1 9 | _ _ 5"
Write-Host "_ _ _ | _ 8 _ | _ 7 9"
Write-Host ""

# Menú de algoritmos
Write-Host "Algoritmos disponibles:" -ForegroundColor Green
Write-Host "0. AUTO - Selección automática (RECOMENDADO)" -ForegroundColor Cyan
Write-Host "1. backtracking (rápido, recursivo)"
Write-Host "2. dfs (profundidad)"
Write-Host "3. bfs (anchura - puede consumir mucha memoria)"
Write-Host "4. greedy (codicioso - puede no resolver)"
Write-Host "5. dp (programación dinámica - puede no resolver)"
Write-Host "6. branchbound (ramificación y poda)"
Write-Host ""

$opcion = Read-Host "Elige un algoritmo (0-6)"

$algoritmo = switch ($opcion) {
    "0" { "auto" }
    "1" { "backtracking" }
    "2" { "dfs" }
    "3" { "bfs" }
    "4" { "greedy" }
    "5" { "dp" }
    "6" { "branchbound" }
    default { "auto" }
}

Write-Host ""
Write-Host "Resolviendo con: $algoritmo..." -ForegroundColor Cyan
Write-Host ""

try {
    if ($algoritmo -eq "auto") {
        $url = "http://localhost:8080/api/sudoku/solve/auto"
    } else {
        $url = "http://localhost:8080/api/sudoku/solve?algoritmo=$algoritmo"
    }
    
    $response = Invoke-RestMethod -Uri $url -Method POST -Body $body -ContentType "application/json"
    
    Write-Host "✓ ¡RESUELTO!" -ForegroundColor Green
    Write-Host ""
    
    # Mostrar información de selección automática
    if ($response.selectedAlgorithm) {
        Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
        Write-Host "║  🤖 SELECCIÓN AUTOMÁTICA DE ALGORITMO                         ║" -ForegroundColor Cyan
        Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Algoritmo seleccionado: " -NoNewline -ForegroundColor Yellow
        Write-Host $response.selectedAlgorithm.ToUpper() -ForegroundColor Green
        Write-Host ""
        Write-Host "Razón:" -ForegroundColor Yellow
        Write-Host "  $($response.selectionReason)" -ForegroundColor White
        Write-Host ""
        
        if ($response.analysis) {
            Write-Host "Análisis del puzzle:" -ForegroundColor Yellow
            Write-Host "  • Celdas vacías: $($response.analysis.emptyCells)" -ForegroundColor White
            Write-Host "  • Completado: $($response.analysis.fillPercentage)%" -ForegroundColor White
            Write-Host "  • Nivel de restricción: $($response.analysis.constraintLevel)/10" -ForegroundColor White
            Write-Host ""
        }
        
        if ($response.rejectedAlgorithms) {
            Write-Host "Algoritmos descartados:" -ForegroundColor Red
            foreach ($algo in $response.rejectedAlgorithms.PSObject.Properties) {
                Write-Host "  ❌ $($algo.Name): " -NoNewline -ForegroundColor Red
                Write-Host $algo.Value -ForegroundColor White
            }
            Write-Host ""
        }
        
        Write-Host "════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
        Write-Host ""
    }
    
    Write-Host "Algoritmo usado: $($response.selectedAlgorithm ?? $response.algoritmo)" -ForegroundColor Yellow
    Write-Host "Tiempo: $($response.runtimeMillis) ms" -ForegroundColor Yellow
    Write-Host "Memoria: $($response.memoryMB) MB" -ForegroundColor Yellow
    Write-Host "Complejidad: $($response.complexity)" -ForegroundColor Yellow
    Write-Host "Nodos expandidos: $($response.nodesExpanded)" -ForegroundColor Yellow
    Write-Host "Profundidad máxima: $($response.maxDepth)" -ForegroundColor Yellow
    Write-Host ""
    
    Write-Host "Solución:" -ForegroundColor Green
    $grid = $response.grid
    for ($i = 0; $i -lt 9; $i++) {
        $row = ""
        for ($j = 0; $j -lt 9; $j++) {
            $row += $grid[$i][$j]
            if ($j -eq 2 -or $j -eq 5) { $row += " | " }
            elseif ($j -lt 8) { $row += " " }
        }
        Write-Host $row
        if ($i -eq 2 -or $i -eq 5) {
            Write-Host "------+-------+------"
        }
    }
    
    if ($response.steps -and $response.steps.Count -gt 0) {
        Write-Host ""
        Write-Host "Primeros pasos del algoritmo:" -ForegroundColor Cyan
        $maxSteps = [Math]::Min(10, $response.steps.Count)
        for ($i = 0; $i -lt $maxSteps; $i++) {
            Write-Host "  $($i+1). $($response.steps[$i])"
        }
        if ($response.steps.Count -gt 10) {
            Write-Host "  ... y $($response.steps.Count - 10) pasos más"
        }
    }
    
} catch {
    Write-Host "✗ ERROR" -ForegroundColor Red
    Write-Host ""
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Código de estado: $statusCode" -ForegroundColor Red
        
        # Leer el cuerpo de la respuesta de error
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd() | ConvertFrom-Json
        
        Write-Host "Mensaje: $($responseBody.message)" -ForegroundColor Yellow
        Write-Host "Algoritmo: $($responseBody.algoritmo)" -ForegroundColor Yellow
        Write-Host "Razón: $($responseBody.reason)" -ForegroundColor Yellow
        Write-Host "Celdas llenadas: $($responseBody.filledCells) / 81" -ForegroundColor Yellow
        
        if ($responseBody.partialGrid) {
            Write-Host ""
            Write-Host "Estado parcial alcanzado:" -ForegroundColor Yellow
            $grid = $responseBody.partialGrid
            for ($i = 0; $i -lt 9; $i++) {
                $row = ""
                for ($j = 0; $j -lt 9; $j++) {
                    if ($grid[$i][$j] -eq 0) {
                        $row += "_"
                    } else {
                        $row += $grid[$i][$j]
                    }
                    if ($j -eq 2 -or $j -eq 5) { $row += " | " }
                    elseif ($j -lt 8) { $row += " " }
                }
                Write-Host $row
                if ($i -eq 2 -or $i -eq 5) {
                    Write-Host "------+-------+------"
                }
            }
        }
    } else {
        Write-Host $_.Exception.Message -ForegroundColor Red
        Write-Host ""
        Write-Host "¿Está el servidor corriendo?" -ForegroundColor Yellow
        Write-Host "Ejecuta: .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Presiona Enter para salir..."
Read-Host
