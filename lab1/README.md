# Advanced Data Structures

## Laboratorio 1

## Autor: Angel David Gomez Pastrana

### Descripción breve
Esta carpeta contiene los datos, código y scripts usados en la práctica de laboratorio.

Archivos y carpetas (explicación):
- `csv/`:
	- `preprocessing_time.csv`: tiempos de preprocesamiento por estructura y tamaño.
	- `query_time.csv`: estadísticas de consulta (mean, median, stddev, throughput, etc.).
	- `memory_usage.csv`: uso de memoria por estructura y tamaño (bytes, bytes por elemento).
- `plots/`:
	- `generate_plots.py`: script Python que importa `plot_functions` y genera todos los gráficos.
	- `plot_functions.py`: funciones para crear gráficos (log-log, scatter tradeoff, throughput, error bars, ranges).
	- Los PNG resultantes se guardan en `lab1/plots/images`.
- `src/`:
	- `datastructures/`: implementaciones Java de las estructuras y RMQ (`NaiveRMQ.java`, `SegmentTreeRMQ.java`, `SparseTableRMQ.java`, `HybridARMQ.java`, `HybridBRMQ.java`, `HybridCRMQ.java`, `FischerHeunRMQ.java`, `BlockDecompRMQ.java`, `FullPreprocessingRMQ.java`).
	- `utils/`: utilidades Java para experimentos (`CSVHandler.java`, `ExperimentDataStructure.java`, `RandomGenerator.java`, `StatsUtils.java`, `Validator.java`).
- `Main.java`: clase principal Java que orquesta las validaciones y los experimentos (paquete `lab1`).
- `setup.bat`: script Windows que compila recursivamente los `.java` en `out/` y ejecuta `lab1.Main`.
	- Uso: `setup.bat` compila y ejecuta. `setup.bat clean` borra `out/`.
- `Reporte.pdf`: pdf en el que se encuentra el reporte del laboratorio (explicaciones, graficas, etc).
 - `Reporte.pdf`: PDF en el que se encuentra el reporte del laboratorio (explicaciones, gráficas, etc).

Cómo usar (rápido):
- Generar gráficos (desde la raíz `lab1`):
	```bash
	python plots/generate_plots.py
	```
- Compilar y ejecutar la parte Java (Windows):
	```bat
	setup.bat        # compila y ejecuta lab1.Main
	setup.bat clean  # borra carpeta out/
	```

