# D3 AI Interaction Log — Team A

**Date:** 2026-11-26  
**AI tool:** ChatGPT (GPT-6)  
**Scope:** D3 Problems 7–10 using only the provided SOEN6611-TeamA ZIP and course materials available in this conversation.

This is a concise summary of the D3 AI assistance in this conversation, not a verbatim transcript of every message. No Google Drive access was used.

## CASTROFF prompt summary

| CASTROFF element | Instruction used |
|---|---|
| Constraints | Use the actual D2 implementation in the ZIP; team size is N=2; exclude tests from production source metrics; clearly label any missing effort as an estimate. |
| Audience | SOEN 6611 instructor and teaching assistant. |
| Structure | Cover measurement scope, then P7 CC, P8 WMC/CF/LCOM*, P9 UCP/COCOMO vs estimated effort, P10 scatter/correlation, definitions, references, and evidence files. |
| Tone | Concise academic reporting, with explicit assumptions and limits. |
| Role | Software measurement analyst supporting the student team. |
| Output | Presentation plus reproducible CSV evidence and analysis scripts. |
| Focus | D2 source snapshot, documented formulas, reproducible calculations, and transparent effort estimation. |
| Function | Complete the D3 measurement deliverable. |

## Interaction summary by problem

| Problem | Prompt summary | AI contribution | Review and limitations |
|---|---|---|---|
| P7 | Measure method-level cyclomatic complexity from D2 production Java source using a stated decision-count rule. | Wrote and ran a Java compiler-tree analyzer; produced `method_cc.csv`, CC distribution, and summary statistics. | Analyzer output was checked against the D2 logical-SLOC counter; this does not mean the D2 test suite was rerun. |
| P8 | Calculate WMC, system CF, and per-class LCOM* with explicit applicability rules. | Added method/class metric extraction and dependency output; computed the formulas and generated CSV evidence. | CF definition and counting boundary are documented; class/type reference rules and direct-field LCOM* access rules are assumptions that should be read with the results. |
| P9 | Estimate UCP and Basic COCOMO 81 from D2 scope; fill the absent effort record with a transparent estimate. | Counted actors/use cases, applied neutral TCF/ECF ratings where the ZIP has no assessment, calculated COCOMO, and proposed 50 person-hours for comparison. | The use-case transaction counts, neutral factor ratings, organic mode, hours/person-month, and 50h figure are estimates/assumptions, not contemporaneous project records. |
| P10 | Plot per-type logical SLOC against WMC and calculate Pearson correlation. | Generated the scatter data, plot, regression line, and correlation summary from the P8 CSV. | Correlation applies only to this supplied source snapshot and is not causal. |

## AI-generated material

The AI drafted the metric analyzer and support script, calculated the tables and figures, and drafted the presentation and this log. The ZIP's application code under `src/` was not changed. The AI did not claim that the student team independently verified the assumptions or that the 50-hour estimate came from a time sheet.
