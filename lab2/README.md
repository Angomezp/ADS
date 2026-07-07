# Advanced Data Structures

## Laboratorio 2

## Autor: Angel David Gomez Pastrana

### Descripción breve
Esta carpeta contiene los datos, código y scripts usados en la práctica de laboratorio.

Archivos y carpetas (explicación):
- `csv/`:
	- `search.csv`: resultados de los experimentos de búsqueda.
	- `insert.csv`: resultados de los experimentos de inserción.
	- `range10.csv`: resultados de consultas de rango con ventana 10.
	- `range100.csv`: resultados de consultas de rango con ventana 100.
	- `range1000.csv`: resultados de consultas de rango con ventana 1000.
	- `range5000.csv`: resultados de consultas de rango con ventana 5000.
	- `traversal.csv`: resultados de los experimentos de recorrido.
- `plots/`:
	- `plots.py`: script Python que genera las gráficas del laboratorio a partir de los CSV.
	- `images/`: carpeta donde se guardan las imágenes generadas.
- `src/`:
	- `ExperimentsUtils/`: utilidades Java para generar datasets y ejecutar experimentos (`DatasetGenerator.java`, `ExperimentResult.java`, `InsertExperiment.java`, `RangeExperiment.java`, `RangeQuery.java`, `SearchExperiment.java`, `TraversalExperiment.java`).
	- `Interfaces/`: interfaz de referencia para las estructuras AVL (`AVL_Tree_Interface.java`).
	- `LeafTree/`: implementación AVL basada en hojas (`AVL_LeafTree.java`, `InternalNode.java`, `LeafNode.java`, `Node.java`).
	- `NodeTree/`: implementación AVL basada en nodos (`AVL_NodeTree.java`, `Node.java`).
	- `Utils/`: utilidades para exportar y medir resultados (`CsvWriter.java`, `Metrics.java`).
- `Main.java`: clase principal Java que orquesta la generación de datos y la ejecución de los experimentos (paquete `lab2`).
- `setup.bat`: script Windows que compila recursivamente los `.java` en `out/` y ejecuta `lab2.Main`.
	- Uso: `setup.bat` compila y ejecuta. `setup.bat clean` borra `out/`.
- `ADS_2026_1_Laboratorio_2.pdf`: PDF con el reporte del laboratorio (explicaciones, gráficas, etc.).

Cómo usar (desde la raíz `lab2`):
- Generar gráficos:
	```bash
	python plots/plots.py
	```
- Compilar y ejecutar la parte Java (Windows):
	```bat
	setup.bat        # compila y ejecuta lab2.Main
	setup.bat clean  # borra carpeta out/
	```
