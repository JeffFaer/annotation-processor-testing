import name.falgout.jeffrey.processor.testing.ExpectError;

public class Expectations {
  @ExpectError(value = "foo", lineOffset = 5)
  public void foo() {}
}
