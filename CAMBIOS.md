# 🤖 Selección Automática de Algoritmo - SudokuIA

## 📋 ¿Qué se agregó?

Se implementó un sistema inteligente que **selecciona automáticamente el mejor algoritmo** para resolver cada Sudoku basándose en su complejidad y características.

## ✨ Características Nuevas

### 1️⃣ **Nuevo Servicio: `AlgoritmoSelector`**

Ubicación: `src/main/java/IA/Sudoku/service/AlgoritmoSelector.java`

**Analiza el Sudoku evaluando:**
- Número de celdas vacías
- Porcentaje de completitud
- Nivel de restricción (0-10)
- Número de candidatos por celda

**Selecciona el algoritmo óptimo basándose en:**
- **Greedy**: Puzzles casi completos (≤10 vacías) con alta restricción
- **DP**: Moderadamente llenos (≤20 vacías) con buena restricción
- **Backtracking**: Complejidad media (≤35 vacías)
- **Branch & Bound**: Alta/muy alta complejidad (>35 vacías)

### 2️⃣ **Nuevo Endpoint: `/api/sudoku/solve/auto`**

```http
POST http://localhost:8080/api/sudoku/solve/auto
Content-Type: application/json

{
  "grid": [[9x9 matriz]]
}
```

**Respuesta incluye:**
- `selectedAlgorithm`: Algoritmo elegido
- `selectionReason`: Explicación detallada de por qué se eligió
- `rejectedAlgorithms`: Mapa con cada algoritmo descartado y su razón
- `analysis`: Métricas del puzzle (celdas vacías, %, nivel de restricción)
- Todos los datos de resolución normales (tiempo, memoria, pasos, etc.)

### 3️⃣ **Interfaz Web Actualizada**

El archivo `sudoku-web.html` ahora incluye:
- Opción "🤖 AUTO - Selección Automática" en el selector
- Visualización detallada de:
  - Algoritmo seleccionado con razón
  - Análisis del puzzle
  - Algoritmos descartados con explicaciones
- Interfaz mejorada con colores distintivos

### 4️⃣ **Script PowerShell Mejorado**

El archivo `test-sudoku.ps1` ahora muestra:
- Opción 0 para selección automática
- Formato visual mejorado
- Sección dedicada a mostrar la selección automática
- Tabla con algoritmos descartados

## 🎯 Cómo Usar

### Opción A: Interfaz Web (Recomendado)

1. Asegúrate de que el servidor esté corriendo:
   ```powershell
   cd "c:\Users\tomas\Desktop\tp progra 3\SudokuIA\Sudoku"
   .\mvnw.cmd spring-boot:run
   ```

2. Abre en tu navegador:
   ```
   c:\Users\tomas\Desktop\tp progra 3\SudokuIA\sudoku-web.html
   ```

3. Selecciona "🤖 AUTO - Selección Automática" y presiona "Resolver"

### Opción B: Script PowerShell

```powershell
cd "c:\Users\tomas\Desktop\tp progra 3\SudokuIA"
.\test-sudoku.ps1
```

Selecciona opción `0` para selección automática.

### Opción C: API Directa

```powershell
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

Invoke-RestMethod -Uri "http://localhost:8080/api/sudoku/solve/auto" -Method POST -Body $body -ContentType "application/json"
```

## 📊 Ejemplo de Respuesta

```json
{
  "solved": true,
  "grid": [[...solución...]],
  "selectedAlgorithm": "backtracking",
  "selectionReason": "Sudoku con 52.0% completado (complejidad media). Backtracking es el balance óptimo...",
  "rejectedAlgorithms": {
    "greedy": "Incompleto - se quedaría sin movimientos en celdas ambiguas",
    "dp": "Incompleto - la propagación sola no garantiza resolución",
    "dfs": "Similar a backtracking pero con overhead de copiar estados completos",
    "bfs": "Consumo excesivo de memoria O(b^d) sin beneficio sobre backtracking",
    "branchbound": "Overhead de cola de prioridad sin mejora significativa"
  },
  "analysis": {
    "emptyCells": 43,
    "fillPercentage": 47.0,
    "constraintLevel": 5
  },
  "runtimeMillis": 12,
  "memoryMB": 0.15,
  "complexity": "Exponencial ~ O(b^d), memoria O(d)",
  ...
}
```

## 🔍 Lógica de Selección

| Celdas Vacías | Restricción | Algoritmo Seleccionado | Razón |
|--------------|-------------|----------------------|-------|
| ≤10 | Alta (≥7) | **Greedy** | Mayoría de celdas tienen única opción |
| ≤20 | Buena (≥6) | **DP** | Propagación eficiente sin búsqueda |
| ≤35 | Cualquiera | **Backtracking** | Balance óptimo completitud/eficiencia |
| 36-50 | Cualquiera | **Branch & Bound** | Poda necesaria para complejidad alta |
| >50 | Cualquiera | **Branch & Bound** | Crítico para evitar explosión combinatoria |

## 📁 Archivos Modificados

### Nuevos archivos:
- `src/main/java/IA/Sudoku/service/AlgoritmoSelector.java` ✨

### Archivos modificados:
- `src/main/java/IA/Sudoku/SudokuController.java` (nuevo endpoint `/solve/auto`)
- `sudoku-web.html` (opción AUTO y visualización mejorada)
- `test-sudoku.ps1` (opción 0 y formato mejorado)

### Archivos originales sin cambios:
- Todos los algoritmos originales
- Modelos y servicios base
- Configuraciones

## 🚀 Ventajas

✅ **Eficiencia**: Cada puzzle se resuelve con el algoritmo más apropiado  
✅ **Educativo**: Explica por qué cada algoritmo es mejor/peor para cada caso  
✅ **Transparente**: Muestra análisis detallado del puzzle  
✅ **Compatible**: El endpoint original `/solve?algoritmo=X` sigue funcionando  

## 📝 Notas

- El proyecto original está **100% intacto**
- La selección automática es **opcional** (puedes seguir usando algoritmos específicos)
- Los archivos auxiliares (HTML, PS1) están **fuera** de la carpeta `Sudoku/`
