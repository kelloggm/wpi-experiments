package org.checkerframework.wholeprograminference.inferredannoscounter;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InferredAnnosCounterTest {

  private final PrintStream standardOut = System.out;
  private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    System.setOut(new PrintStream(outputStreamCaptor));
  }

  @After
  public void tearDown() {
    System.setOut(standardOut);
  }

  @Test
  public void throwsRunTimeExceptionForNoInputFiles() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("Provide at least one .java file");
    InferredAnnosCounter.main(new String[] {});
  }

  @Test
  public void throwsRunTimeExceptionForInvalidInputFiles() {
    exception.expect(RuntimeException.class);
    exception.expectMessage(
        "Could not read file: " + "meaningless.java" + ". Check that it exists?");
    InferredAnnosCounter.main(new String[] {"meaningless.java", "testbca.ajava"});
  }

  @Test
  public void onlyOneJavaFile() {
    InferredAnnosCounter.main(new String[] {"testCases/OnlyOneJavaFile.java"});
    String line1 = "@Pure got 0/1";
    String line2 = "@NonNull got 0/1";
    String line3 = "@SideEffectFree got 0/1";
    assertTrue(outputStreamCaptor.toString().trim().contains(line1));
    assertTrue(outputStreamCaptor.toString().trim().contains(line2));
    assertTrue(outputStreamCaptor.toString().trim().contains(line3));
  }

  @Test
  public void matchThreeAnnotations() {
    InferredAnnosCounter.main(
        new String[] {
          "testCases/MatchThreeAnnotations.java", "testCases/MatchThreeAnnotations.ajava"
        });
    String line1 = "@Pure got 1/1";
    String line2 = "@NonNull got 1/1";
    String line3 = "@SideEffectFree got 1/1";
    assertTrue(outputStreamCaptor.toString().trim().contains(line1));
    assertTrue(outputStreamCaptor.toString().trim().contains(line2));
    assertTrue(outputStreamCaptor.toString().trim().contains(line3));
  }

  @Test
  public void smallTestForCollectionUtils() {
    InferredAnnosCounter.main(
        new String[] {
          "../inputExamples/RxNorm-explorer/human-written/src/java/gov/fda/nctr/util/CollectionUtils.java",
          "../inputExamples/RxNorm-explorer/generated/src/gov/fda/nctr/util/CollectionUtils-org.checkerframework.checker.nullness.NullnessChecker.ajava"
        });
    String line1 = "@NonNull got 0/10";
    assertTrue(outputStreamCaptor.toString().trim().contains(line1));
  }

  @Test
  public void commentInMiddle() {
    InferredAnnosCounter.main(
        new String[] {"testCases/CommentInMiddle.java", "testCases/CommentInMiddle.ajava"});
    String line1 = "@Pure got 1/1";
    String line2 = "@NonNull got 1/1";
    String line3 = "@SideEffectFree got 1/1";
    assertTrue(outputStreamCaptor.toString().trim().contains(line1));
    assertTrue(!outputStreamCaptor.toString().trim().contains(line2));
    assertTrue(outputStreamCaptor.toString().trim().contains(line3));
  }

  @Test
  public void annotationInString() {
    InferredAnnosCounter.main(
        new String[] {"testCases/AnnotationInString.java", "testCases/AnnotationInString.ajava"});
    String line1 = "@Pure got 1/1";
    String line2 = "@NonNull got 1/1";
    String line3 = "@SideEffectFree got 1/1";
    assertTrue(outputStreamCaptor.toString().trim().contains(line1));
    assertTrue(!outputStreamCaptor.toString().trim().contains(line2));
    assertTrue(outputStreamCaptor.toString().trim().contains(line3));
  }

  @Test
  public void annotationInMiddleOfADeclaration() {
    InferredAnnosCounter.main(
        new String[] {
          "testCases/AnnotationInMiddleOfADeclaration.java",
          "testCases/AnnotationInMiddleOfADeclaration.ajava"
        });
    String line1 = "@Pure got 1/1";
    String line2 = "@NonNull got 1/1";
    String line3 = "@SideEffectFree got 1/1";
    assertTrue(outputStreamCaptor.toString().trim().contains(line1));
    assertTrue(outputStreamCaptor.toString().trim().contains(line2));
    assertTrue(outputStreamCaptor.toString().trim().contains(line3));
  }

  @Test
  public void annotationWithArgument() {
    InferredAnnosCounter.main(
        new String[] {
          "testCases/AnnotationWithArgument.java", "testCases/AnnotationWithArgument.ajava"
        });
    String line1 = "@EnsuresNonNull got 1/2";
    String line2 = "@EnsuresCalledMethods got 1/2";
    assertTrue(
        "Didn't find the correct number of @EnsuresNonNull annotations; expected 1/2, got: "
            + outputStreamCaptor,
        outputStreamCaptor.toString().trim().contains(line1));
    assertTrue(
        "Didn't find the correct number of @EnsuresCalledMethods annotations; expected 1/2, got: "
            + outputStreamCaptor,
        outputStreamCaptor.toString().trim().contains(line2));
  }

  @Test
  public void dotInSubChecker() {
    InferredAnnosCounter.main(
        new String[] {"testCases/dotInSubChecker.java", "testCases/dotInSubChecker.ajava"});
    String line = "@Pure got 1/1";
    assertTrue(
        "Didn't find the correct number of @Pure annotations; expected 1/1, got: "
            + outputStreamCaptor,
        outputStreamCaptor.toString().trim().contains(line));
  }

  /**
   * This test is for annotations in string literals. As long as it does not throw out any
   * exception, this will be a passing test. Otherwise, if a NullPointerException is thrown, then
   * most likely we have forgotten to ignore annotations in string literals in
   * eachAnnotationInASingleLine
   */
  @Test
  public void TestValueExtractor() {
    InferredAnnosCounter.main(
        new String[] {"testCases/TestValueExtractor.java", "testCases/TestValueExtractor.ajava"});
  }

  @Test
  public void dotInParathense() {
    InferredAnnosCounter.main(
        new String[] {"testCases/dotInParathense.java", "testCases/dotInParathense.ajava"});
    String line = "@EnsuresCalledMethods got 1/1";
    assertTrue(
        "Didn't find the correct number of @EnsuresCalledMethods annotations; expected 1/1, got: "
            + outputStreamCaptor,
        outputStreamCaptor.toString().trim().contains(line));
  }

  @Test
  public void annotationMismatch() {
    InferredAnnosCounter.main(
        new String[] {"testCases/AnnotationMismatch.java", "testCases/AnnotationMismatch.ajava"});
    String line = "@NonNull got 0/1";
    String line2 = "@Nullable got 1/1";
    assertTrue("Annotation is mismatched", outputStreamCaptor.toString().trim().contains(line));
    assertTrue("Annotation is miscounted", outputStreamCaptor.toString().trim().contains(line2));
  }

  @Test
  public void commentWithDashStar() {
    InferredAnnosCounter.main(
        new String[] {"testCases/CommentWithDashStar.java", "testCases/CommentWithDashStar.ajava"});
    String line1 = "@Pure got 1/1";
    String line2 = "@NonNull got 1/1";
    String line3 = "@SideEffectFree got 1/1";
    assertTrue(outputStreamCaptor.toString().trim().contains(line1));
    assertTrue(!outputStreamCaptor.toString().trim().contains(line2));
    assertTrue(outputStreamCaptor.toString().trim().contains(line3));
  }

  @Test
  public void commentWithDoubleDash() {
    InferredAnnosCounter.main(
        new String[] {
          "testCases/CommentWithDoubleDash.java", "testCases/CommentWithDoubleDash.ajava"
        });
    String line1 = "@Pure got 1/1";
    String line2 = "@NonNull got 1/1";
    String line3 = "@SideEffectFree got 1/1";
    assertTrue(outputStreamCaptor.toString().trim().contains(line1));
    assertTrue(!outputStreamCaptor.toString().trim().contains(line2));
    assertTrue(outputStreamCaptor.toString().trim().contains(line3));
  }

  @Test
  public void commentWithStar() {
    InferredAnnosCounter.main(
        new String[] {"testCases/CommentWithStar.java", "testCases/CommentWithStar.ajava"});
    String line1 = "@Pure got 1/1";
    String line2 = "@NonNull got 1/1";
    String line3 = "@SideEffectFree got 1/1";
    assertTrue(outputStreamCaptor.toString().trim().contains(line1));
    assertTrue(!outputStreamCaptor.toString().trim().contains(line2));
    assertTrue(outputStreamCaptor.toString().trim().contains(line3));
  }

  @Test
  public void multiBlockOfComments() {
    InferredAnnosCounter.main(
        new String[] {
          "testCases/MultiBlockOfComments.java", "testCases/MultiBlockOfComments.ajava"
        });
    String line = "@NonNull got 0/1";
    assertTrue(outputStreamCaptor.toString().trim().contains(line));
  }

  @Test
  public void multiLineAnnotation() {
    InferredAnnosCounter.main(
        new String[] {"testCases/MultiLineAnnotation.java", "testCases/MultiLineAnnotation.ajava"});
    String line1 = "@EnsuresCalledMethods got 1/1";
    assertTrue(
        "Didn't find the correct number of @EnsuresCalledMethods annotations; expected 1/1, got: "
            + outputStreamCaptor,
        outputStreamCaptor.toString().trim().contains(line1));
  }

  @Test
  public void annotationsNotSurroundedBySpace() {
    InferredAnnosCounter.main(
        new String[] {
          "testCases/AnnotationNotSurroundedBySpace.java",
          "testCases/AnnotationNotSurroundedBySpace.ajava"
        });
    String line1 = "@NonNull got 0/3";
    assertTrue(
        "Didn't find the correct number of @NonNull annotations; expected 0/3, got: "
            + outputStreamCaptor,
        outputStreamCaptor.toString().trim().contains(line1));
  }

  @Test
  public void annotationWithinWarningSuppressions() {
    InferredAnnosCounter.main(
        new String[] {
          "testCases/AnnotationWithinWarningSuppression.java",
          "testCases/AnnotationWithinWarningSuppression.ajava"
        });
    assertTrue(
        "IAC should not count an annotation that is within a scope of SuppressWarnings "
            + outputStreamCaptor,
        outputStreamCaptor.toString().trim().isEmpty());
  }

  @Test
  public void blankSpaceBeforePackage() {
    InferredAnnosCounter.main(new String[] {"testCases/BlankSpaceBeforePackage.java"});
    // To ensure that the blank space before the package line has been removed, we can verify if the
    // annotation within the scope of SuppressWarnings is being correctly ignored.
    assertTrue(
        "InferredAnnosCounter is not working properly with Java files having blank space before the package line "
            + outputStreamCaptor,
        outputStreamCaptor.toString().trim().isEmpty());
  }

  @Test
  public void commentsBeforePackage() {
    InferredAnnosCounter.main(new String[] {"testCases/CommentsBeforePackage.java"});
    // To ensure that the comments before the package line have been removed, we can verify if the
    // annotation within the scope of  SuppressWarnings is being correctly ignored.
    assertTrue(
        "InferredAnnosCounter is not working properly with Java files having comments before the package line "
            + outputStreamCaptor,
        outputStreamCaptor.toString().trim().isEmpty());
  }

  @Test
  public void gJFMultiLine() {
    InferredAnnosCounter.main(
        new String[] {"testCases/GJFMultiLine.java", "testCases/GJFMultiLine.ajava"});
    String line1 = "@CalledMethods got 1/1";
    String line2 = "@NonNull got 1/1";
    assertTrue(
        "Didn't find the correct number of @CalledMethods annotations; expected 1/1, got: "
            + outputStreamCaptor,
        outputStreamCaptor.toString().trim().contains(line1));
    assertTrue(
        "Didn't find the correct number of @NonNull annotations; expected 1/1, got: "
            + outputStreamCaptor,
        outputStreamCaptor.toString().trim().contains(line2));
  }
}
