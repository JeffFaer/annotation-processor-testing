package name.falgout.jeffrey.testing.processor;

import java.util.Comparator;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.value.AutoAnnotation;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

public final class ExpectedDiagnostics {
  private static final Comparator<ExpectedDiagnostic<?>> EXPECTED_DIAGNOSTIC_LINE_ORDER =
      Comparator.comparing(ExpectedDiagnostic<?>::getExpectedLineNumber)
          .thenComparing(ed -> ed.getExpectDiagnostic().value())
          .thenComparing(ed -> ed.getExpectDiagnostic().testName());

  private ExpectedDiagnostics() {}

  public static List<ExpectedDiagnostic<?>> getExpectedDiagnostics(JavaFileObject file) {
    return getExpectedDiagnostics(Compiler.javac(), file);
  }

  public static List<ExpectedDiagnostic<?>> getExpectedDiagnostics(Compiler compiler,
      JavaFileObject file) {
    ExpectDiagnosticProcessor processor = new ExpectDiagnosticProcessor();
    Compilation compilation = compiler.withProcessors(processor).compile(file);

    List<ExpectedDiagnostic<?>> diagnostics = processor.getExpectedDiagnostics(compilation);
    diagnostics.sort(EXPECTED_DIAGNOSTIC_LINE_ORDER);
    return diagnostics;
  }

  static ExpectDiagnostic createExpectDiagnostic(ExpectError expectError) {
    return createExpectDiagnostic(Diagnostic.Kind.ERROR, expectError.value(), expectError.regex(),
        expectError.lineOffset(), expectError.testName());
  }

  @AutoAnnotation
  private static ExpectDiagnostic createExpectDiagnostic(Diagnostic.Kind value, String message,
      boolean regex, int lineOffset, String testName) {
    return new AutoAnnotation_ExpectedDiagnostics_createExpectDiagnostic(value, message, regex,
        lineOffset, testName);
  }
}
