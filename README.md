# annotation-processor-testing

An easier way to determine if your annotation processor is creating the right diagnostics. Create a class with your annotations used incorrectly. Add [`@ExpectError`](annotation-processor-testing/src/main/java/name/falgout/jeffrey/testing/processor/ExpectError.java) to that class to ensure that your diagnostics are correct.
```java
@RunWith(DiagnosticTester.class)
@UseProcessor(YourProcessor.class)
public class YourProcessorTest {
  @ExpectError @IncorrectlyUsedAnnotation
  public void method() {}
  
  @ExpectError("substring of the message") @IncorrectlyUsedAnnotation
  public void method1() {}
  
  @ExpectError(lineOffset = 1) // Expect a diagnostic on the next line.
  @IncorrectlyUsedAnnotation
  public void method2() {}
  
  @ExpectError(value = "[abc]+", regex = true) @IncorrectlyUsedAnnotation
  public void method3() {}
  
  @ExpectError(testName = "descriptive JUnit test name") @IncorrectlyUsedAnnotation
  public void method4() {}
  
  // No @Expect annotation needed.
  @CorrectlyUsedAnnotation
  public void method5() {}
}
```

In order to use the JUnit4 runner, you must include your test sources as a resource:
```
  <build>
  ...
    <testResources>
      <testResource>
        <directory>src/test/java/</directory>
      </testResource>
      <testResource>
        <directory>src/test/resources/</directory>
      </testResource>
    </testResources>
    ...
  </build>
```
