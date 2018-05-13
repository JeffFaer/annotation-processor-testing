package name.falgout.jeffrey.testing.processor.tests;

import name.falgout.jeffrey.testing.processor.ExpectError;
import name.falgout.jeffrey.testing.processor.UseProcessor;

@UseProcessor(FakeProcessor.class)
public class NestedDiagnosticTestClass {
  @ExpectError(lineOffset = 1)
  @FakeProcessor.Error
  void method1() {}

  public static class Static {
     @ExpectError("a") @FakeProcessor.Error("a")
     void method1() {}

    public static class StaticStatic {
      @ExpectError(value = "b", lineOffset = 1)
      @FakeProcessor.Error("b")
      void method() {}
    }

    public class StaticInstance {
      @ExpectError("c") @FakeProcessor.Error("c")
      void method() {}
    }

    @ExpectError("d") @FakeProcessor.Error("d")
    void method2() {}
  }

  @ExpectError("e") @FakeProcessor.Error("e")
  void method2() {}

  public class Instance {
    @ExpectError("f") @FakeProcessor.Error("f")
    void method1() {}

    public class InstanceInstance {
      @ExpectError("g") @FakeProcessor.Error("g")
      void method() {}
    }

    @ExpectError("h") @FakeProcessor.Error("h")
    void method2() {}
  }
}
