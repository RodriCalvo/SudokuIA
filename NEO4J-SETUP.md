# 🔥 Configuración de Neo4j para Sudoku

## 📥 Instalación

### Opción 1: Neo4j Desktop (Recomendado para desarrollo)
1. Descarga Neo4j Desktop: https://neo4j.com/download/
2. Instala y abre Neo4j Desktop
3. Crea un nuevo proyecto "Sudoku"
4. Crea una base de datos local:
   - **Name**: sudoku-db
   - **Password**: password (o la que prefieras)
5. Inicia la base de datos (botón Start)
6. Verifica que corra en `bolt://localhost:7687`

### Opción 2: Docker (Más rápido)
```powershell
docker run -d `
  --name neo4j-sudoku `
  -p 7474:7474 `
  -p 7687:7687 `
  -e NEO4J_AUTH=neo4j/password `
  neo4j:latest
```

## ⚙️ Configuración en Spring Boot

Ya está configurado en `application.properties`:
```properties
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=password
```

⚠️ **IMPORTANTE**: Si usaste otra contraseña, cámbiala en `application.properties`

## 🎯 Uso

### Verificar conexión
Accede a Neo4j Browser: http://localhost:7474
- Usuario: `neo4j`
- Password: `password`

### Probar la aplicación
1. Reinicia el servidor Spring Boot (se detendrá si Neo4j no está disponible)
2. Resuelve un Sudoku con el endpoint `/api/sudoku/solve/auto`
3. Verifica que se guardó en Neo4j:

```cypher
MATCH (s:Sudoku) RETURN s LIMIT 10
```

## 📊 Endpoints disponibles

### Resolver y guardar
```bash
POST http://localhost:8080/api/sudoku/solve/auto
```
Automáticamente guarda en Neo4j si se resuelve exitosamente.

### Consultar historial
```bash
GET http://localhost:8080/api/sudoku/history
```

### Ver por ID
```bash
GET http://localhost:8080/api/sudoku/history/1
```

### Estadísticas por algoritmo
```bash
GET http://localhost:8080/api/sudoku/stats/algorithm/backtracking
GET http://localhost:8080/api/sudoku/stats/algorithm/dfs
```

### Los más rápidos
```bash
GET http://localhost:8080/api/sudoku/stats/fastest?limit=5
```

### Por dificultad
```bash
GET http://localhost:8080/api/sudoku/stats/difficulty/Fácil
GET http://localhost:8080/api/sudoku/stats/difficulty/Medio
GET http://localhost:8080/api/sudoku/stats/difficulty/Difícil
```

## 🔍 Queries útiles en Neo4j Browser

### Ver todos los Sudokus
```cypher
MATCH (s:Sudoku) 
RETURN s.id, s.algorithm, s.runtimeMs, s.difficulty, s.createdAt
ORDER BY s.createdAt DESC
```

### Promedio de tiempo por algoritmo
```cypher
MATCH (s:Sudoku)
WHERE s.solved = true
RETURN s.algorithm, 
       avg(s.runtimeMs) as avgTime, 
       count(s) as total,
       min(s.runtimeMs) as fastest
ORDER BY avgTime
```

### Distribución por dificultad
```cypher
MATCH (s:Sudoku)
RETURN s.difficulty, count(s) as cantidad
```

### Comparar algoritmos
```cypher
MATCH (s:Sudoku)
WHERE s.solved = true
RETURN s.algorithm, 
       avg(s.runtimeMs) as avgRuntime,
       avg(s.memoryMB) as avgMemory,
       count(s) as totalSolved
ORDER BY avgRuntime
```

## 🛠️ Troubleshooting

### Error: "Neo4j no disponible"
- Verifica que Neo4j esté corriendo: http://localhost:7474
- Revisa usuario/password en `application.properties`
- Si usas Docker: `docker ps` para verificar el contenedor

### Error de conexión al iniciar Spring Boot
El servidor intentará conectarse a Neo4j al arrancar. Si falla:
1. Inicia Neo4j primero
2. Luego reinicia el servidor Spring Boot

### Cambiar puerto de Neo4j
Si 7687 está ocupado, cambia el puerto en Neo4j y en `application.properties`:
```properties
spring.neo4j.uri=bolt://localhost:NUEVO_PUERTO
```

## 📈 Modelo de datos

```
(Sudoku)
├── id: Long (auto-generado)
├── initialGrid: String (grid inicial serializado)
├── solvedGrid: String (grid resuelto serializado)
├── algorithm: String (backtracking, dfs, etc.)
├── runtimeMs: Double (tiempo de ejecución)
├── memoryMB: Double (memoria usada)
├── solved: Boolean (true si se resolvió)
├── createdAt: LocalDateTime (timestamp)
├── difficulty: String (Fácil, Medio, Difícil)
└── emptyCells: Integer (celdas vacías iniciales)
```
