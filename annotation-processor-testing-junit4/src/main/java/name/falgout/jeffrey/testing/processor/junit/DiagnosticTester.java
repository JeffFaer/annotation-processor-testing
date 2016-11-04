package name.falgout.jeffrey.testing.processor.junit;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import name.falgout.jeffrey.testing.processor.ActualDiagnostics;
import name.falgout.jeffrey.testing.processor.ExpectedDiagnostic;
import name.falgout.jeffrey.testing.processor.ExpectedDiagnostics;

public final class DiagnosticTester extends ParentRunner<ExpectedDiagnostic<?>> {
  private final JavaFileObject sourceFile;
  private final Queue<Diagnostic<? extends JavaFileObject>> diagnostics;

  public DiagnosticTester(Class<?> testClass) throws InitializationError {
    super(testClass);

    try {
      sourceFile = ActualDiagnostics.getSourceFile(testClass);
    } catch (IllegalArgumentException e) {
      throw new InitializationError(
          e.getMessage() + ": The test directory should also be a testResource");
    }

    try {
      diagnostics = new LinkedList<>(ActualDiagnostics.getActualDiagnostics(testClass));
    } catch (InvocationTargetException e) {
      throw new InitializationError(e.getCause());
    } catch (Throwable e) {
      throw new InitializationError(e);
    }
  }

  @Override
  protected List<ExpectedDiagnostic<?>> getChildren() {
    return ExpectedDiagnostics.getExpectedDiagnostics(sourceFile);
  }

  @Override
  protected Description describeChild(ExpectedDiagnostic<?> child) {
    return Description.createTestDescription(getTestClass().getJavaClass(),
        child.getExpectDiagnostic().testName());
  }

  @Override
  protected void runChild(ExpectedDiagnostic<?> child, RunNotifier notifier) {
    Description description = describeChild(child);
    notifier.fireTestStarted(description);
    try {
      assertThat(diagnostics.poll(), child.asMatcher());
    } catch (AssumptionViolatedException e) {
      notifier.fireTestAssumptionFailed(new Failure(description, e));
    } catch (Throwable e) {
      notifier.fireTestFailure(new Failure(description, e));
    } finally {
      notifier.fireTestFinished(description);
    }
  }

  @Override
  protected Statement withAfterClasses(Statement statement) {
    Statement superStatement = super.withAfterClasses(statement);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        superStatement.evaluate();

        if (!diagnostics.isEmpty()) {
          fail(diagnostics.size() + " unmatched diagnostics: " + diagnostics);
        }
      }
    };
  }
}
