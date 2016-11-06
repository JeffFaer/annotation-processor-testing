package name.falgout.jeffrey.testing.processor.junit;

import static name.falgout.jeffrey.testing.processor.ExpectedDiagnostics.getExpectedDiagnostics;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

public final class DiagnosticTester extends Runner {
  private final Class<?> testClass;
  private final List<ExpectedDiagnostic<?>> expectedDiagnostics;
  private final Description description;

  public DiagnosticTester(Class<?> testClass) throws InitializationError {
    this.testClass = testClass;
    expectedDiagnostics = getExpectedDiagnostics(getSourceFile(testClass));

    try {
      description = createDescription(testClass, expectedDiagnostics);
    } catch (ClassNotFoundException e) {
      throw new InitializationError(e);
    }
  }

  private static JavaFileObject getSourceFile(Class<?> testClass) throws InitializationError {
    try {
      return ActualDiagnostics.getSourceFile(testClass);
    } catch (IllegalArgumentException e) {
      throw new InitializationError(
          e.getMessage() + ": The test source should also be a testResource.");
    }
  }

  private static Description createDescription(Class<?> root,
      List<ExpectedDiagnostic<?>> expectedDiagnostics) throws ClassNotFoundException {
    Map<String, Description> suites = new LinkedHashMap<>();
    Description rootDescription = Description.createSuiteDescription(root);
    suites.put(root.getName(), rootDescription);

    for (ExpectedDiagnostic<?> expectedDiagnostic : expectedDiagnostics) {
      String enclosingClassName = expectedDiagnostic.getEnclosingClassName();
      Description suite =
          enclosingClassName == null ? rootDescription : createSuite(enclosingClassName, suites);
      suite.addChild(createDescription(expectedDiagnostic));
    }
    return rootDescription;
  }


  private static Description createDescription(ExpectedDiagnostic<?> expectedDiagnostic) {
    return Description.createTestDescription(expectedDiagnostic.getEnclosingClassName(),
        expectedDiagnostic.getExpectDiagnostic().testName());
  }

  private static Description createSuite(String className, Map<String, Description> suites)
      throws ClassNotFoundException {
    if (suites.containsKey(className)) {
      return suites.get(className);
    }

    Class<?> clazz = Class.forName(className);
    Description child = Description.createSuiteDescription(clazz.getSimpleName());
    suites.put(className, child);

    if (clazz.getEnclosingClass() != null) {
      Description parent = createSuite(clazz.getEnclosingClass().getName(), suites);
      parent.addChild(child);
    }

    return child;
  }

  @Override
  public Description getDescription() {
    return description;
  }

  @Override
  public void run(RunNotifier notifier) {
    Queue<Diagnostic<? extends JavaFileObject>> actualDiagnostics;
    try {
      actualDiagnostics = new LinkedList<>(ActualDiagnostics.getActualDiagnostics(testClass));
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
      notifier.fireTestFailure(new Failure(getDescription(), e));
      return;
    } catch (InvocationTargetException e) {
      notifier.fireTestFailure(new Failure(getDescription(), e.getCause()));
      return;
    }

    for (ExpectedDiagnostic<?> expected : expectedDiagnostics) {
      Description desc = createDescription(expected);
      EachTestNotifier each = new EachTestNotifier(notifier, desc);
      each.fireTestStarted();
      try {
        assertThat(actualDiagnostics.poll(), expected.asMatcher());
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

    if (!actualDiagnostics.isEmpty()) {
      notifier.fireTestFailure(new Failure(getDescription(), new AssertionError(
          actualDiagnostics.size() + " unmatched diagnostics: " + actualDiagnostics)));
    }
  }
}
