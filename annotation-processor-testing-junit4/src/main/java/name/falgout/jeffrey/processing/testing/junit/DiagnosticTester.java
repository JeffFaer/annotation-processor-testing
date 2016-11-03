package name.falgout.jeffrey.processing.testing.junit;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;

import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

import name.falgout.jeffrey.processor.testing.ExpectedDiagnostic;
import name.falgout.jeffrey.processor.testing.ExpectedDiagnostics;

public final class DiagnosticTester extends ParentRunner<ExpectedDiagnostic> {
  private final JavaFileObject sourceFile;
  private final List<? extends Processor> processors;

  private final Object lock = new Object();
  private volatile Compilation compilation;

  public DiagnosticTester(Class<?> testClass) throws InitializationError {
    super(testClass);

    try {
      String className = testClass.getName().replace('.', '/');
      sourceFile = JavaFileObjects.forResource(className + ".java");
    } catch (IllegalArgumentException e) {
      throw new InitializationError(
          e.getMessage() + ": The test directory should also be a resource folder.");
    }

    try {
      processors = ExpectedDiagnostics.createProcessors(testClass);
    } catch (InvocationTargetException e) {
      throw new InitializationError(e.getCause());
    } catch (Throwable e) {
      throw new InitializationError(e);
    }

    if (processors.isEmpty()) {
      throw new InitializationError(
          "Did you forget to annotate " + testClass.getSimpleName() + " with @UseProcessor?");
    }
  }

  @Override
  protected List<ExpectedDiagnostic> getChildren() {
    return ExpectedDiagnostics.getExpectedDiagnostics(sourceFile);
  }

  @Override
  protected Description describeChild(ExpectedDiagnostic child) {
    return Description.createTestDescription(getTestClass().getJavaClass(), child.getTestName());
  }

  @Override
  protected void runChild(ExpectedDiagnostic child, RunNotifier notifier) {
    Description description = describeChild(child);
    notifier.fireTestStarted(description);
    try {
      if (compilation == null) {
        runCompilation();
      }

      child.checkCompilation(compilation);
    } catch (AssumptionViolatedException e) {
      notifier.fireTestAssumptionFailed(new Failure(description, e));
    } catch (Throwable e) {
      notifier.fireTestFailure(new Failure(description, e));
    } finally {
      notifier.fireTestFinished(description);
    }
  }

  private void runCompilation() {
    if (compilation != null) {
      return;
    }

    synchronized (lock) {
      if (compilation != null) {
        return;
      }

      compilation = getCompiler().compile(sourceFile);
    }
  }

  private Compiler getCompiler() {
    return Compiler.javac().withProcessors(processors);
  }
}
