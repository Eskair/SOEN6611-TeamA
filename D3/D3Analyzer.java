import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.*;

/**
 * Reproducible D3 source analyzer for the D2 production Java source.
 *
 * Rules: CC = 1 plus if/loop/catch/ternary/short-circuit decisions and each
 * non-default switch arm. WMC is the sum of CC for declared methods and
 * constructors. Class-level logical SLOC follows the D2 StatementCounter rules,
 * excluding package/import statements. LCOM* uses non-static declared methods
 * (constructors excluded) and non-static declared fields. CF counts directed
 * internal type/member references and calls, excluding inheritance.
 */
public final class D3Analyzer {
  private final Trees trees;
  private final Elements elements;
  private final Types types;
  private final SourcePositions positions;
  private final Map<String, ClassInfo> classes = new TreeMap<>();
  private final Set<String> couplingEdges = new TreeSet<>();
  private final Map<String, Integer> slocCategories = new TreeMap<>();
  private final Map<ClassTree, SourceClassMetrics> sourceClasses = new IdentityHashMap<>();
  private final Map<MethodTree, SourceMethodMetrics> sourceMethods = new IdentityHashMap<>();

  private D3Analyzer(Trees trees, Elements elements, Types types) {
    this.trees = trees;
    this.elements = elements;
    this.types = types;
    this.positions = trees.getSourcePositions();
  }

  private static final class ClassInfo {
    final String key;
    final String name;
    final String file;
    final TypeElement element;
    final int declarationLine;
    final Set<String> fields = new TreeSet<>();
    final List<MethodInfo> methods = new ArrayList<>();
    int logicalSloc;

    ClassInfo(String key, String name, String file, TypeElement element, int declarationLine) {
      this.key = key;
      this.name = name;
      this.file = file;
      this.element = element;
      this.declarationLine = declarationLine;
    }
  }

  private static final class MethodInfo {
    final ClassInfo owner;
    final String name;
    final int line;
    final boolean isStatic;
    final Set<String> accessedFields = new TreeSet<>();
    int cc = 1;

    MethodInfo(ClassInfo owner, String name, int line, boolean isStatic) {
      this.owner = owner;
      this.name = name;
      this.line = line;
      this.isStatic = isStatic;
    }
  }

  private static final class SourceClassMetrics {
    int logicalSloc;
  }

  private static final class SourceMethodMetrics {
    final String name;
    final int line;
    final boolean isStatic;
    int cc = 1;

    SourceMethodMetrics(String name, int line, boolean isStatic) {
      this.name = name;
      this.line = line;
      this.isStatic = isStatic;
    }
  }

  private void sourceSloc(SourceClassMetrics owner, String category) {
    if (owner == null) return;
    owner.logicalSloc++;
    slocCategories.merge(category, 1, Integer::sum);
  }

  private final class SourceMetricScanner extends TreePathScanner<Void, Void> {
    private SourceClassMetrics currentClass;
    private SourceMethodMetrics currentMethod;

    private void decision() {
      if (currentMethod != null) currentMethod.cc++;
    }

    @Override public Void visitClass(ClassTree tree, Void value) {
      SourceClassMetrics previous = currentClass;
      SourceMethodMetrics previousMethod = currentMethod;
      currentClass = new SourceClassMetrics();
      sourceClasses.put(tree, currentClass);
      currentMethod = null;
      sourceSloc(currentClass, "type declaration");
      super.visitClass(tree, value);
      currentClass = previous;
      currentMethod = previousMethod;
      return null;
    }

    @Override public Void visitMethod(MethodTree tree, Void value) {
      if (currentClass == null) return super.visitMethod(tree, value);
      SourceMethodMetrics previous = currentMethod;
      long start = positions.getStartPosition(getCurrentPath().getCompilationUnit(), tree);
      int line = (int) getCurrentPath().getCompilationUnit().getLineMap().getLineNumber(start);
      String name = tree.getName().contentEquals("<init>") ? "<init>" : tree.getName().toString();
      SourceMethodMetrics method = new SourceMethodMetrics(name, line,
          tree.getModifiers().getFlags().contains(Modifier.STATIC));
      sourceMethods.put(tree, method);
      sourceSloc(currentClass, "method/constructor header");
      currentMethod = method;
      super.visitMethod(tree, value);
      currentMethod = previous;
      return null;
    }

    @Override public Void visitVariable(VariableTree tree, Void value) {
      Tree parent = getCurrentPath().getParentPath().getLeaf();
      if (parent.getKind() == Tree.Kind.CLASS || parent.getKind() == Tree.Kind.ENUM
          || parent.getKind() == Tree.Kind.INTERFACE || parent.getKind() == Tree.Kind.RECORD) {
        sourceSloc(currentClass, "field/enum-constant declaration");
      } else if (parent.getKind() == Tree.Kind.BLOCK) {
        sourceSloc(currentClass, "local variable declaration");
      }
      return super.visitVariable(tree, value);
    }

    @Override public Void visitExpressionStatement(ExpressionStatementTree tree, Void value) {
      sourceSloc(currentClass, "expression statement");
      return super.visitExpressionStatement(tree, value);
    }

    @Override public Void visitIf(IfTree tree, Void value) {
      sourceSloc(currentClass, "selection (if / else if / else)");
      decision();
      if (tree.getElseStatement() != null && tree.getElseStatement().getKind() != Tree.Kind.IF) {
        sourceSloc(currentClass, "selection (if / else if / else)");
      }
      return super.visitIf(tree, value);
    }

    @Override public Void visitForLoop(ForLoopTree tree, Void value) {
      sourceSloc(currentClass, "iteration (for / while / do)");
      decision();
      scanDecisions(tree.getCondition());
      scan(tree.getStatement(), value);
      return null;
    }

    private void scanDecisions(Tree tree) {
      if (tree == null) return;
      new TreeScanner<Void, Void>() {
        @Override public Void visitBinary(BinaryTree node, Void ignored) {
          if (node.getKind() == Tree.Kind.CONDITIONAL_AND
              || node.getKind() == Tree.Kind.CONDITIONAL_OR) decision();
          return super.visitBinary(node, ignored);
        }

        @Override public Void visitConditionalExpression(ConditionalExpressionTree node, Void ignored) {
          decision();
          return super.visitConditionalExpression(node, ignored);
        }
      }.scan(tree, null);
    }

    @Override public Void visitEnhancedForLoop(EnhancedForLoopTree tree, Void value) {
      sourceSloc(currentClass, "iteration (for / while / do)");
      decision();
      scan(tree.getExpression(), value);
      scan(tree.getStatement(), value);
      return null;
    }

    @Override public Void visitWhileLoop(WhileLoopTree tree, Void value) {
      sourceSloc(currentClass, "iteration (for / while / do)");
      decision();
      return super.visitWhileLoop(tree, value);
    }

    @Override public Void visitDoWhileLoop(DoWhileLoopTree tree, Void value) {
      sourceSloc(currentClass, "iteration (for / while / do)");
      decision();
      return super.visitDoWhileLoop(tree, value);
    }

    @Override public Void visitSwitch(SwitchTree tree, Void value) {
      sourceSloc(currentClass, "selection (switch / case)");
      return super.visitSwitch(tree, value);
    }

    @Override public Void visitCase(CaseTree tree, Void value) {
      sourceSloc(currentClass, "selection (switch / case)");
      if (tree.getExpressions() != null && !tree.getExpressions().isEmpty()) decision();
      return super.visitCase(tree, value);
    }

    @Override public Void visitBinary(BinaryTree tree, Void value) {
      if (tree.getKind() == Tree.Kind.CONDITIONAL_AND
          || tree.getKind() == Tree.Kind.CONDITIONAL_OR) decision();
      return super.visitBinary(tree, value);
    }

    @Override public Void visitConditionalExpression(ConditionalExpressionTree tree, Void value) {
      decision();
      return super.visitConditionalExpression(tree, value);
    }

    @Override public Void visitReturn(ReturnTree tree, Void value) {
      sourceSloc(currentClass, "jump (return / break / continue / throw)");
      return super.visitReturn(tree, value);
    }

    @Override public Void visitBreak(BreakTree tree, Void value) {
      sourceSloc(currentClass, "jump (return / break / continue / throw)");
      return super.visitBreak(tree, value);
    }

    @Override public Void visitContinue(ContinueTree tree, Void value) {
      sourceSloc(currentClass, "jump (return / break / continue / throw)");
      return super.visitContinue(tree, value);
    }

    @Override public Void visitThrow(ThrowTree tree, Void value) {
      sourceSloc(currentClass, "jump (return / break / continue / throw)");
      return super.visitThrow(tree, value);
    }

    @Override public Void visitTry(TryTree tree, Void value) {
      sourceSloc(currentClass, "exception handling (try / catch / finally)");
      if (tree.getFinallyBlock() != null) {
        sourceSloc(currentClass, "exception handling (try / catch / finally)");
      }
      scan(tree.getBlock(), value);
      scan(tree.getCatches(), value);
      scan(tree.getFinallyBlock(), value);
      return null;
    }

    @Override public Void visitCatch(CatchTree tree, Void value) {
      sourceSloc(currentClass, "exception handling (try / catch / finally)");
      decision();
      scan(tree.getBlock(), value);
      return null;
    }
  }

  private String binaryName(TypeElement type) {
    return elements.getBinaryName(type).toString();
  }

  private String ownerKey(Element element) {
    Element e = element;
    while (e != null && !(e instanceof TypeElement)) e = e.getEnclosingElement();
    return e instanceof TypeElement ? binaryName((TypeElement) e) : null;
  }

  private void addCoupling(ClassInfo source, Element target) {
    if (source == null || target == null) return;
    String targetKey = ownerKey(target);
    // Keep forward references too; the complete source class set is known only
    // after every compilation unit has been scanned.
    if (targetKey == null || targetKey.equals(source.key)) return;
    couplingEdges.add(source.key + "\t" + targetKey);
  }

  private void addCouplingType(ClassInfo source, TypeMirror mirror) {
    addCouplingType(source, mirror, new HashSet<>());
  }

  private void addCouplingType(ClassInfo source, TypeMirror mirror, Set<String> visited) {
    if (source == null || mirror == null || !visited.add(mirror.toString())) return;
    switch (mirror.getKind()) {
      case DECLARED, ERROR -> {
        Element target = types.asElement(mirror);
        if (target instanceof TypeElement) addCoupling(source, target);
        if (mirror instanceof DeclaredType) {
          for (TypeMirror argument : ((DeclaredType) mirror).getTypeArguments()) {
            addCouplingType(source, argument, visited);
          }
        }
      }
      case ARRAY -> addCouplingType(source, ((ArrayType) mirror).getComponentType(), visited);
      case TYPEVAR -> {
        TypeVariable variable = (TypeVariable) mirror;
        addCouplingType(source, variable.getUpperBound(), visited);
        addCouplingType(source, variable.getLowerBound(), visited);
      }
      case WILDCARD -> {
        WildcardType wildcard = (WildcardType) mirror;
        addCouplingType(source, wildcard.getExtendsBound(), visited);
        addCouplingType(source, wildcard.getSuperBound(), visited);
      }
      case INTERSECTION -> {
        for (TypeMirror bound : ((IntersectionType) mirror).getBounds()) {
          addCouplingType(source, bound, visited);
        }
      }
      case UNION -> {
        for (TypeMirror alternative : ((UnionType) mirror).getAlternatives()) {
          addCouplingType(source, alternative, visited);
        }
      }
      default -> { }
    }
  }

  private static String safeName(TypeElement type, String key) {
    String simple = type.getSimpleName().toString();
    if (!simple.isEmpty()) return type.getQualifiedName().toString();
    return key.replace('$', '.') + " (anonymous)";
  }

  private final class Scanner extends TreePathScanner<Void, Void> {
    private ClassInfo currentClass;
    private MethodInfo currentMethod;
    private int anonymousNumber;

    @Override public Void visitCompilationUnit(CompilationUnitTree tree, Void value) {
      super.visitCompilationUnit(tree, value);
      return null;
    }

    @Override public Void visitClass(ClassTree tree, Void value) {
      Element symbol = trees.getElement(getCurrentPath());
      if (!(symbol instanceof TypeElement)) return super.visitClass(tree, value);
      TypeElement type = (TypeElement) symbol;
      String key = binaryName(type);
      String name = safeName(type, key);
      long classStart = positions.getStartPosition(getCurrentPath().getCompilationUnit(), tree);
      int declarationLine = (int) getCurrentPath().getCompilationUnit().getLineMap().getLineNumber(classStart);
      if (type.getSimpleName().length() == 0) {
        anonymousNumber++;
        name = currentClass == null ? key + " (anonymous)"
            : currentClass.name + ".anonymous" + anonymousNumber;
      }
      ClassInfo info = classes.get(key);
      if (info == null) {
        info = new ClassInfo(key, name, treePathFile(getCurrentPath()), type, declarationLine);
        classes.put(key, info);
      }
      ClassInfo previousClass = currentClass;
      MethodInfo previousMethod = currentMethod;
      currentClass = info;
      currentMethod = null;
      SourceClassMetrics source = sourceClasses.get(tree);
      info.logicalSloc = source == null ? 0 : source.logicalSloc;
      super.visitClass(tree, value);
      currentClass = previousClass;
      currentMethod = previousMethod;
      return null;
    }

    private String treePathFile(TreePath path) {
      return path.getCompilationUnit().getSourceFile().getName().replace('\\', '/');
    }

    @Override public Void visitMethod(MethodTree tree, Void value) {
      if (currentClass == null) return super.visitMethod(tree, value);
      SourceMethodMetrics source = sourceMethods.get(tree);
      if (source == null) return null; // Ignore constructors synthesized by javac.
      MethodInfo info = new MethodInfo(currentClass, source.name, source.line, source.isStatic);
      info.cc = source.cc;
      currentClass.methods.add(info);
      Element symbol = trees.getElement(getCurrentPath());
      if (symbol instanceof ExecutableElement) {
        ExecutableElement method = (ExecutableElement) symbol;
        addCouplingType(currentClass, method.getReturnType());
        for (VariableElement parameter : method.getParameters()) {
          addCouplingType(currentClass, parameter.asType());
        }
      }
      MethodInfo previous = currentMethod;
      currentMethod = info;
      super.visitMethod(tree, value);
      currentMethod = previous;
      return null;
    }

    private void countFieldUse(Element symbol) {
      if (!(symbol instanceof VariableElement) || currentClass == null) return;
      VariableElement variable = (VariableElement) symbol;
      if (variable.getKind() != ElementKind.FIELD) return;
      String fieldOwner = ownerKey(variable);
      if (fieldOwner == null) return;
      if (fieldOwner.equals(currentClass.key) && currentMethod != null
          && !variable.getModifiers().contains(Modifier.STATIC)) {
        currentMethod.accessedFields.add(variable.getSimpleName().toString());
      } else {
        addCoupling(currentClass, variable);
      }
    }

    @Override public Void visitIdentifier(IdentifierTree tree, Void value) {
      Element symbol = trees.getElement(getCurrentPath());
      countFieldUse(symbol);
      return super.visitIdentifier(tree, value);
    }

    @Override public Void visitMemberSelect(MemberSelectTree tree, Void value) {
      Element symbol = trees.getElement(getCurrentPath());
      if (symbol instanceof ExecutableElement) addCoupling(currentClass, symbol);
      countFieldUse(symbol);
      return super.visitMemberSelect(tree, value);
    }

    @Override public Void visitMethodInvocation(MethodInvocationTree tree, Void value) {
      Element symbol = trees.getElement(getCurrentPath());
      if (symbol instanceof ExecutableElement) addCoupling(currentClass, symbol);
      return super.visitMethodInvocation(tree, value);
    }

    @Override public Void visitNewClass(NewClassTree tree, Void value) {
      Element symbol = trees.getElement(getCurrentPath());
      if (symbol instanceof ExecutableElement) addCoupling(currentClass, symbol);
      return super.visitNewClass(tree, value);
    }

    @Override public Void visitVariable(VariableTree tree, Void value) {
      Tree parent = getCurrentPath().getParentPath().getLeaf();
      Element symbol = trees.getElement(getCurrentPath());
      if (symbol instanceof VariableElement) {
        addCouplingType(currentClass, ((VariableElement) symbol).asType());
      }
      if (parent.getKind() == Tree.Kind.CLASS || parent.getKind() == Tree.Kind.ENUM
          || parent.getKind() == Tree.Kind.INTERFACE || parent.getKind() == Tree.Kind.RECORD) {
        if (symbol instanceof VariableElement
            && !symbol.getModifiers().contains(Modifier.STATIC)
            && symbol.getKind() == ElementKind.FIELD) {
          currentClass.fields.add(symbol.getSimpleName().toString());
        }
      }
      return super.visitVariable(tree, value);
    }

  }

  private Set<String> inheritancePairs() {
    Set<String> pairs = new TreeSet<>();
    for (ClassInfo child : classes.values()) {
      if (child.element == null) continue;
      Deque<TypeMirror> pending = new ArrayDeque<>();
      TypeMirror parent = child.element.getSuperclass();
      if (parent != null) pending.add(parent);
      pending.addAll(child.element.getInterfaces());
      Set<String> seen = new HashSet<>();
      while (!pending.isEmpty()) {
        TypeMirror next = pending.removeFirst();
        Element e = types.asElement(next);
        if (!(e instanceof TypeElement)) continue;
        String key = binaryName((TypeElement) e);
        if (!classes.containsKey(key) || !seen.add(key)) continue;
        pairs.add(child.key + "\t" + key);
        TypeElement type = (TypeElement) e;
        if (type.getSuperclass() != null) pending.add(type.getSuperclass());
        pending.addAll(type.getInterfaces());
      }
    }
    return pairs;
  }

  private void write(Path out) throws Exception {
    Files.createDirectories(out);
    Set<String> inheritance = inheritancePairs();
    List<String> actualEdges = new ArrayList<>();
    for (String edge : couplingEdges) {
      String[] ab = edge.split("\t");
      if (!classes.containsKey(ab[0]) || !classes.containsKey(ab[1])) continue;
      if (inheritance.contains(edge) || inheritance.contains(ab[1] + "\t" + ab[0])) continue;
      actualEdges.add(edge);
    }
    int n = classes.size();
    // MOOD CF denominator: every ordered, non-self pair is a possible direct
    // coupling; inheritance-generated client relations are excluded above.
    int maxCouplings = n * (n - 1);
    double cf = maxCouplings == 0 ? 0 : (double) actualEdges.size() / maxCouplings;

    try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out.resolve("class_metrics.csv")))) {
      w.println("class,file,method_and_constructor_count,instance_method_count,instance_fields,wmc,logical_sloc,lcom_star,lcom_status,dependencies");
      for (ClassInfo c : classes.values()) {
        int wmc = 0;
        int nonStaticMethods = 0;
        Map<String, Integer> fieldUses = new HashMap<>();
        for (String field : c.fields) fieldUses.put(field, 0);
        for (MethodInfo m : c.methods) {
          wmc += m.cc;
          if (!m.isStatic && !m.name.equals("<init>")) {
            nonStaticMethods++;
          }
        }
        for (MethodInfo m : c.methods) {
          if (!m.isStatic && !m.name.equals("<init>")) {
            for (String field : m.accessedFields) fieldUses.merge(field, 1, Integer::sum);
          }
        }
        double lcom = Double.NaN;
        String status = "N/A: fewer than 2 methods";
        if (c.fields.isEmpty()) status = "N/A: no instance fields";
        else if (nonStaticMethods >= 2) {
          double average = fieldUses.values().stream().mapToInt(Integer::intValue).sum() / (double) c.fields.size();
          lcom = (nonStaticMethods - average) / (nonStaticMethods - 1.0);
          status = "computed";
        }
        long deps = actualEdges.stream().filter(e -> e.startsWith(c.key + "\t")).count();
        w.printf(Locale.ROOT,"%s,%s,%d,%d,%d,%d,%d,%s,%s,%d%n", csv(c.name),csv(c.file),
            c.methods.size(),nonStaticMethods,c.fields.size(),wmc,c.logicalSloc,
            Double.isNaN(lcom)?"":String.format(Locale.ROOT,"%.6f",lcom),status,deps);
      }
    }

    try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out.resolve("method_cc.csv")))) {
      w.println("class,file,method,line,cyclomatic_complexity,static,fields_accessed");
      for (ClassInfo c : classes.values()) {
        for (MethodInfo m : c.methods) {
          w.printf(Locale.ROOT,"%s,%s,%s,%d,%d,%s,%s%n",csv(c.name),csv(c.file),csv(m.name),m.line,m.cc,
              m.isStatic,m.accessedFields.size());
        }
      }
    }
    try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out.resolve("class_dependencies.csv")))) {
      w.println("client,server");
      for (String edge : actualEdges) {
        String[] ab = edge.split("\t");
        w.println(csv(classes.get(ab[0]).name) + "," + csv(classes.get(ab[1]).name));
      }
    }
    try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out.resolve("analysis_summary.txt")))) {
      int totalWmc=0, totalSloc=0, methodCount=0, nApplicable=0;
      for (ClassInfo c:classes.values()) {
        totalSloc += c.logicalSloc;
        for (MethodInfo m:c.methods) { totalWmc+=m.cc; methodCount++; }
        if (c.fields.size()>0 && c.methods.stream().filter(m->!m.isStatic && !m.name.equals("<init>")).count()>=2) nApplicable++;
      }
      w.println("classes="+n);
      w.println("methods_and_constructors="+methodCount);
      w.println("total_method_cc_and_wmc="+totalWmc);
      w.println("class_logical_sloc_excluding_package_import="+totalSloc);
      w.println("internal_directed_couplings="+actualEdges.size());
      w.println("inheritance_related_ordered_pairs="+inheritance.size());
      w.println("maximum_possible_couplings="+maxCouplings);
      w.println(String.format(Locale.ROOT,"CF=%.6f (%d/%d)%n",cf,actualEdges.size(),maxCouplings));
      w.println("classes_with_computable_LCOM_star="+nApplicable);
      for (Map.Entry<String,Integer> count : slocCategories.entrySet()) {
        w.println("sloc_category." + count.getKey() + "=" + count.getValue());
      }
    }
  }

  private static String csv(String s) {
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) throw new IllegalArgumentException("Usage: D3Analyzer <src-dir> <output-dir>");
    Path src = Path.of(args[0]);
    List<File> files;
    try (var stream = Files.walk(src)) {
      files = stream.filter(p -> p.toString().endsWith(".java")).sorted().map(Path::toFile).toList();
    }
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    try (StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, Locale.ROOT, null)) {
      Iterable<? extends JavaFileObject> units = fm.getJavaFileObjectsFromFiles(files);
      JavacTask task = (JavacTask) compiler.getTask(null, fm, diagnostics,
          List.of("-proc:none"), null, units);
      List<CompilationUnitTree> parsed = new ArrayList<>();
      for (CompilationUnitTree unit : task.parse()) parsed.add(unit);
      D3Analyzer analyzer = new D3Analyzer(Trees.instance(task), task.getElements(), task.getTypes());
      for (CompilationUnitTree unit : parsed) {
        analyzer.new SourceMetricScanner().scan(unit, null);
      }
      task.analyze();
      for (CompilationUnitTree unit : parsed) {
        analyzer.new Scanner().scan(unit, null);
      }
      analyzer.write(Path.of(args[1]));
      try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(Path.of(args[1], "javac_diagnostics.txt")))) {
        for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) w.println(d.toString());
      }
    }
  }
}
