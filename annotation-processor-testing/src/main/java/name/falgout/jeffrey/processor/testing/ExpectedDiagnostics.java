package name.falgout.jeffrey.processor.testing;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.value.AutoAnnotation;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

public final class ExpectedDiagnostics {
  private ExpectedDiagnostics() {}

  public static List<ExpectedDiagnostic> getExpectedDiagnostics(JavaFileObject file) {
    return getExpectedDiagnostics(Compiler.javac(), file);
  }

  public static List<ExpectedDiagnostic> getExpectedDiagnostics(Compiler compiler,
      JavaFileObject file) {
    ExpectDiagnosticProcessor processor = new ExpectDiagnosticProcessor();
    Compilation compilation = compiler.withProcessors(processor).compile(file);
    return processor.getExpectedDiagnostics(compilation);
  }

  static ExpectDiagnostic createExpectDiagnostic(ExpectError expectError) {
    return createExpectDiagnostic(Diagnostic.Kind.ERROR, expectError.value(), expectError.regex(),
        expectError.lineOffset());
  }

  @AutoAnnotation
  private static ExpectDiagnostic createExpectDiagnostic(Diagnostic.Kind kind,
      String message,
      boolean regex,
      int lineOffset) {
    return new AutoAnnotation_ExpectedDiagnostics_createExpectDiagnostic(kind, message, regex,
        lineOffset);
  }
}
