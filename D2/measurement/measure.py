"""SOEN 6611 D2 measurement script (not part of iBank).

Usage: python3 measure.py <project folder> <parse.tsv from StatementCounter> <output folder>

Problem 5: physical SLOC per file (lines containing code that is not only a comment).
Problem 6: Posnett, Hindle & Devanbu (2011) readability, per method:
    z = 8.87 - 0.033*V + 0.40*Lines - 1.5*Entropy,  R = 1 / (1 + e^-z)
"""
import collections
import csv
import json
import math
import os
import re
import sys

KEYWORDS = set("""abstract assert boolean break byte case catch char class const continue default
do double else enum extends final finally float for goto if implements import instanceof int
interface long native new package private protected public return short static strictfp super
switch synchronized this throw throws transient try void volatile while var record yield""".split())
LITERAL_WORDS = {"true", "false", "null"}
OPERATORS = sorted(""">>>= <<= >>= >>> ... -> :: ++ -- && || == != <= >= += -= *= /= %= &= |= ^= << >>
+ - * / % = < > ! ~ ? : & | ^ ( ) { } [ ] ; , . @""".split(), key=len, reverse=True)
TOKEN_RE = re.compile(
    r'"(?:\\.|[^"\\])*"'           # string literal
    r"|'(?:\\.|[^'\\])*'"          # char literal
    r"|\d[\w.]*"                   # number
    r"|[A-Za-z_$][\w$]*"           # identifier or keyword
    r"|" + "|".join(re.escape(o) for o in OPERATORS))


def strip_comments(text):
    """Returns the text with comments replaced by spaces (newlines kept), honouring strings."""
    out, i, n = [], 0, len(text)
    while i < n:
        c = text[i]
        if text.startswith("//", i):
            while i < n and text[i] != "\n":
                out.append(" ")
                i += 1
        elif text.startswith("/*", i):
            end = text.find("*/", i + 2)
            end = n if end < 0 else end + 2
            out.append(re.sub(r"[^\n]", " ", text[i:end]))
            i = end
        elif c in "\"'":
            j = i + 1
            while j < n and text[j] != c:
                j += 2 if text[j] == "\\" else 1
            out.append(text[i:j + 1])
            i = j + 1
        else:
            out.append(c)
            i += 1
    return "".join(out)


def physical_counts(text):
    """Physical SLOC, comment-only lines and blank lines of one file."""
    code_lines = strip_comments(text).split("\n")
    raw_lines = text.split("\n")
    if raw_lines and raw_lines[-1] == "":
        raw_lines, code_lines = raw_lines[:-1], code_lines[:-1]
    sloc = blank = comment = 0
    for raw, code in zip(raw_lines, code_lines):
        if code.strip():
            sloc += 1
        elif raw.strip():
            comment += 1
        else:
            blank += 1
    return {"total": len(raw_lines), "sloc": sloc, "comment": comment, "blank": blank}


SEPARATORS = set("( ) { } [ ] ; , . @".split())


def halstead_volume(code, count_separators=False):
    """Halstead volume V = N * log2(n).

    Operands: identifiers (variables, fields, types) and literals.
    Operators: Java operators, keywords, and method/constructor names (as in Posnett et al.).
    Separators ( ) { } [ ] ; , . @ are counted only if count_separators is True.
    """
    tokens = TOKEN_RE.findall(strip_comments(code))
    operators, operands = [], []
    for k, tok in enumerate(tokens):
        nxt = tokens[k + 1] if k + 1 < len(tokens) else ""
        if tok in SEPARATORS and not count_separators:
            continue
        if tok[0] in "\"'" or tok[0].isdigit() or tok in LITERAL_WORDS:
            operands.append(tok)
        elif re.match(r"[A-Za-z_$]", tok):
            if tok in KEYWORDS or nxt == "(":
                operators.append(tok)  # keywords and method/constructor names
            else:
                operands.append(tok)
        else:
            operators.append(tok)
    big_n = len(operators) + len(operands)
    small_n = len(set(operators)) + len(set(operands))
    return big_n * math.log2(small_n) if small_n > 1 else 0.0, len(set(operators)), len(set(operands)), len(operators), len(operands)


def char_entropy(text):
    counts = collections.Counter(text)
    total = sum(counts.values())
    return -sum(c / total * math.log2(c / total) for c in counts.values())


def dedent(snippet, first_line_indent):
    lines = snippet.split("\n")
    out = [lines[0]]
    for line in lines[1:]:
        cut = min(first_line_indent, len(line) - len(line.lstrip(" ")))
        out.append(line[cut:])
    return "\n".join(out)


def posnett(volume, lines, entropy):
    z = 8.87 - 0.033 * volume + 0.40 * lines - 1.5 * entropy
    return z, 1 / (1 + math.exp(-z))


def readability(snippet):
    volume, n1, n2, big_n1, big_n2 = halstead_volume(snippet)
    lines = snippet.count("\n") + 1
    entropy = char_entropy(snippet)
    z, r = posnett(volume, lines, entropy)
    volume_sep = halstead_volume(snippet, count_separators=True)[0]
    r_sep = posnett(volume_sep, lines, entropy)[1]
    return {"V": volume, "lines": lines, "entropy": entropy, "z": z, "R": r,
            "n1": n1, "n2": n2, "N1": big_n1, "N2": big_n2,
            "V_with_separators": volume_sep, "R_with_separators": r_sep}


def main(project, parse_tsv, out_dir):
    os.makedirs(out_dir, exist_ok=True)
    logical = collections.defaultdict(lambda: collections.Counter())
    methods = []
    with open(parse_tsv) as f:
        for row in csv.reader(f, delimiter="\t"):
            if row[0] == "LSLOC":
                logical[(os.path.relpath(row[1], project) if os.path.isabs(row[1]) else row[1])][row[2]] += int(row[3])
            elif row[0] == "METHOD":
                methods.append(((os.path.relpath(row[1], project) if os.path.isabs(row[1]) else row[1]), row[2], int(row[3]), int(row[4])))

    files = []
    texts = {}
    for path in sorted(logical):
        text = open(os.path.join(project, path), encoding="utf-8").read()
        texts[path] = text
        pc = physical_counts(text)
        lsloc = sum(logical[path].values())
        imports = logical[path]["package/import"]
        files.append({"file": path, **pc, "lsloc": lsloc, "lsloc_no_imports": lsloc - imports})

    method_rows = []
    for path, name, start, end in methods:
        text = texts[path]
        line_start = text.rfind("\n", 0, start) + 1
        indent = start - line_start
        snippet = dedent(text[start:end], indent)
        r = readability(snippet)
        method_rows.append({"file": path, "method": name, **r})

    json.dump({"files": files, "methods": method_rows, "logical_by_type":
               {p: dict(c) for p, c in logical.items()}}, open(os.path.join(out_dir, "results.json"), "w"), indent=1)
    with open(os.path.join(out_dir, "p5_sloc_per_file.csv"), "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(files[0].keys()))
        w.writeheader()
        w.writerows(files)
    with open(os.path.join(out_dir, "p6_readability_per_method.csv"), "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=["file", "method", "lines", "n1", "n2", "N1", "N2", "V", "entropy", "z", "R", "V_with_separators", "R_with_separators"])
        w.writeheader()
        for m in method_rows:
            w.writerow({k: (round(v, 4) if isinstance(v, float) else v) for k, v in m.items() if k in w.fieldnames})


if __name__ == "__main__":
    main(*sys.argv[1:4])
