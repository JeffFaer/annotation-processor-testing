package name.falgout.jeffrey.testing.processor.junit;

import org.junit.runner.RunWith;

import name.falgout.jeffrey.testing.processor.ExpectError;
import name.falgout.jeffrey.testing.processor.UseProcessor;

@RunWith(DiagnosticTester.class)
@UseProcessor(FakeProcessor.class)
public class DiagnosticTesterTestClass {
  @ExpectError(value = "foo", testName = "sameLine") @FakeProcessor.Error("foo")
  void method1() {}

  @ExpectError(value = "bar", lineOffset = 1, testName = "nextLine")
  @FakeProcessor.Error("bar")
  void method2() {}

  @ExpectError(value = "something-else", lineOffset = 1, testName = "wrongMessage")
  @FakeProcessor.Error
  void method3() {}

  @ExpectError(lineOffset = 1, testName = "wrongKind")
  @FakeProcessor.Warning
  void method4() {}

  @ExpectError(testName = "wrongLine")
  @FakeProcessor.Error
  void method5() {}

  @FakeProcessor.Error("extra diagnostic")
  void method6() {}
}
