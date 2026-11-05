SOEN 6611 D2 - Problems 5 and 6: evidence and reproduction
Measured code: src/ at commit 5d451db (main after PR #1). Test code excluded.

Files
  p5_sloc_per_file.csv            physical / comment / blank / logical SLOC per file
  p6_readability_per_method.csv   Posnett et al. (2011) readability per method (217 methods)
  p5_sloc_by_package.png          chart for P5
  p6_readability_by_size.png      chart for P6
  p6_mindmap.png                  P6(a) metric-selection mind map
  StatementCounter.java           counts logical SLOC with the JDK's own Java parser; lists method positions
  measure.py                      physical SLOC + readability (Python 3 standard library only)

Reproduce (from the folder that contains src/):
  javac -d /tmp/tool StatementCounter.java
  java -cp /tmp/tool StatementCounter $(find src -name '*.java' | sort) > parse.tsv
  python3 measure.py . parse.tsv results
