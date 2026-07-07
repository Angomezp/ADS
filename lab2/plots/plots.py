import os

import pandas as pd
import matplotlib.pyplot as plt

CSV_DIR = "lab2/csv"
OUTPUT_DIR = "lab2/plots/images"

os.makedirs(OUTPUT_DIR, exist_ok=True)


def save_plot(filename):
    plt.tight_layout()
    plt.savefig(os.path.join(OUTPUT_DIR, filename), dpi=300)
    plt.close()


def plot_metric(csv_file, metric, ylabel, title, output_file):
    df = pd.read_csv(os.path.join(CSV_DIR, csv_file))

    plt.figure(figsize=(8, 5))

    for tree in df["tree"].unique():
        subset = df[df["tree"] == tree]

        plt.plot(
            subset["n"],
            subset[metric],
            marker="o",
            linewidth=2,
            label=tree
        )

    plt.xlabel("Dataset Size (n)")
    plt.ylabel(ylabel)
    plt.title(title)
    plt.grid(True)
    plt.legend()

    save_plot(output_file)


def plot_search():
    plot_metric(
        "search.csv",
        "executionTimeNs",
        "Execution Time (ns)",
        "Point Search Execution Time",
        "search_time.png"
    )

    plot_metric(
        "search.csv",
        "averageNodesVisited",
        "Average Nodes Visited",
        "Point Search Average Nodes Visited",
        "search_nodes.png"
    )


def plot_insert():
    plot_metric(
        "insert.csv",
        "executionTimeNs",
        "Execution Time (ns)",
        "Insertion Execution Time",
        "insert_time.png"
    )

    plot_metric(
        "insert.csv",
        "rotations",
        "Total Rotations",
        "Insertion Rotations",
        "insert_rotations.png"
    )


def plot_range(workload):
    csv = f"{workload}.csv"

    plot_metric(
        csv,
        "executionTimeNs",
        "Execution Time (ns)",
        f"{workload} Execution Time",
        f"{workload}_time.png"
    )

    plot_metric(
        csv,
        "averageNodesVisited",
        "Average Nodes Visited",
        f"{workload} Average Nodes Visited",
        f"{workload}_nodes.png"
    )

    plot_metric(
        csv,
        "averageReportedKeys",
        "Average Reported Keys",
        f"{workload} Average Reported Keys",
        f"{workload}_reported.png"
    )


def plot_traversal():
    plot_metric(
        "traversal.csv",
        "executionTimeNs",
        "Execution Time (ns)",
        "Sequential Traversal",
        "traversal_time.png"
    )


def main():

    plot_search()

    plot_insert()

    plot_range("range10")
    plot_range("range100")
    plot_range("range1000")
    plot_range("range5000")

    plot_traversal()

    print("All plots generated successfully.")


if __name__ == "__main__":
    main()