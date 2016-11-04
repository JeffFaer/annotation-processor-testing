package name.falgout.jeffrey.testing.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

public final class ActualDiagnostics {
  private static final Comparator<Diagnostic<?>> ACTUAL_DIAGNOSTIC_LINE_ORDER =
      Comparator.comparing(Diagnostic<?>::getLineNumber).thenComparing(Diagnostic::getKind);

  private ActualDiagnostics() {}

  public static JavaFileObject getSourceFile(Class<?> clazz) throws IllegalArgumentException {
    String className = clazz.getName().replace('.', '/');
    return JavaFileObjects.forResource(className + ".java");
  }

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

  public static List<Diagnostic<? extends JavaFileObject>> getActualDiagnostics(Class<?> clazz)
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
      NoSuchMethodException {
    return getActualDiagnostics(Compiler.javac(), clazz);
  }

  public static List<Diagnostic<? extends JavaFileObject>> getActualDiagnostics(Compiler compiler,
      Class<?> clazz) throws InstantiationException, IllegalAccessException,
      InvocationTargetException, NoSuchMethodException {
    List<? extends Processor> processors = createProcessors(clazz);

    if (processors.isEmpty()) {
      throw new IllegalArgumentException(
          "Did you forget to annotate " + clazz.getSimpleName() + " with @UseProcessor?");
    }

    Compilation compilation =
        Compiler.javac().withProcessors(processors).compile(getSourceFile(clazz));
    List<Diagnostic<? extends JavaFileObject>> diagnostics =
        new ArrayList<>(compilation.diagnostics());
    diagnostics.sort(ACTUAL_DIAGNOSTIC_LINE_ORDER);
    return diagnostics;
  }
}
