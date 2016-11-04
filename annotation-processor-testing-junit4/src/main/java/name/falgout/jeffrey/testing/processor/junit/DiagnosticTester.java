package name.falgout.jeffrey.testing.processor.junit;

import static name.falgout.jeffrey.testing.processor.ActualDiagnostics.getSourceFile;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.junit.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.model.InitializationError;

import name.falgout.jeffrey.testing.processor.ActualDiagnostics;
import name.falgout.jeffrey.testing.processor.ExpectedDiagnostic;
import name.falgout.jeffrey.testing.processor.ExpectedDiagnostics;

public final class DiagnosticTester extends Runner {
  private final List<ExpectedDiagnosticOrRunner> expectedDiagnostics;
  private final Queue<Diagnostic<? extends JavaFileObject>> actualDiagnostics;
  private final boolean isRoot;

  private final Description description;

  public DiagnosticTester(Class<?> testClass) throws InitializationError {
    this(testClass, getExpectedDiagnostics(testClass), getActualDiagnostics(testClass), true);
  }

  private static List<ExpectedDiagnostic<?>> getExpectedDiagnostics(Class<?> testClass)
      throws InitializationError {
    try {
      return ExpectedDiagnostics.getExpectedDiagnostics(getSourceFile(testClass));
    } catch (IllegalArgumentException e) {
      throw new InitializationError(
          e.getMessage() + ": The test directory should also be a testResource");
    }
  }

  private static Queue<Diagnostic<? extends JavaFileObject>> getActualDiagnostics(
      Class<?> testClass) throws InitializationError {
    try {
      return new LinkedList<>(ActualDiagnostics.getActualDiagnostics(testClass));
    } catch (InvocationTargetException e) {
      throw new InitializationError(e.getCause());
    } catch (Throwable e) {
      throw new InitializationError(e);
    }
  }

  private DiagnosticTester(Class<?> testClass, List<ExpectedDiagnostic<?>> expectedDiagnostics,
      Queue<Diagnostic<? extends JavaFileObject>> actualDiagnostics, boolean isRoot)
      throws InitializationError {
    this.expectedDiagnostics = new ArrayList<>();
    this.actualDiagnostics = actualDiagnostics;
    this.isRoot = isRoot;

    description = Description.createSuiteDescription(testClass);
    for (int i = 0; i < expectedDiagnostics.size(); i++) {
      ExpectedDiagnostic<?> diagnostic = expectedDiagnostics.get(i);
      if (testClass.getName().equals(diagnostic.getEnclosingClassName())) {
        Description childDescription = Description.createTestDescription(testClass,
            diagnostic.getExpectDiagnostic().testName());
        description.addChild(childDescription);

        this.expectedDiagnostics.add(new ExpectedDiagnosticOrRunner() {
          @Override
          public void run(RunNotifier notifier) {
            EachTestNotifier each = new EachTestNotifier(notifier, childDescription);
            each.fireTestStarted();
            try {
              assertThat(actualDiagnostics.poll(), diagnostic.asMatcher());
            } catch (StoppedByUserException e) {
              throw e;
            } catch (AssumptionViolatedException e) {
              each.addFailedAssumption(e);
            } catch (Throwable e) {
              each.addFailure(e);
            } finally {
              each.fireTestFinished();
            }
          }

          @Override
          public Description getDescription() {
            return childDescription;
          }
        });
      } else {
        int j = i + 1;
        for (; j < expectedDiagnostics.size() && !expectedDiagnostics.get(j)
            .getEnclosingClassName()
            .equals(testClass.getName()); j++) {}
        try {
          DiagnosticTester nestedRunner = new DiagnosticTester(diagnostic.getEnclosingClass(),
              expectedDiagnostics.subList(i, j), actualDiagnostics, false);
          description.addChild(nestedRunner.getDescription());
          this.expectedDiagnostics.add(new ExpectedDiagnosticOrRunner() {
            @Override
            public void run(RunNotifier notifier) {
              nestedRunner.run(notifier);
            }

            @Override
            public Description getDescription() {
              return nestedRunner.getDescription();
            }
          });
        } catch (ClassNotFoundException e) {
          throw new InitializationError(e);
        }

        i = j;
      }
    }
  }

  @Override
  public Description getDescription() {
    return description;
  }

  @Override
  public void run(RunNotifier notifier) {
    for (ExpectedDiagnosticOrRunner r : expectedDiagnostics) {
      r.run(notifier);
    }

    if (isRoot && !actualDiagnostics.isEmpty()) {
      notifier.fireTestFailure(new Failure(getDescription(), new AssertionError(
          actualDiagnostics.size() + " unmatched diagnostics: " + actualDiagnostics)));
    }
  }

  private interface ExpectedDiagnosticOrRunner {
    Description getDescription();

    void run(RunNotifier notifier);
  }
}
