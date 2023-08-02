package org.checkerframework.wholeprograminference.inferredannoscounter;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** This integration test checks that the IAC produces the expected outputs on ICalAvailable. */
public class ICalAvailableTest {

  private final PrintStream standardOut = System.out;
  private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

  @Before
  public void setUp() {
    System.setOut(new PrintStream(outputStreamCaptor));
  }

  @After
  public void tearDown() {
    System.setOut(standardOut);
  }

  private final String EXPECTED_OUTPUT =
      "@Pure got 1/1\n"
          + "@RequiresNonNull got 2/2\n"
          + "@Nullable got 0/1\n"
          + "@NonNull got 0/1\n"
          + "@MonotonicNonNull got 1/1\n"
          + "@Regex got 1/1\n"
          + "@EnsuresNonNull got 1/1";

  @Test
  public void iCalAvailableTest() {
    InferredAnnosCounter.main(
        new String[] {
          "../inputExamples/icalavailable/human-written/org/plumelib/icalavailable/ICalAvailable.java",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.index.IndexChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.signedness.SignednessChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.common.returnsreceiver.ReturnsReceiverChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.index.searchindex.SearchIndexChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.nullness.NullnessChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.formatter.FormatterChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.regex.RegexChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.lock.LockChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.index.substringindex.SubstringIndexChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.common.value.ValueChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.common.initializedfields.InitializedFieldsChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.signature.SignatureChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.index.inequality.LessThanChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.nullness.KeyForSubchecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.index.samelen.SameLenChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.interning.InterningChecker.ajava",
          "../inputExamples/icalavailable/generated/org/plumelib/icalavailable/ICalAvailable-org.checkerframework.checker.index.lowerbound.LowerBoundChecker.ajava"
        });

    assertTrue(
        "ICalAvailableTest got wrong output.\n"
            + "Expected: "
            + EXPECTED_OUTPUT
            + "\nActual: "
            + outputStreamCaptor.toString(),
        outputStreamCaptor
            .toString()
            .replaceAll("\\s", "")
            .contentEquals(EXPECTED_OUTPUT.replaceAll("\\s", "")));
  }
}
