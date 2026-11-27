# SOEN 6611 — D3 measurement package

This package measures the D2 production source included in this ZIP. The test source is excluded. The D2 measurement README identifies the measured baseline as `src/` at commit `5d451db`; the outputs here reproduce the supplied source snapshot.

The project state follows D2: the active team has **N = 2** and the GQM plan has **four questions**. The 50 person-hour comparison below is a retrospective estimate, not a time-sheet value.

## Reproduce

From the project root, with JDK 17 or newer and Python 3 with NumPy and Matplotlib installed:

```text
javac -d D3/out D3/D3Analyzer.java
java -cp D3/out D3Analyzer src D3/results
python3 D3/build_support.py
```

The analyzer uses the Java compiler tree API. Its logical SLOC categories match the D2 `StatementCounter.java` rules. The source-only pass runs before compiler attribution so compiler-inserted nodes are not counted. The sum of class L-SLOC is **1,143**, matching D2's `lsloc_no_imports` total exactly.

## Scope and metric rules

| Metric | Rule used |
|---|---|
| Cyclomatic complexity (CC) | Per declared method or constructor: baseline 1, plus each `if`, loop, `catch`, ternary, `&&`/`||` condition, and each non-default switch arm. Compiler-generated methods are excluded. |
| WMC | Sum of CC across each declared class's methods and constructors. The full method list is in `results/method_cc.csv`. |
| Coupling factor (CF) | System-level MOOD factor. One directed client→supplier pair is counted if source code references an in-scope type/member through a field/parameter/local/return type, member call, field access, or construction. Library types are excluded. Inheritance-based ancestor/descendant pairs are excluded. The denominator is `N(N−1)` for `N=52` declared types. |
| LCOM* | Henderson-Sellers form `(m − average(μ(Aj))) / (m − 1)`. `m` counts declared non-static instance methods, excluding constructors; `a` counts declared instance fields; `μ(Aj)` counts methods that directly access field `Aj`. N/A when `a=0` or `m<2`. Static members and indirect access through helper methods are excluded. |
| Logical SLOC | Same AST statement categories as the D2 counter; package/import lines are excluded. Type and method declarations, fields, locals, statements, branches, jumps, loops, and exception-handling nodes follow the D2 counting rules. |

The analyzer treats top-level, member, local, and anonymous types as separate declared types. This produces 52 types from 48 production Java files. CF is a system-level value; the `dependencies` column in `class_metrics.csv` gives each class's distinct outbound client pairs.

## P7/P8 results

- 217 declared methods and constructors; total CC and total WMC are both 332.
- Method CC: mean 1.53, median 1, maximum 9. Of 217 methods, 214 have CC ≤ 5; three are in the 6–10 band; none exceed 10.
- CF: 170 directed pairs / 2,652 possible pairs = **0.064103 (6.41%)**.
- LCOM*: computable for 19 of 52 types; mean 0.769, median 0.800, range 0.300–1.000.
- Per-class WMC, L-SLOC, LCOM*, and outbound dependencies are in `results/class_metrics.csv`.

## P9 estimation assumptions

### UCP

The use-case boundary follows the D1/D2 model: Cardholder, Bank core, Cash dispenser, and Receipt printer are actors; the latter three represent external service/device roles through simulated interfaces in this course product. The use-case table counts eight implemented user-facing flows. Receipt behavior is included in transaction flows and lockout in authentication, so neither is double-counted as a separate use case.

Actor weights are Cardholder = 3 (complex GUI actor), and each service/device actor = 1 (simple API/interface), giving UAW = 6. The transaction counts and use-case weights are explicit assumptions in `results/p9_use_case_points.csv`; they give UUCW = 65. With all technical and environmental factor ratings set to the neutral value 3 because the ZIP contains no rating record, TCF = 1.020 and ECF = 0.995. Adjusted UCP = `(6 + 65) × 1.020 × 0.995 = 72.06`.

The generic Karner productivity default of 20 person-hours/UCP yields 1,441 person-hours. The commonly cited 15–30 range is included as a sensitivity check, not as a project-calibrated forecast. No local productivity history is present in the ZIP.

### Basic COCOMO 81 and estimated D2 effort

The size proxy is D2 Logical SLOC excluding package/import lines: 1.143 KLOC. Organic mode is an explicit assumption for this small course simulator:

```text
Effort = 2.4 × KLOC^1.05 = 2.762 person-months
Schedule = 2.5 × Effort^0.38 = 3.678 months
```

Using 160 hours/person-month gives 442 person-hours. The ZIP has no time sheet. As authorized in the conversation, the comparison uses a **retrospective 50 person-hour estimate**: 33h implementation/integration, 11h testing/review/regression fixes, and 6h measurement/documentation. These allocations are estimates inferred from the D2 scope and role record, not recorded actual hours. Basic COCOMO is 8.84× this estimated effort. `results/p9_effort_estimates.csv` contains the calculations.

## P10

Each point in `results/p10_scatter_data.csv` is one of the 52 declared production types. Pearson correlation between class logical SLOC and WMC is **r = 0.9363** (`r² = 0.8767`). This describes a strong positive association within this source snapshot; it does not show that class size causes complexity or generalize to other systems.

## Output files

- `D3Analyzer.java` — reproducible Java parser and metric extractor.
- `build_support.py` — derived statistics, UCP/COCOMO tables, and charts.
- `results/method_cc.csv` — P7 per-method detail.
- `results/class_metrics.csv` and `results/class_dependencies.csv` — P8 per-type detail.
- `results/p9_*.csv` — UCP inputs, factor ratings, and effort comparisons.
- `results/p10_*.csv` and `results/p10_sloc_wmc_scatter.png` — P10 data and figure.
- `AI_Interaction_Log.md` — D3 AI use summary and CASTROFF record.

## References

1. McCabe, T. J. (1976). “A Complexity Measure.” *IEEE Transactions on Software Engineering*, SE-2(4), 308–320. DOI: 10.1109/TSE.1976.233837.
2. Chidamber, S. R., & Kemerer, C. F. (1994). “A Metrics Suite for Object-Oriented Design.” *IEEE Transactions on Software Engineering*, 20(6), 476–493. DOI: 10.1109/32.295895.
3. Brito e Abreu, F., Esteves, R., & Goulão, M. (1996). “The Design of Eiffel Programs: Quantitative Evaluation Using the MOOD Metrics.” *TOOLS’96 USA*.
4. Henderson-Sellers, B., Constantine, L. L., & Graham, I. M. (1996). “Coupling and Cohesion (towards a valid metrics suite for object-oriented analysis and design).” *Object Oriented Systems*, 3, 143–158.
5. Karner, G. (1993). *Resource Estimation for Objectory Projects*.
6. Boehm, B. W. (1981). *Software Engineering Economics*. Prentice-Hall.
