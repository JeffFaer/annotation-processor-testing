package name.falgout.jeffrey.testing.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.value.AutoAnnotation;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

import name.falgout.jeffrey.testing.processor.AutoAnnotation_ExpectedDiagnostics_createExpectDiagnostic;

public final class ExpectedDiagnostics {
  private ExpectedDiagnostics() {}

  public static List<? extends Processor> createProcessors(Class<?> annotatedClass)
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
      NoSuchMethodException {
    List<Processor> processors = new ArrayList<>();
    for (UseProcessor useProcessor : annotatedClass.getAnnotationsByType(UseProcessor.class)) {
      for (Class<? extends Processor> processor : useProcessor.value()) {
        Constructor<? extends Processor> ctor = processor.getConstructor();
        if (Modifier.isPublic(ctor.getModifiers())) {
          ctor.setAccessible(true);
        }

        processors.add(processor.getConstructor().newInstance());
      }
    }

    return processors;
  }

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
        expectError.lineOffset(), expectError.testName());
  }

  @AutoAnnotation
  private static ExpectDiagnostic createExpectDiagnostic(Diagnostic.Kind value,
      String message,
      boolean regex,
      int lineOffset,
      String testName) {
    return new AutoAnnotation_ExpectedDiagnostics_createExpectDiagnostic(value, message, regex,
        lineOffset, testName);
  }
}
