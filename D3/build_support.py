#!/usr/bin/env python3
"""Build D3 summary tables and figures from D3Analyzer's CSV outputs."""

import csv
import json
import statistics
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np


ROOT = Path(__file__).resolve().parent
OUT = ROOT / "results"


def read_csv(name):
    with (OUT / name).open(encoding="utf-8", newline="") as f:
        return list(csv.DictReader(f))


def write_csv(name, rows, fields):
    with (OUT / name).open("w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


classes = read_csv("class_metrics.csv")
methods = read_csv("method_cc.csv")
summary_text = (OUT / "analysis_summary.txt").read_text(encoding="utf-8")
summary = dict(line.split("=", 1) for line in summary_text.splitlines() if "=" in line)

cc_values = [int(row["cyclomatic_complexity"]) for row in methods]
cc_counts = {value: cc_values.count(value) for value in range(1, max(cc_values) + 1)}
cc_bins = [
    {"CC": str(value), "methods": cc_counts.get(value, 0)}
    for value in range(1, max(cc_values) + 1)
]
write_csv("p7_cc_distribution.csv", cc_bins, ["CC", "methods"])

classes_by_wmc = sorted(classes, key=lambda row: int(row["wmc"]), reverse=True)
write_csv(
    "p8_top_wmc.csv",
    [
        {
            "class": row["class"],
            "WMC": row["wmc"],
            "logical_SLOC": row["logical_sloc"],
            "methods_and_constructors": row["method_and_constructor_count"],
        }
        for row in classes_by_wmc[:10]
    ],
    ["class", "WMC", "logical_SLOC", "methods_and_constructors"],
)

lcom_rows = [row for row in classes if row["lcom_star"]]
lcom_values = [float(row["lcom_star"]) for row in lcom_rows]
write_csv(
    "p8_lcom_summary.csv",
    [
        {
            "classes_total": len(classes),
            "classes_computable": len(lcom_values),
            "classes_NA": len(classes) - len(lcom_values),
            "mean_LCOM_star": f"{statistics.mean(lcom_values):.6f}",
            "median_LCOM_star": f"{statistics.median(lcom_values):.6f}",
            "min_LCOM_star": f"{min(lcom_values):.6f}",
            "max_LCOM_star": f"{max(lcom_values):.6f}",
        }
    ],
    [
        "classes_total",
        "classes_computable",
        "classes_NA",
        "mean_LCOM_star",
        "median_LCOM_star",
        "min_LCOM_star",
        "max_LCOM_star",
    ],
)

actors = [
    {"actor": "Cardholder", "actor_type": "Complex GUI user", "weight": 3, "count": 1},
    {"actor": "Bank core", "actor_type": "Simple API/service", "weight": 1, "count": 1},
    {"actor": "Cash dispenser", "actor_type": "Simple device interface", "weight": 1, "count": 1},
    {"actor": "Receipt printer", "actor_type": "Simple device interface", "weight": 1, "count": 1},
]
for row in actors:
    row["weighted_points"] = row["weight"] * row["count"]
write_csv("p9_actor_weights.csv", actors, ["actor", "actor_type", "weight", "count", "weighted_points"])

use_cases = [
    {"use_case": "Select language", "transactions": 1, "complexity": "Simple", "weight": 5,
     "scope_note": "One user selection; EN/FR."},
    {"use_case": "Authenticate", "transactions": 3, "complexity": "Simple", "weight": 5,
     "scope_note": "Card/PIN entry and authentication; lockout is an alternate flow."},
    {"use_case": "Inquire balance", "transactions": 3, "complexity": "Simple", "weight": 5,
     "scope_note": "Select balance flow/account and display result."},
    {"use_case": "Withdraw CAD", "transactions": 8, "complexity": "Complex", "weight": 15,
     "scope_note": "Account, amount, optional note mix, validation, dispense, posting, receipt."},
    {"use_case": "Deposit CAD", "transactions": 6, "complexity": "Average", "weight": 10,
     "scope_note": "Account/amount, validation, posting, optional receipt."},
    {"use_case": "Transfer own accounts", "transactions": 7, "complexity": "Average", "weight": 10,
     "scope_note": "Source, target, amount, validation, posting, optional receipt."},
    {"use_case": "Change PIN", "transactions": 5, "complexity": "Average", "weight": 10,
     "scope_note": "Current PIN, new PIN, confirmation, validation, update."},
    {"use_case": "End session", "transactions": 2, "complexity": "Simple", "weight": 5,
     "scope_note": "End-session action or idle timeout, then clear session."},
]
for row in use_cases:
    row["weighted_points"] = row["weight"]
write_csv("p9_use_case_points.csv", use_cases,
          ["use_case", "transactions", "complexity", "weight", "weighted_points", "scope_note"])

technical = [
    ("T1", "Distributed system", 2.0),
    ("T2", "Response time / performance", 1.0),
    ("T3", "End-user efficiency", 1.0),
    ("T4", "Complex internal processing", 1.0),
    ("T5", "Reusable code", 1.0),
    ("T6", "Easy to install", 0.5),
    ("T7", "Easy to use", 0.5),
    ("T8", "Portable", 2.0),
    ("T9", "Easy to change", 1.0),
    ("T10", "Concurrent", 1.0),
    ("T11", "Security features", 1.0),
    ("T12", "Third-party access", 1.0),
    ("T13", "Special user training", 1.0),
]
environment = [
    ("E1", "Familiarity with project", 1.5),
    ("E2", "Application experience", 0.5),
    ("E3", "Object-oriented experience", 1.0),
    ("E4", "Lead analyst capability", 0.5),
    ("E5", "Motivation", 1.0),
    ("E6", "Requirements stability", 2.0),
    ("E7", "Part-time staff", -1.0),
    ("E8", "Difficulty of programming language", -1.0),
]
factor_rows = []
for factor, description, weight in technical:
    factor_rows.append({"factor": factor, "description": description, "weight": weight,
                        "rating_0_to_5": 3, "weighted_rating": weight * 3,
                        "group": "Technical"})
for factor, description, weight in environment:
    factor_rows.append({"factor": factor, "description": description, "weight": weight,
                        "rating_0_to_5": 3, "weighted_rating": weight * 3,
                        "group": "Environmental"})
write_csv("p9_factor_ratings.csv", factor_rows,
          ["group", "factor", "description", "weight", "rating_0_to_5", "weighted_rating"])

uaw = sum(row["weighted_points"] for row in actors)
uucw = sum(row["weighted_points"] for row in use_cases)
t_sum = sum(weight * 3 for _, _, weight in technical)
e_sum = sum(weight * 3 for _, _, weight in environment)
tcf = 0.6 + 0.01 * t_sum
ecf = 1.4 - 0.03 * e_sum
uucp = uaw + uucw
ucp = uucp * tcf * ecf
ucp_effort = ucp * 20

kloc = int(summary["class_logical_sloc_excluding_package_import"]) / 1000
cocomo_pm = 2.4 * kloc ** 1.05
cocomo_months = 2.5 * cocomo_pm ** 0.38
hours_per_month = 160
cocomo_hours = cocomo_pm * hours_per_month
estimated_actual_hours = 50
estimate_rows = [
    {"estimate": "Adjusted Use Case Points", "value": f"{ucp:.4f}", "unit": "UCP",
     "basis": f"({uaw} UAW + {uucw} UUCW) x TCF {tcf:.3f} x ECF {ecf:.3f}"},
    {"estimate": "UCP effort at Karner productivity default", "value": f"{ucp_effort:.1f}",
     "unit": "person-hours", "basis": "Adjusted UCP x 20 person-hours/UCP; generic, not project-calibrated."},
    {"estimate": "UCP sensitivity at 15 person-hours/UCP", "value": f"{ucp * 15:.1f}",
     "unit": "person-hours", "basis": "Adjusted UCP x 15; lower end of commonly cited range."},
    {"estimate": "UCP sensitivity at 30 person-hours/UCP", "value": f"{ucp * 30:.1f}",
     "unit": "person-hours", "basis": "Adjusted UCP x 30; upper end of commonly cited range."},
    {"estimate": "Basic COCOMO 81 effort", "value": f"{cocomo_pm:.4f}", "unit": "person-months",
     "basis": f"Organic mode: 2.4 x {kloc:.3f} KLOC^1.05."},
    {"estimate": "Basic COCOMO 81 effort converted", "value": f"{cocomo_hours:.1f}",
     "unit": "person-hours", "basis": f"{cocomo_pm:.4f} PM x {hours_per_month} hours/PM."},
    {"estimate": "Basic COCOMO 81 schedule", "value": f"{cocomo_months:.4f}",
     "unit": "months", "basis": f"Organic mode: 2.5 x effort^0.38."},
    {"estimate": "Retrospective D2 effort estimate", "value": str(estimated_actual_hours),
     "unit": "person-hours", "basis": "Estimated from D2 work scope and role record; no timesheet in ZIP."},
]
write_csv("p9_effort_estimates.csv", estimate_rows, ["estimate", "value", "unit", "basis"])

points = [
    {"class": row["class"], "logical_SLOC": int(row["logical_sloc"]), "WMC": int(row["wmc"]),
     "LCOM_star": row["lcom_star"], "outbound_couplings": int(row["dependencies"])}
    for row in classes
]
write_csv("p10_scatter_data.csv", points,
          ["class", "logical_SLOC", "WMC", "LCOM_star", "outbound_couplings"])
x = np.array([row["logical_SLOC"] for row in points], dtype=float)
y = np.array([row["WMC"] for row in points], dtype=float)
r = float(np.corrcoef(x, y)[0, 1])
r2 = r * r
slope, intercept = np.polyfit(x, y, 1)
with (OUT / "p10_correlation.txt").open("w", encoding="utf-8") as f:
    f.write(f"n={len(points)}\nPearson_r={r:.6f}\nR_squared={r2:.6f}\n")
    f.write(f"linear_fit_WMC={slope:.6f}*logical_SLOC+{intercept:.6f}\n")
    f.write("Interpretation: strong positive association in this source snapshot; not causal.\n")

plt.rcParams.update({"font.family": "Liberation Sans", "font.size": 10})
fig, ax = plt.subplots(figsize=(8.7, 5.15), dpi=180)
ax.scatter(x, y, s=32, color="#8B1239", edgecolors="white", linewidths=0.45, alpha=0.9)
line_x = np.linspace(0, max(x) * 1.06, 100)
ax.plot(line_x, slope * line_x + intercept, color="#19334E", linewidth=1.8,
        label=f"Linear fit (r = {r:.3f})")
ax.set_xlabel("Class logical SLOC")
ax.set_ylabel("WMC")
ax.set_title("Logical SLOC vs WMC by declared production type")
ax.grid(True, color="#D9E0E5", linewidth=0.65)
ax.set_axisbelow(True)
ax.legend(frameon=False, loc="upper left")
ax.text(0.98, 0.04, f"n = {len(points)}  |  r = {r:.3f}  |  r² = {r2:.3f}",
        transform=ax.transAxes, ha="right", va="bottom", color="#19334E",
        bbox={"boxstyle": "round,pad=0.35", "facecolor": "#F4F6F8", "edgecolor": "#D9E0E5"})
label_offsets = {
    "CashDispenser": (10, 12),
    "AtmFrame": (10, 22),
    "StateStore": (12, -4),
    "TransactionService": (10, 8),
}
for row in sorted(points, key=lambda p: p["WMC"], reverse=True)[:4]:
    label = row["class"].split(".")[-1]
    ax.annotate(label, (row["logical_SLOC"], row["WMC"]),
                xytext=label_offsets.get(label, (5, 6)), textcoords="offset points",
                fontsize=8, color="#202833")
fig.tight_layout()
fig.savefig(OUT / "p10_sloc_wmc_scatter.png", bbox_inches="tight", facecolor="white")
plt.close(fig)

fig, ax = plt.subplots(figsize=(7.8, 4.5), dpi=180)
labels = [str(v) for v in sorted(cc_counts)]
values = [cc_counts[v] for v in sorted(cc_counts)]
ax.bar(labels, values, color="#8B1239", width=0.66)
ax.set_xlabel("Cyclomatic complexity per method")
ax.set_ylabel("Method count")
ax.set_title("Cyclomatic complexity distribution")
ax.grid(axis="y", color="#D9E0E5", linewidth=0.65)
ax.set_axisbelow(True)
fig.tight_layout()
fig.savefig(OUT / "p7_cc_distribution.png", bbox_inches="tight", facecolor="white")
plt.close(fig)

with (OUT / "derived_summary.json").open("w", encoding="utf-8") as f:
    json.dump({
        "class_count": len(classes), "method_count": len(methods),
        "total_wmc": sum(int(row["wmc"]) for row in classes),
        "logical_sloc": sum(int(row["logical_sloc"]) for row in classes),
        "cf_couplings": int(summary["internal_directed_couplings"]),
        "cf_maximum": int(summary["maximum_possible_couplings"]),
        "cf": float(summary["CF"].split()[0]),
        "cc_mean": statistics.mean(cc_values), "cc_median": statistics.median(cc_values),
        "cc_max": max(cc_values), "cc_counts": cc_counts,
        "lcom_computable": len(lcom_values), "lcom_mean": statistics.mean(lcom_values),
        "lcom_median": statistics.median(lcom_values), "lcom_min": min(lcom_values),
        "lcom_max": max(lcom_values), "uaw": uaw, "uucw": uucw, "uucp": uucp,
        "t_sum": t_sum, "tcf": tcf, "e_sum": e_sum, "ecf": ecf, "ucp": ucp,
        "ucp_effort_hours_20": ucp_effort, "kloc": kloc, "cocomo_pm": cocomo_pm,
        "cocomo_months": cocomo_months, "cocomo_hours": cocomo_hours,
        "estimated_actual_hours": estimated_actual_hours, "pearson_r": r, "r_squared": r2,
        "fit_slope": slope, "fit_intercept": intercept,
    }, f, indent=2)

print(json.dumps(json.load(open(OUT / "derived_summary.json", encoding="utf-8")), indent=2))
