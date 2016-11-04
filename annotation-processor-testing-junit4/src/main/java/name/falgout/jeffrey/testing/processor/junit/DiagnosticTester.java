package name.falgout.jeffrey.testing.processor.junit;

import static name.falgout.jeffrey.testing.processor.ActualDiagnostics.getSourceFile;
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
  private final Map<ExpectedDiagnostic<?>, Description> descriptions;
  private final Description description;

  public DiagnosticTester(Class<?> testClass) throws InitializationError {
    try {
      this.testClass = testClass;
      expectedDiagnostics = getExpectedDiagnostics(getSourceFile(testClass));
      descriptions = new LinkedHashMap<>();
      description = createDescription(testClass, expectedDiagnostics, descriptions);
    } catch (IllegalArgumentException e) {
      throw new InitializationError(
          e.getMessage() + ": The test directory should also be a testResource");
    }
  }

  private Description createDescription(Class<?> root,
      List<ExpectedDiagnostic<?>> expectedDiagnostics,
      Map<ExpectedDiagnostic<?>, Description> descriptions) {
    Map<String, Description> suites = new LinkedHashMap<>();
    Description rootDescription = Description.createSuiteDescription(root);
    suites.put(root.getName(), rootDescription);

    for (ExpectedDiagnostic<?> expectedDiagnostic : expectedDiagnostics) {
      Description suite = suites.computeIfAbsent(expectedDiagnostic.getEnclosingClassName(),
          className -> createSuite(className, suites));
      Description leaf =
          Description.createTestDescription(expectedDiagnostic.getEnclosingClassName(),
              expectedDiagnostic.getExpectDiagnostic().testName());

      suite.addChild(leaf);
      descriptions.put(expectedDiagnostic, leaf);
    }
    return rootDescription;
  }

  private Description createSuite(String className, Map<String, Description> suites) {
    try {
      Class<?> clazz = Class.forName(className);
      Description returnValue = Description.createSuiteDescription(clazz);

      Description suite = returnValue;
      Class<?> enclosing = clazz.getEnclosingClass();
      while (!suites.containsKey(enclosing.getName())) {
        Description superSuite = Description.createSuiteDescription(enclosing);
        suites.put(enclosing.getName(), superSuite);
        superSuite.addChild(suite);

        suite = superSuite;
        enclosing = enclosing.getEnclosingClass();
      }

      suites.get(enclosing.getName()).addChild(suite);

      return returnValue;
    } catch (ClassNotFoundException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public Description getDescription() {
    return description;
  }

  @Override
  public void run(RunNotifier notifier) {
    try {
      Queue<Diagnostic<? extends JavaFileObject>> actualDiagnostics =
          new LinkedList<>(ActualDiagnostics.getActualDiagnostics(testClass));

      for (ExpectedDiagnostic<?> expected : expectedDiagnostics) {
        Description desc = descriptions.get(expected);
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
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
      notifier.fireTestFailure(new Failure(getDescription(), e));
    } catch (InvocationTargetException e) {
      notifier.fireTestFailure(new Failure(getDescription(), e.getCause()));
    }
  }
}
