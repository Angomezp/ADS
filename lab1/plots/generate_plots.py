import plot_functions
from pathlib import Path

# Resolve paths relative to lab1 directory (two levels up from this file)
BASE_DIR = Path(__file__).resolve().parent.parent
CSV_DIR = BASE_DIR / "csv"
PLOTS_DIR = BASE_DIR / "plots" / "images"

preprocessing_time_path = str(CSV_DIR / "preprocessing_time.csv")
memory_usage_path = str(CSV_DIR / "memory_usage.csv")
query_time_path = str(CSV_DIR / "query_time.csv")
OUTPUT_DIR = str(PLOTS_DIR)


# PLOT 1: Median Preprocessing Time vs Array Size 
plot_functions.graph_log_log_plot(
    csv_path= preprocessing_time_path,
    x_column= "n",
    y_column="Median(ms)",
    title="Median Preprocessing Time vs. Array Size",
    plot_name= "1_MedPrepTime_ArraySize.png",
    output_dir= OUTPUT_DIR
)

# PLOT 2: Median Query Time vs Array Size 
plot_functions.graph_log_log_plot(
    csv_path= query_time_path,
    x_column= "n",
    y_column="Median(ms)",
    title="Median Query Time vs. Array Size",
    plot_name= "2_MedQueryTime_ArraySize.png",
    output_dir= OUTPUT_DIR
)

# PLOT 3: Memory Usage (Bytes) vs Array Size
plot_functions.graph_log_log_plot(
    csv_path= memory_usage_path,
    x_column= "n",
    y_column="Bytes",
    title="Memory Usage vs. Array Size",
    plot_name= "3_BytesMemUsage_ArraySize.png",
    output_dir= OUTPUT_DIR
)

#PLOT 4: Scatterplot Median Query Time vs Median Preprocessing, Memory Usage as bubble size
plot_functions.graph_tradeoff_scatter(
    preprocessing_csv = preprocessing_time_path,
    query_csv = query_time_path,
    memory_csv = memory_usage_path,
    plot_name = "4_tradeoff_scatterplot.png",
    preprocessing_col = "Median(ms)",
    query_col = "Median(ms)",
    memory_col = "BytesPerElement",
    title = "Tradeoff Scatter",
    output_dir = OUTPUT_DIR
)

plot_functions.graph_tradeoff_scatter_fix(
    preprocessing_csv = preprocessing_time_path,
    query_csv = query_time_path,
    memory_csv = memory_usage_path,
    plot_name = "4_tradeoff_scatterplot_fix.png",
    preprocessing_col = "Median(ms)",
    query_col = "Median(ms)",
    memory_col = "BytesPerElement",
    title = "Tradeoff Scatter",
    output_dir = OUTPUT_DIR
)


#PLOT 5: Throughput vs Array Size

plot_functions.graph_throughput_vs_size(
    csv_path= query_time_path,
    plot_name = "5_throughput_vs_size.png", 
    throughput_col = "Throughput(ops/s)",
    x_column = "n",
    structure_column = "Structure",
    output_dir = OUTPUT_DIR,
    title = "Throughput(ops/s) vs Array Size",
    log_x = True
)

#PLOTS 6:  Mean and Median Comparison vs Array Size
plot_functions.graph_mean_median_query_time(
    csv_path= query_time_path,
    plot_name = "6_mean_med",
    mean_col = "Mean(ms)",
    median_col = "Median(ms)",
    x_column = "n",
    structure_column = "Structure",
    output_dir = OUTPUT_DIR,
    title = "Mean and Median vs Array Size",
    log_x = True,
    log_y = True
)

#PLOTS 7: Variability Analysis
plot_functions.graph_query_time_with_error_bars(
    csv_path = query_time_path,
    plot_name = "7_var_analysis",
    mean_col = "Mean(ms)",
    median_col = "Median(ms)",
    std_col = "StdDev(ms)",
    structure_column = "Structure",
    n_column = "n",
    output_dir = OUTPUT_DIR,
    title = "Variability Analysis",
    log_x = True,
    log_y = True
)

#PLOTS 8: Performance Range
plot_functions.graph_query_time_performance_range(
    csv_path = query_time_path,
    plot_name = "8_perf_range",
    min_col = "Min(ms)",
    median_col = "Median(ms)",
    max_col = "Max(ms)",
    structure_column = "Structure",
    n_column = "n",
    output_dir = OUTPUT_DIR,
    title = "Performance Range",
    log_x = True,
    log_y = True
)