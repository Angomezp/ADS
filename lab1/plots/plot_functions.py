import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os


def graph_log_log_plot(
        csv_path: str,
        plot_name: str,
        y_column: str,
        x_column: str,
        title: str,
        output_dir: str = "images",
        structure_column: str = "Structure"
    ) -> None:

    # Load CSV
    df = pd.read_csv(csv_path)
    df.columns = df.columns.str.strip()

    # Ensure output directory exists
    os.makedirs(output_dir, exist_ok=True)

    # Full path
    full_path = os.path.join(output_dir, plot_name)

    # Create figure
    plt.figure(figsize=(8, 6))

    # Get structures
    structures = df[structure_column].unique()

    # Plot
    for structure in structures:

        structure_df = (
            df[df[structure_column] == structure]
            .sort_values(x_column)
        )

        plt.plot(
            structure_df[x_column],
            structure_df[y_column],
            marker="o",
            label=structure
        )

    # Log scale
    plt.xscale("log", base=2)
    plt.yscale("log", base=2)

    # Labels
    plt.xlabel(x_column)
    plt.ylabel(y_column)
    plt.title(title)

    # Grid and legend
    plt.grid(True, which="both", linestyle="--", linewidth=0.5)
    plt.legend(fontsize=8, loc="best")

    plt.tight_layout()
    plt.savefig(full_path, dpi=300, bbox_inches="tight")
    plt.close()


def graph_tradeoff_scatter(
        preprocessing_csv: str,
        query_csv: str,
        memory_csv: str,
        plot_name: str,
        preprocessing_col: str,
        query_col: str,
        memory_col: str,
        structure_column: str = "Structure",
        n_column: str = "n",
        output_dir: str = "images",
        title: str | None = None
    ) -> None:

    os.makedirs(output_dir, exist_ok=True)
    full_path = os.path.join(output_dir, plot_name)

    # ---------------- LOAD ----------------
    df_p = pd.read_csv(preprocessing_csv)
    df_q = pd.read_csv(query_csv)
    df_m = pd.read_csv(memory_csv)

    for df in (df_p, df_q, df_m):
        df.columns = df.columns.str.strip()

    # ---------------- RENAME ----------------
    df_p = df_p[[structure_column, n_column, preprocessing_col]].rename(
        columns={preprocessing_col: "preprocess"}
    )
    df_q = df_q[[structure_column, n_column, query_col]].rename(
        columns={query_col: "query"}
    )
    df_m = df_m[[structure_column, n_column, memory_col]].rename(
        columns={memory_col: "memory"}
    )

    # ---------------- MERGE ----------------
    df = df_p.merge(df_q, on=[structure_column, n_column])
    df = df.merge(df_m, on=[structure_column, n_column])

    # ---------------- REPRESENTATIVE POINT (max n) ----------------
    df_rep = df.loc[df.groupby(structure_column)[n_column].idxmax()].copy()

    # ---------------- CLEAN (convert to numeric) ----------------
    df_rep["preprocess"] = pd.to_numeric(df_rep["preprocess"], errors="coerce")
    df_rep["query"] = pd.to_numeric(df_rep["query"], errors="coerce")
    df_rep["memory"] = pd.to_numeric(df_rep["memory"], errors="coerce")

    # --------------------------
    # REPLACE ZEROS / NEGATIVES
    # --------------------------
    EPS = 1e-12   # constante muy pequeña para preprocess y query

    # Preprocess: valores <= 0 -> EPS
    df_rep["preprocess"] = df_rep["preprocess"].apply(lambda x: EPS if x <= 0 else x)

    # Query: valores <= 0 -> EPS
    df_rep["query"] = df_rep["query"].apply(lambda x: EPS if x <= 0 else x)

    # Memory: valores <= 0 -> pequeño relativo al mínimo positivo
    positive_mem = df_rep["memory"][df_rep["memory"] > 0]
    if len(positive_mem) > 0:
        min_mem_pos = positive_mem.min()
        small_mem = min_mem_pos * 0.1
    else:
        small_mem = 1e-13   # si todos son cero, usar un valor extremadamente pequeño

    df_rep["memory"] = df_rep["memory"].apply(lambda x: small_mem if x <= 0 else x)

    # ---------------- SIZE SCALING (log-safe) ----------------
    mem_log = np.log10(df_rep["memory"])

    df_rep["size"] = 30 + (
        (mem_log - mem_log.min()) /
        (mem_log.max() - mem_log.min() + 1e-9)
    ) * 400

    # ---------------- PLOT ----------------
    plt.figure(figsize=(10, 8))

    # Identificar estructuras "Naive" (para etiqueta separada, opcional)
    is_naive = df_rep[structure_column].str.contains("Naive", case=False, na=False)

    # No Naive
    df_other = df_rep[~is_naive]
    plt.scatter(
        df_other["preprocess"],
        df_other["query"],
        s=df_other["size"],
        alpha=0.75,
        edgecolors="black",
        linewidths=0.5,
        label="Structures"
    )

    # Naive (si existe)
    df_naive = df_rep[is_naive]
    if len(df_naive) > 0:
        plt.scatter(
            df_naive["preprocess"],
            df_naive["query"],
            s=df_naive["size"],
            alpha=0.75,
            edgecolors="black",
            linewidths=0.5,
            label="NaiveRMQ"
        )

    # Etiquetas de texto
    for _, row in df_rep.iterrows():
        plt.text(
            row["preprocess"],
            row["query"],
            row[structure_column],
            fontsize=8
        )

    # ---------------- LOG SCALE ----------------
    plt.xscale("log", base=2)
    plt.yscale("log", base=2)

    # Padding en espacio logarítmico
    x_min, x_max = df_rep["preprocess"].min(), df_rep["preprocess"].max()
    y_min, y_max = df_rep["query"].min(), df_rep["query"].max()

    plt.xlim(x_min / 2, x_max * 2)
    plt.ylim(y_min / 2, y_max * 2)

    # ---------------- LABELS ----------------
    plt.xlabel("Median Preprocessing Time")
    plt.ylabel("Median Query Time")

    if title:
        plt.title(title)

    plt.grid(True, which="both", linestyle="--", linewidth=0.5)
    plt.tight_layout()
    plt.savefig(full_path, dpi=300, bbox_inches="tight")
    plt.close()
    
def graph_throughput_vs_size(
        csv_path: str,
        plot_name: str,
        throughput_col: str,
        x_column: str = "n",
        structure_column: str = "Structure",
        output_dir: str = "images",
        title: str | None = None,
        log_x: bool = True
    ) -> None:

    os.makedirs(output_dir, exist_ok=True)
    full_path = os.path.join(output_dir, plot_name)

    df = pd.read_csv(csv_path)
    df.columns = df.columns.str.strip()

    # Ensure numeric
    df[x_column] = pd.to_numeric(df[x_column], errors="coerce")
    df[throughput_col] = pd.to_numeric(df[throughput_col], errors="coerce")

    df = df.dropna(subset=[x_column, throughput_col])

    plt.figure(figsize=(10, 7))

    structures = df[structure_column].unique()

    for s in structures:
        sub = df[df[structure_column] == s].sort_values(x_column)

        plt.plot(
            sub[x_column],
            sub[throughput_col],
            marker="o",
            label=s
        )

    # X scale
    if log_x:
        plt.xscale("log", base=2)

    plt.xlabel(x_column)
    plt.ylabel("Queries per second")

    if title:
        plt.title(title)

    plt.grid(True, which="both", linestyle="--", linewidth=0.5)
    plt.legend(fontsize=8, loc="best")

    plt.tight_layout()
    plt.savefig(full_path, dpi=300, bbox_inches="tight")
    plt.close()

def graph_mean_median_query_time(
        csv_path: str,
        plot_name: str,
        mean_col: str,
        median_col: str,
        x_column: str = "n",
        structure_column: str = "Structure",
        output_dir: str = "images",
        title: str | None = None,
        log_x: bool = True,
        log_y: bool = True
    ) -> None:

    os.makedirs(output_dir, exist_ok=True)
    full_path = os.path.join(output_dir, plot_name)

    df = pd.read_csv(csv_path)
    df.columns = df.columns.str.strip()

    # Ensure numeric
    df[x_column] = pd.to_numeric(df[x_column], errors="coerce")
    df[mean_col] = pd.to_numeric(df[mean_col], errors="coerce")
    df[median_col] = pd.to_numeric(df[median_col], errors="coerce")

    df = df.dropna(subset=[x_column, mean_col, median_col])

    structures = df[structure_column].unique()

    for s in structures:
        sub = df[df[structure_column] == s].sort_values(x_column)

        plt.figure(figsize=(10, 7))

        plt.plot(sub[x_column], sub[mean_col],
                 marker="o", label="Mean")

        plt.plot(sub[x_column], sub[median_col],
                 marker="o", label="Median")

        if log_x:
            plt.xscale("log", base=2)

        if log_y:
            plt.yscale("log", base=2)

        plt.xlabel(x_column)
        plt.ylabel("Query time")

        plt.title(f"{title} - {s}" if title else s)

        plt.grid(True, which="both", linestyle="--", linewidth=0.5)
        plt.legend(fontsize=9)

        structure_safe = s.replace(" ", "")
        plt.tight_layout()
        plt.savefig(os.path.join(output_dir, f"{plot_name}_{structure_safe}.png"),
                    dpi=300, bbox_inches="tight")
        plt.close()


def graph_query_time_with_error_bars(
        csv_path: str,
        plot_name: str,
        mean_col: str,
        std_col: str,
        median_col: str,
        structure_column: str = "Structure",
        n_column: str = "n",
        output_dir: str = "images",
        title: str | None = None,
        log_x: bool = True,
        log_y: bool = True
    ) -> None:

    os.makedirs(output_dir, exist_ok=True)
    full_path = os.path.join(output_dir, plot_name)

    df = pd.read_csv(csv_path)
    df.columns = df.columns.str.strip()

    # Ensure numeric
    df[n_column] = pd.to_numeric(df[n_column], errors="coerce")
    df[mean_col] = pd.to_numeric(df[mean_col], errors="coerce")
    df[std_col] = pd.to_numeric(df[std_col], errors="coerce")
    df[median_col] = pd.to_numeric(df[median_col], errors="coerce")

    df = df.dropna(subset=[n_column, mean_col, std_col, median_col])

    structures = df[structure_column].unique()

    for s in structures:
        sub = df[df[structure_column] == s].sort_values(n_column)

        plt.figure(figsize=(10, 7))

        # Median line
        plt.plot(
            sub[n_column],
            sub[median_col],
            marker="o",
            label="Median"
        )

        # Error bars (std around median or mean)
        plt.errorbar(
            sub[n_column],
            sub[median_col],
            yerr=sub[std_col],
            fmt="none",
            capsize=4,
            alpha=0.6
        )

        if log_x:
            plt.xscale("log", base=2)

        if log_y:
            plt.yscale("log", base=2)

        plt.xlabel("n")
        plt.ylabel("Query time")

        if title:
            plt.title(f"{title} - {s}")
        else:
            plt.title(s)

        plt.grid(True, which="both", linestyle="--", linewidth=0.5)
        plt.legend(fontsize=9)

        plt.tight_layout()
        plt.savefig(
            os.path.join(output_dir, f"{plot_name}_{s.replace(' ', '')}.png"),
            dpi=300,
            bbox_inches="tight"
        )
        plt.close()

def graph_query_time_performance_range(
        csv_path: str,
        plot_name: str,
        min_col: str,
        median_col: str,
        max_col: str,
        structure_column: str = "Structure",
        n_column: str = "n",
        output_dir: str = "images",
        title: str | None = None,
        log_x: bool = True,
        log_y: bool = True
    ) -> None:

    os.makedirs(output_dir, exist_ok=True)
    full_path = os.path.join(output_dir, plot_name)

    df = pd.read_csv(csv_path)
    df.columns = df.columns.str.strip()

    # Ensure numeric
    for col in [n_column, min_col, median_col, max_col]:
        df[col] = pd.to_numeric(df[col], errors="coerce")

    df = df.dropna(subset=[n_column, min_col, median_col, max_col])

    structures = df[structure_column].unique()

    for s in structures:
        sub = df[df[structure_column] == s].sort_values(n_column)

        plt.figure(figsize=(10, 7))

        # Range (min-max band)
        plt.fill_between(
            sub[n_column],
            sub[min_col],
            sub[max_col],
            alpha=0.2,
            label="Min-Max Range"
        )

        # Median line
        plt.plot(
            sub[n_column],
            sub[median_col],
            marker="o",
            label="Median"
        )

        if log_x:
            plt.xscale("log", base=2)

        if log_y:
            plt.yscale("log", base=2)

        plt.xlabel("n")
        plt.ylabel("Query time")

        if title:
            plt.title(f"{title} - {s}")
        else:
            plt.title(s)

        plt.grid(True, which="both", linestyle="--", linewidth=0.5)
        plt.legend(fontsize=9)

        plt.tight_layout()
        plt.savefig(
            os.path.join(output_dir, f"{plot_name}_{s.replace(' ', '')}.png"),
            dpi=300,
            bbox_inches="tight"
        )
        plt.close()

def graph_tradeoff_scatter_fix(
        preprocessing_csv: str,
        query_csv: str,
        memory_csv: str,
        plot_name: str,
        preprocessing_col: str,
        query_col: str,
        memory_col: str,
        structure_column: str = "Structure",
        n_column: str = "n",
        output_dir: str = "images",
        title: str | None = None
    ) -> None:

    os.makedirs(output_dir, exist_ok=True)
    full_path = os.path.join(output_dir, plot_name)

    # ---------------- LOAD ----------------
    df_p = pd.read_csv(preprocessing_csv)
    df_q = pd.read_csv(query_csv)
    df_m = pd.read_csv(memory_csv)

    for df in (df_p, df_q, df_m):
        df.columns = df.columns.str.strip()

    # ---------------- RENAME ----------------
    df_p = df_p[[structure_column, n_column, preprocessing_col]].rename(
        columns={preprocessing_col: "preprocess"}
    )
    df_q = df_q[[structure_column, n_column, query_col]].rename(
        columns={query_col: "query"}
    )
    df_m = df_m[[structure_column, n_column, memory_col]].rename(
        columns={memory_col: "memory"}
    )

    # ---------------- MERGE ----------------
    df = df_p.merge(df_q, on=[structure_column, n_column])
    df = df.merge(df_m, on=[structure_column, n_column])

    # ---------------- REPRESENTATIVE POINT (max n) ----------------
    df_rep = df.loc[df.groupby(structure_column)[n_column].idxmax()].copy()

    # ---------------- CLEAN ----------------
    df_rep["preprocess"] = pd.to_numeric(df_rep["preprocess"], errors="coerce")
    df_rep["query"] = pd.to_numeric(df_rep["query"], errors="coerce")
    df_rep["memory"] = pd.to_numeric(df_rep["memory"], errors="coerce")

    # ---------------- FIX Naive (0 preprocessing breaks log scale) ----------------
    EPS = 1e-9
    is_naive = df_rep[structure_column].str.contains("Naive", case=False, na=False)
    df_rep.loc[is_naive, "preprocess"] = EPS

    # Remove invalid values
    df_rep = df_rep[
        (df_rep["preprocess"] > 0) &
        (df_rep["query"] > 0) &
        (df_rep["memory"] > 0)
    ]
    # ---- MEMORY FIX (handles 0 values) ----
    df_rep["memory"] = pd.to_numeric(df_rep["memory"], errors="coerce")

    # replace 0 with small positive value (keeps ordering but visible)
    min_nonzero = df_rep.loc[df_rep["memory"] > 0, "memory"].min()

    if pd.isna(min_nonzero):
        min_nonzero = 1

    df_rep["memory"] = df_rep["memory"].replace(0, min_nonzero * 0.1)
    # ---------------- SIZE SCALING (log-safe) ----------------
    mem_log = np.log10(df_rep["memory"])

    df_rep["size"] = 30 + (
        (mem_log - mem_log.min()) /
        (mem_log.max() - mem_log.min() + 1e-9)
    ) * 400

    # ---------------- PLOT ----------------
    plt.figure(figsize=(10, 8))

    # Non-Naive
    df_other = df_rep[~is_naive]

    plt.scatter(
        df_other["preprocess"],
        df_other["query"],
        s=df_other["size"],
        alpha=0.75,
        edgecolors="black",
        linewidths=0.5,
        label="Structures"
    )

    # Naive (plotted separately at epsilon)
    df_naive = df_rep[is_naive]

    if len(df_naive) > 0:
        plt.scatter(
            np.full(len(df_naive), EPS),
            df_naive["query"],
            s=df_naive["size"],
            alpha=0.75,
            edgecolors="black",
            linewidths=0.5,
            label="NaiveRMQ"
        )

    # Labels
    for _, row in df_rep.iterrows():
        plt.text(
            row["preprocess"],
            row["query"],
            row[structure_column],
            fontsize=8
        )

    # ---------------- LOG SCALE ----------------
    plt.xscale("log", base=2)
    plt.yscale("log", base=2)

    # Padding (safe in log space)
    x_min, x_max = df_rep["preprocess"].min(), df_rep["preprocess"].max()
    y_min, y_max = df_rep["query"].min(), df_rep["query"].max()

    plt.xlim(x_min / 2, x_max * 2)
    plt.ylim(y_min / 2, y_max * 2)

    # ---------------- LABELS ----------------
    plt.xlabel("Median Preprocessing Time")
    plt.ylabel("Median Query Time")

    if title:
        plt.title(title)

    plt.grid(True, which="both", linestyle="--", linewidth=0.5)

    plt.tight_layout()
    plt.savefig(full_path, dpi=300, bbox_inches="tight")
    plt.close()
