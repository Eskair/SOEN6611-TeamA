import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.File;
import java.util.*;
import javax.tools.*;

/**
 * Measurement tool for SOEN 6611 D2 (not part of iBank).
 *
 * <p>Uses the JDK's own Java parser to (1) count logical SLOC per file by statement type,
 * and (2) list every method/constructor with its start/end character offsets, so the
 * readability model can be applied per method.
 *
 * <p>Output lines (tab-separated):
 *   LSLOC  file  category  count
 *   METHOD file  class.method  startOffset  endOffset
 */
public class StatementCounter {

  public static void main(String[] args) throws Exception {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null);
    List<File> files = new ArrayList<>();
    for (String a : args) files.add(new File(a));
    JavacTask task = (JavacTask) compiler.getTask(null, fm, null, null, null,
        fm.getJavaFileObjectsFromFiles(files));
    SourcePositions pos = Trees.instance(task).getSourcePositions();
    for (CompilationUnitTree unit : task.parse()) {
      String file = unit.getSourceFile().getName();
      Counter c = new Counter(unit, pos);
      if (unit.getPackageName() != null) c.add("package/import");
      c.scan(unit, null);
      for (Map.Entry<String, Integer> e : c.counts.entrySet()) {
        System.out.println("LSLOC\t" + file + "\t" + e.getKey() + "\t" + e.getValue());
      }
      for (String m : c.methods) System.out.println("METHOD\t" + file + "\t" + m);
    }
  }

  static class Counter extends TreePathScanner<Void, Void> {
    final Map<String, Integer> counts = new TreeMap<>();
    final List<String> methods = new ArrayList<>();
    final CompilationUnitTree unit;
    final SourcePositions pos;
    final Deque<String> classNames = new ArrayDeque<>();

    Counter(CompilationUnitTree unit, SourcePositions pos) {
      this.unit = unit;
      this.pos = pos;
    }

    void add(String category) {
      counts.merge(category, 1, Integer::sum);
    }

    @Override public Void visitImport(ImportTree t, Void v) { add("package/import"); return null; }

    @Override public Void visitClass(ClassTree t, Void v) {
      add("type declaration");
      String name = t.getSimpleName().toString();
      classNames.push(name.isEmpty() ? "(anonymous)" : name);
      super.visitClass(t, v);
      classNames.pop();
      return null;
    }

    @Override public Void visitMethod(MethodTree t, Void v) {
      add("method/constructor header");
      String name = t.getName().contentEquals("<init>") ? classNames.peek() : t.getName().toString();
      methods.add(String.join(".", reverse(classNames)) + "." + name + "\t"
          + pos.getStartPosition(unit, t) + "\t" + pos.getEndPosition(unit, t));
      return super.visitMethod(t, v);
    }

    private static List<String> reverse(Deque<String> d) {
      List<String> l = new ArrayList<>(d);
      Collections.reverse(l);
      return l;
    }

    @Override public Void visitVariable(VariableTree t, Void v) {
      Tree.Kind parent = getCurrentPath().getParentPath().getLeaf().getKind();
      if (parent == Tree.Kind.CLASS || parent == Tree.Kind.ENUM || parent == Tree.Kind.INTERFACE) {
        add("field/enum-constant declaration");
      } else if (parent == Tree.Kind.BLOCK) {
        add("local variable declaration");
      } // parameters, lambda/catch/for-each variables are part of another statement
      return super.visitVariable(t, v);
    }

    @Override public Void visitExpressionStatement(ExpressionStatementTree t, Void v) {
      add("expression statement");
      return super.visitExpressionStatement(t, v);
    }

    @Override public Void visitIf(IfTree t, Void v) {
      add("selection (if / else if / else)");
      if (t.getElseStatement() != null && t.getElseStatement().getKind() != Tree.Kind.IF) {
        add("selection (if / else if / else)"); // a plain 'else'
      }
      return super.visitIf(t, v);
    }

    @Override public Void visitForLoop(ForLoopTree t, Void v) {
      add("iteration (for / while / do)");
      scan(t.getStatement(), v); // the header counts once; its parts are not separate statements
      return null;
    }

    @Override public Void visitEnhancedForLoop(EnhancedForLoopTree t, Void v) {
      add("iteration (for / while / do)");
      scan(t.getExpression(), v);
      scan(t.getStatement(), v);
      return null;
    }

    @Override public Void visitWhileLoop(WhileLoopTree t, Void v) { add("iteration (for / while / do)"); return super.visitWhileLoop(t, v); }
    @Override public Void visitDoWhileLoop(DoWhileLoopTree t, Void v) { add("iteration (for / while / do)"); return super.visitDoWhileLoop(t, v); }
    @Override public Void visitSwitch(SwitchTree t, Void v) { add("selection (switch / case)"); return super.visitSwitch(t, v); }
    @Override public Void visitCase(CaseTree t, Void v) { add("selection (switch / case)"); return super.visitCase(t, v); }
    @Override public Void visitReturn(ReturnTree t, Void v) { add("jump (return / break / continue / throw)"); return super.visitReturn(t, v); }
    @Override public Void visitBreak(BreakTree t, Void v) { add("jump (return / break / continue / throw)"); return super.visitBreak(t, v); }
    @Override public Void visitContinue(ContinueTree t, Void v) { add("jump (return / break / continue / throw)"); return super.visitContinue(t, v); }
    @Override public Void visitThrow(ThrowTree t, Void v) { add("jump (return / break / continue / throw)"); return super.visitThrow(t, v); }

    @Override public Void visitTry(TryTree t, Void v) {
      add("exception handling (try / catch / finally)");
      if (t.getFinallyBlock() != null) add("exception handling (try / catch / finally)");
      scan(t.getBlock(), v); // try-with-resources variables are part of the 'try'
      scan(t.getCatches(), v);
      scan(t.getFinallyBlock(), v);
      return null;
    }

    @Override public Void visitCatch(CatchTree t, Void v) {
      add("exception handling (try / catch / finally)");
      scan(t.getBlock(), v);
      return null;
    }
  }
}
