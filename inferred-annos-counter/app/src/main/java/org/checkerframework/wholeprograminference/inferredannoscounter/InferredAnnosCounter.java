package org.checkerframework.wholeprograminference.inferredannoscounter;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.index.qual.IndexFor;
import org.checkerframework.framework.stub.RemoveAnnotationsForInference;

/**
 * The entry point for the inferred annos counter. To run the program, you need to pass arguments.
 * The first argument should be the path to the human-written Java file. Optionally, you can provide
 * one or more paths to the corresponding computer-generated .ajava files. The path here should be
 * relative to "experiments\inferred-annos-counter." InferredAnnosCounter only takes one
 * human-written source file at a time. So in case it is needed to run multiple human-written files
 * with corresponding computer-generated files, InferredAnnosCounter needs to be invoked multiple
 * times. The way to run InferredAnnosCounter is like this: cd experiments\inferred-annos-counter
 * (going to the working directory) and then gradle run --args = "(a path to the human-written file)
 * (optionally one or more paths to the computer-generated files)". The result will not be in
 * alphabetical order.
 */
public class InferredAnnosCounter {

  /**
   * This enum classifies input lines. A line is OPEN if it contains the beginning of a multi-line
   * annotation, CLOSE if it contains the ending of a multi-line annotation. For other cases, it is
   * considered COMPLETE.
   */
  enum LineStatus {
    OPEN,
    COMPLETE,
    CLOSE
  }

  /**
   * This method returns true if the first not-a-whitespace character of a line is a dot. It returns
   * false in other cases
   *
   * @param line a line to be analyzed
   * @return true of the first non-whitespace character of that line is a dot, false otherwise.
   */
  private static boolean firstIsDot(String line) {
    String trimmed = line.trim();
    return trimmed.length() == 0 ? false : trimmed.charAt(0) == '.';
  }

  /**
   * If an annotation is too long, Google Java Format will make it multi-line. This method checks if
   * a line contains the beginning of those annotations.
   *
   * <p>For example, the first line contains the beginning of MonotonicNonNull:
   * static @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker
   * .nullness.qual.MonotonicNonNull TimeZone tz2;
   *
   * @param line a line from the input file
   * @return true if it contains the beginning of a multi-line annotation
   */
  private static boolean checkGoogleFormatOpenCase(String line) {
    if (line.length() == 0) {
      return false;
    }
    String[] elements = line.trim().split(" ");
    int n = elements.length;
    if (n >= 1 && elements[n - 1].contains("@org")) {
      String annotation = elements[n - 1];
      annotation = trimParen(annotation);
      String[] breaks = annotation.split("[.]");
      int numberOfParts = breaks.length;
      if (numberOfParts < 2) {
        throw new RuntimeException("Invalid annotation form in line: " + line);
      }
      return (!breaks[numberOfParts - 2].equals("qual"));
    }
    return false;
  }

  /**
   * If an annotation is too long, Google Java Format will make it multi-line. This method checks if
   * a line contains the ending of those annotations.
   *
   * <p>For example, the second line contains the ending of MonotonicNonNull:
   * static @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker
   * .nullness.qual.MonotonicNonNull TimeZone tz2;
   *
   * @param line a line from the input file
   * @return true if it contains the ending of a multi-line annotation
   */
  private static boolean checkGoogleFormatCloseCase(String line) {
    return (line.length() > 0 && firstIsDot(line));
  }

  /**
   * This method checks the status of a line. If that line is the opening of a multi-line
   * annotation, we call it "Open". If the line is the closing of a multi-line annotation, we call
   * it "Close". And if the line does not contain any multi-line annotation, we call it "Complete".
   *
   * @param line a line from the input file
   * @return the status of that line
   */
  private static LineStatus checkLineStatus(String line) {
    if (checkGoogleFormatOpenCase(line)) {
      return LineStatus.OPEN;
    }
    if (checkGoogleFormatCloseCase(line)) {
      return LineStatus.CLOSE;
    }
    int openParen = 0;
    int closeParen = 0;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '(') {
        openParen++;
      }
      if (c == ')') {
        closeParen++;
      }
    }
    if (openParen < closeParen) {
      return LineStatus.CLOSE;
    }
    if (openParen > closeParen) {
      return LineStatus.OPEN;
    }
    return LineStatus.COMPLETE;
  }

  /**
   * This method reads the input files, and changes all the multi-line annotations into a single
   * line. This method returns a list, each element of that list is a line of the formatted file.
   *
   * @param fileContent a String containing all lines of the input file
   * @return inputFiles a list containing lines of the formatted file
   */
  private static List<String> annoMultiToSingle(String fileContent) {
    List<String> inputFiles = new ArrayList<>();
    String[] fileLines = fileContent.split("\n");
    String tempLine = "";
    boolean inProgress = false;
    for (String originalFileLine : fileLines) {
      LineStatus status = checkLineStatus(originalFileLine);
      if (inProgress) {
        if (status == LineStatus.CLOSE) {
          /*
          There are two cases that an anotation is multi-line, either by Google Java Format or by default.
          For the first case, it's easy to understand that we don't want any space in the middle of an annotation.
          For the second case, it will be easier for the Diff Algorithm if there's no space in the bracket.
           */
          tempLine = tempLine + originalFileLine.trim();
          inputFiles.add(tempLine);
          inProgress = false;
          tempLine = "";
        } else {
          originalFileLine = originalFileLine.trim();
          tempLine = tempLine + originalFileLine;
        }
      } else if (status == LineStatus.COMPLETE || !originalFileLine.contains("@")) {
        tempLine = tempLine + originalFileLine;
        inputFiles.add(tempLine);
        tempLine = "";
      } else if (status == LineStatus.OPEN && originalFileLine.contains("@")) {
        tempLine = tempLine + originalFileLine;
        inProgress = true;
      } else if (status == LineStatus.CLOSE) {
        // This line is irrelevant: it contains an entire annotation and also
        // happens to have unbalanced parentheses. This is fine and happens all
        // the time, but doesn't matter for our purposes.
        continue;
      } else {
        throw new RuntimeException(
            "unexpected line status: " + status + " for line " + originalFileLine);
      }
    }
    return inputFiles;
  }

  /**
   * This method will put each annotation in a separate line.
   *
   * @param inputFiles a list containing lines of the input file after going through
   *     quickReadAndFormat
   * @return a list containing lines of the input files with each annotation in a separate line.
   */
  private static List eachAnnotationInOneSingleLine(List<String> inputFiles) {
    List<String> formatted = new ArrayList<String>();
    for (int i = 0; i < inputFiles.size(); i++) {
      String line = inputFiles.get(i);
      String temp[] = line.split(" ");
      String resultLine = "";
      boolean inProgress = false;
      for (String element : temp) {
        int indexOfAnno = element.indexOf('@');
        int indexOfElement = line.indexOf(element);
        if (indexOfAnno != -1
            && !inProgress
            && indexOfElement != -1
            && indexOfElement < line.length()
            && notInStringLiteral(indexOfElement, line)) {
          if (resultLine.length() > 0) {
            // sometimes the annotation can be in the middle of a declaration
            formatted.add(resultLine + element.substring(0, indexOfAnno));
            element = element.substring(indexOfAnno);
          }
          resultLine = "";
          if (checkLineStatus(element) == LineStatus.COMPLETE) {
            formatted.add(element);
          } else {
            resultLine = resultLine + " " + element;
            inProgress = true;
          }
        } else {
          resultLine = resultLine + " " + element;
          if (inProgress && element.indexOf(')') != -1) {
            formatted.add(resultLine);
            inProgress = false;
            resultLine = "";
          }
        }
      }
      formatted.add(" " + resultLine);
    }
    return formatted;
  }

  /**
   * This method takes a line, which contains at least one annotation, and return the first
   * annotation in that line.
   *
   * @param line a non-empty line containing at least one annotation
   * @return the annotation which the line begins with
   */
  private static String getAnnos(String line) {
    String[] temp = line.split(" ");
    String result = "";
    for (String word : temp) {
      if (word.length() >= 1) {
        int indexOfAnno = word.indexOf('@');
        if (indexOfAnno != -1) {
          int begin = indexOfAnno + 1;
          result = word.substring(begin, word.length());
          break;
        }
      }
    }
    return result;
  }

  /**
   * This method counts the number of annotations in a line
   *
   * @param line a line
   * @return the number of annotations in that line
   */
  private static int countAnnos(String line) {
    int count = 0;
    boolean checkinString = true;
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) == '\"') {
        if (checkinString) {
          checkinString = false;
        } else {
          checkinString = true;
        }
      }
      if (line.charAt(i) == '@' && checkinString) {
        count++;
      }
    }
    return count;
  }

  /**
   * This method checks if a particular index in a line is not inside a string literal.
   *
   * <p>Assumes that the beginning of {@code line} is not already inside a string literal.
   *
   * @param line a line
   * @param index index to check
   * @return false if index is inside a string literal, true otherwise
   */
  private static boolean notInStringLiteral(@IndexFor("#2") int index, String line) {
    int before = 0;
    for (int i = 0; i < index; i++) {
      if (line.charAt(i) == '\"') {
        before++;
      }
    }
    if (before % 2 == 0) {
      return true;
    }
    return false;
  }

  /**
   * This method replaces a particular character at an index of a string with a new symbol.
   *
   * @param line a line that needs to be modified
   * @param index a valid index of that line, which is non-negative and less than the length of line
   * @param symbol the new character
   * @return the line with the character at index replaced by symbol
   */
  private static String replaceAtIndex(String line, @IndexFor("#1") int index, String symbol) {
    String result = line;
    String firstPart = line.substring(0, index);
    if (index + 1 < result.length()) {
      String secondPart = result.substring(index + 1);
      result = firstPart + symbol + secondPart;
    } else {
      // the element at index is also the last element of the string
      result = firstPart + symbol;
    }

    return result;
  }

  /**
   * This method formats annotations that contain arguments, such as
   * {@literal @}EnsuresNonNull("tz1").
   *
   * <p>These annotations are difficult to format, because they might or might not contain an extra
   * pair of curly braces. The computer-generated files sometimes put an additional pair of curly
   * braces (e.g., writing {@literal @}EnsuresNonNull({"tz1"}), which is valid: the argument is
   * technically an array, but Java permits single-value arrays to omit the curly braces).
   * Initially, we approached this problem by adding a pair of curly braces to every annotation
   * having a pair of parentheses but no curly braces. But then we might run into a problem if there
   * are parentheses inside the arguments.
   *
   * <p>This method instead has two parts. First, it checks if the annotation contains a matrix
   * (that is, a multi-dimensional array argument) and formats any found matrix by changing all "},
   * {" pattern to "|, |". Second, the method removes all curly braces in each annotation. This way,
   * we make sure that we will not mess up the important curly braces (i.e., those defining internal
   * array structure in a matrix), otherwise something like ({3, 4}) and ({3}, {4}) will both end up
   * being (3,4). This approach solves the problem described above by standardizing on a format with
   * no curly braces at all.
   *
   * @param annotation an annotation to be formatted
   * @return formatted annotation
   */
  private static String formatAnnotaionsWithArguments(String annotation) {
    // remove all whitespace inside of annotations with arguments, to prevent
    // whitespace-based diffs from producing incorrect results later
    String result = annotation.replaceAll("\\s+", "");
    /*
    First, we format cases involving matrix by changing all "}, {" to "|, |"
     */
    int indexOfClose = result.indexOf("},");
    while (indexOfClose != -1) {
      int indexOfOpen = result.indexOf('{', indexOfClose);
      // reaching the end of a line
      if (indexOfOpen < 0) {
        return result;
      }
      // When an annotation has multiple arguments, a }, .* { can occur
      // because of the argument names. In those cases, just continue the
      // loop.
      if (result
          .substring(indexOfClose, indexOfOpen)
          .chars()
          .anyMatch(c -> !(c == '{' || c == '}' || c == ','))) {
        indexOfClose = result.indexOf("},", indexOfClose + 1);
        continue;
      }

      if (notInStringLiteral(indexOfClose, result)) {
        result = replaceAtIndex(result, indexOfClose, "|");
        result = replaceAtIndex(result, indexOfOpen, "|");
        indexOfClose = result.indexOf("},");
      } else {
        indexOfClose = result.indexOf("},", indexOfClose + 1);
      }
    }
    /*
    Second, we will remove all curly braces
     */
    result = result.replace("{", "");
    result = result.replace("}", "");
    return result;
  }

  /**
   * This method trims out all the comments in all lines from an input file
   *
   * @param filePath the absolute path of the file to be trimmed out comments
   * @return the content of the file without comments
   */
  private static String ignoreComment(String filePath) {
    try {
      StaticJavaParser.getParserConfiguration().setAttributeComments(false);
      CompilationUnit cu = StaticJavaParser.parse(new File(filePath));
      return cu.toString();
    } catch (Exception e) {
      throw new RuntimeException(
          "Could not read file: " + filePath + ". Check that it exists?" + e.getMessage());
    }
  }

  /**
   * This method formats a line that may or may not contain fully-qualified annotation. The way this
   * method formats that line is to change all annotations written in the fully-qualified format to
   * the simple format. For example, changing "@org.checkerframework.dataflow.qual.Pure" to "@Pure."
   * This method should be applied before passing a line to the Diff algorithm.
   *
   * @param line a line that belongs to the input files
   * @return the same line with all the annotations being changed to the simple format.
   */
  private static String extractCheckerPackage(String line) {
    String[] temp = line.split(" ");
    String result = line;
    if (line.length() != 0) {
      for (String word : temp) {
        int indexOfPackage = word.indexOf("org.");
        if (indexOfPackage != -1) {
          int indexOfParen = word.indexOf('(');
          if (indexOfParen != -1) {
            String insideParen = word.substring(indexOfParen + 1, word.length());
            if (insideParen.contains("org.")) {
              String newInsideParen = extractCheckerPackage(insideParen);
              String newWord = word.replace(insideParen, newInsideParen);
              result = result.replace(word, newWord);
              word = newWord;
            }
          }
          String originalPart = word.substring(indexOfPackage, word.length());
          originalPart = trimParen(originalPart);
          String[] tempo = originalPart.split("[.]");
          String tempResult = tempo[tempo.length - 1];
          String newWord = word.replace(originalPart, tempResult);
          result = result.replace(word, newWord);
        }
      }
    }
    return result;
  }

  /**
   * This method trims out the parenthesized part in an annotation, for example, @Annotation(abc)
   * will be changed to @Annotation.
   *
   * <p>This method needs to be used with care. We want to use it to update the final result. This
   * method should not be used for any list or string that will become the input of the Diff
   * algorithm. If we do that, the Diff algorithm will not be able to recognize any potential
   * difference in the parentheses between an annotation written by human and an annotation
   * generated by the computer anymore.
   *
   * @param anno the annotation which will be trimmed
   * @return that annotation without the parenthesized part
   */
  private static String trimParen(String anno) {
    int para = anno.indexOf("(");
    if (para == -1) {
      return anno;
    }
    return anno.substring(0, para);
  }

  /**
   * This method returns a List containing all the annotations belonging to a line (with the
   * {@literal @} symbol stripped off each annotation).
   *
   * @param str a line
   * @return a Linked List containing all annotations of str.
   */
  private static List extractString(String str) {
    List<String> result = new ArrayList<String>();
    int countAnno = countAnnos(str);
    String temp = str;
    for (int i = 0; i < countAnno; i++) {
      int index1 = temp.indexOf('@');
      if (index1 == -1) {
        throw new RuntimeException(
            "The extractString method relies on the countAnnos method. Either the countAnnos method is wrong"
                + "or it was not called properly");
      }
      String tempAnno = getAnnos(temp);
      if (notInStringLiteral(index1, temp)) {
        if (tempAnno.contains("(")) {
          if (temp.contains(")")) {
            tempAnno = temp.substring(index1 + 1, temp.indexOf(')') + 1);
          } else {
            tempAnno = temp.substring(index1 + 1);
          }
          result.add(tempAnno);
        } else {
          result.add(tempAnno);
        }
      }
      temp = temp.substring(index1 + 1);
    }
    return result;
  }

  /**
   * Given an input file, this method returns a Map of annotations that should be ignored in that
   * file and their corresponding quantity. The main reason we ignore some annotations is that they
   * are within a scope of SuprressWarnings.
   *
   * @param filePath the path of the input file
   * @return a Map with the name of the annotation to ignore as key and the number of that
   *     annotation in the file as value
   */
  public static Map<String, Integer> returnListOfAnnosToIgnore(String filePath) {
    String tempDirectoryFile = "";
    Map<String, Integer> listOfAnnoToIgnore = new HashMap<>();
    try {
      Path file = Paths.get(filePath);
      Path tempDir = Files.createTempDirectory("mytemp");
      Path tempFile = tempDir.resolve(file.getFileName());
      Files.copy(file, tempFile);
      removePossiblePackage(tempFile.toString());
      RemoveAnnotationsForInference.main(new String[] {tempDir.toString()});
      tempDirectoryFile = tempFile.toString();
    } catch (IOException e) {
      throw new RuntimeException("Could not read file: " + filePath + ". Check that it exists?");
    }
    List<String> tempFileWithOnlySingleLineAnno =
        annoMultiToSingle(ignoreComment(tempDirectoryFile));
    List<String> tempFileWithEachAnnotationOnASingleLine =
        eachAnnotationInOneSingleLine(tempFileWithOnlySingleLineAnno);
    for (String tempFileLine : tempFileWithEachAnnotationOnASingleLine) {
      tempFileLine = extractCheckerPackage(tempFileLine);
      // this line is an annotation. So we need to clear all preceding and succeeding space.
      tempFileLine = tempFileLine.trim();
      String specialAnno = trimParen(tempFileLine);
      // the fact that this if statement's condition is true means that this line contains exactly
      // one CF annotation and nothing else.
      if (tempFileLine.indexOf('@') == 0) {
        int numberOfAnno = listOfAnnoToIgnore.getOrDefault(specialAnno, 0);
        listOfAnnoToIgnore.put(specialAnno, numberOfAnno + 1);
      }
    }
    return listOfAnnoToIgnore;
  }

  /**
   * This method removes the first line of a Java file if that line declares the package of the
   * file. The reason we do this is that RemoveAnnotationsForInference requires the file to be in a
   * directory with the exact structure as the package line declares. To achieve that is too
   * difficult, so we simply remove that line to make the file looks like it belongs to no package.
   * For InferredAnnosCounter, we only take one Java file into consideration at a time, so this
   * method will not make the program go wrong because of two Java files with the same name but in
   * two different package
   *
   * @param filePath the path of the file to remove possible package line
   */
  public static void removePossiblePackage(String filePath) {
    // fileContents will have no comments or blank space at the beginning
    String fileContents = ignoreComment(filePath);
    String[] fileLines = fileContents.split("\n");
    String firstLine = fileLines[0];
    if (!firstLine.contains("package")) {
      return;
    }
    fileContents = fileContents.replace(firstLine, "");
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.append(fileContents);
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  /**
   * Return true if there is a mismatched annotation between the pair of input deltas. The way we
   * check is that, if at least one of the deltas has the CHANGE type and none of them contains only
   * annotations, we claim that there is a mismatched annotation between them. Note that the two
   * deltas must be consecutive.
   *
   * @param thisDelta the first delta
   * @param nextDelta the second delta
   * @return true if there is a mismatch between them
   */
  public static boolean hasMismatchAnnotationInTheMiddle(
      AbstractDelta<String> thisDelta, AbstractDelta<String> nextDelta) {
    if (thisDelta.getType() != DeltaType.CHANGE && nextDelta.getType() != DeltaType.CHANGE) {
      return false;
    }
    if (containsOnlyAnnotations(thisDelta) || containsOnlyAnnotations(nextDelta)) {
      return false;
    }
    return true;
  }

  /**
   * This method checks if a delta contains nothing but annotations
   *
   * @param delta
   * @return true if the delta contains only annotations
   */
  public static boolean containsOnlyAnnotations(AbstractDelta<String> delta) {
    List<String> currentSourceLines = delta.getSource().getLines();
    for (String line : currentSourceLines) {
      if (line.length() == 0) {
        return false;
      }
      if (line.charAt(0) != '@') {
        return false;
      }
    }
    return true;
  }

  /**
   * The main entry point. Running this outputs the percentage of annotations in some source file
   * that were inferred by WPI.
   *
   * <p>-param args the files. The first element is the original source file. All remaining elements
   * should be corresponding .ajava files produced by WPI. This program assumes that all inputs have
   * been converted to some unified formatting style to eliminate unnecessary changes (e.g., by
   * running google java format on each input).
   */
  public static void main(String[] args) {
    int fileCount = 0;
    List<String> checkerPackage = new ArrayList<String>();
    File file1 = new File("type-qualifiers.txt");
    try (FileReader fr = new FileReader(file1)) {
      BufferedReader br = new BufferedReader(fr);
      String str;
      while ((str = br.readLine()) != null) {
        // the extractCheckerPackage will keep the char element of the string, such as '@' or '"'.
        // So we need to add a
        // space here since the element in this txt does not have a '@'.
        str = extractCheckerPackage('@' + str);
        str = str.replaceAll("\\s", "");
        checkerPackage.add(str);
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not read type-qualifiers.txt, check if it exists?");
    }

    if (args.length < 1) {
      throw new RuntimeException("Provide at least one .java file");
    }

    // These variables are maintained throughout:

    // The original file, reformatted to remove comments and clean up annotation names (i.e., remove
    // package names),
    // etc.
    List<String> originalFile = new ArrayList<>();
    // specific annotations and the number of computer-written files missing them
    Map<String, Integer> annoLocate = new HashMap<>();
    // the name of the types of annotation and their numbers in the human-written file
    Map<String, Integer> annoCount = new HashMap<>();
    /* the name of the types of annotations and their "correct" numbers (meaning the number of annotations of that
    type not missed by computer-written files) */
    Map<String, Integer> annoSimilar = new HashMap<>();
    Map<String, Integer> listOfAnnoToIgnore = returnListOfAnnosToIgnore(args[0]);
    // the number of lines in the original file
    int originalFileLineCount = 0;
    List<String> inputFileWithOnlySingleLineAnno = annoMultiToSingle(ignoreComment(args[0]));
    List<String> inputFileWithEachAnnoOnOneLine =
        eachAnnotationInOneSingleLine(inputFileWithOnlySingleLineAnno);
    int originalFileLineIndex = 0;
    // Read the original file once to determine the annotations that written by the human.
    for (String originalFileLine : inputFileWithEachAnnoOnOneLine) {
      originalFileLine = extractCheckerPackage(originalFileLine);
      // since it's too difficult to keep the length of whitespace at the beginning of each line the
      // same
      originalFileLine = originalFileLine.trim();
      String specialAnno = trimParen(originalFileLine);
      // the fact that this if statement's condition is true means that this line contains exactly
      // one CF annotation and nothing else.
      if (checkerPackage.contains(specialAnno)) {
        originalFileLine = formatAnnotaionsWithArguments(originalFileLine);
        int numberOfAnno = annoCount.getOrDefault(specialAnno, 0);
        annoCount.put(specialAnno, numberOfAnno + 1);
        annoSimilar.put(specialAnno, 0);
        // we want the keys in the map annoLocate has this following format: type_position
        annoLocate.put(originalFileLine + "_" + originalFileLineIndex, 0);
      }
      if (originalFileLine.length() != 0) {
        originalFile.add(originalFileLine);
        originalFileLineIndex++;
      }
      originalFileLineCount = originalFileLineIndex;
    }
    // Iterate over the arguments from 1 to the end and diff each with the original,
    // putting the results into diffs.
    List<Patch<String>> diffs = new ArrayList<>(args.length - 1);
    // Iterate over the arguments from 1 to the end and diff each with the original,
    // putting the results into diffs.
    for (int i = 1; i < args.length; ++i) {
      List<String> inputFileWithOnlySingleLineAnno2 = annoMultiToSingle(ignoreComment(args[i]));
      List<String> inputFileWithEachAnnoOnOneLine2 =
          eachAnnotationInOneSingleLine(inputFileWithOnlySingleLineAnno2);
      List<String> newFile = new ArrayList<>();
      for (String ajavaFileLine : inputFileWithEachAnnoOnOneLine2) {
        // if the condition is true, this line contains only one single annotation and nothing else.
        if (ajavaFileLine.contains("@org")) {
          ajavaFileLine = formatAnnotaionsWithArguments(ajavaFileLine);
        }
        ajavaFileLine = extractCheckerPackage(ajavaFileLine);
        ajavaFileLine = ajavaFileLine.trim();
        if (ajavaFileLine.length() != 0) {
          newFile.add(ajavaFileLine);
        }
      }
      diffs.add(DiffUtils.diff(originalFile, newFile));
    }
    // Iterate over the list of diffs and process each. There must be args.length - 1 diffs, since
    // there is one diff between args[0] and each other element of args.
    for (int i = 0; i < args.length - 1; i++) {
      Patch<String> patch = diffs.get(i);
      List<AbstractDelta<String>> listOfDelta = patch.getDeltas();
      for (int currPointer = 0; currPointer < listOfDelta.size(); currPointer++) {
        AbstractDelta<String> delta = listOfDelta.get(currPointer);
        List<String> sourceLines = delta.getSource().getLines();
        int nextPointer = currPointer + 1;
        // if there are two consecutive deltas that are originally one single line before the
        // eachAnnotationInOneSingleLine is applied, then the annotation between those two lines is
        // mismatched
        if (nextPointer < listOfDelta.size()) {
          AbstractDelta<String> nextDelta = listOfDelta.get(nextPointer);
          if (hasMismatchAnnotationInTheMiddle(delta, nextDelta)) {
            int indexOfMismatched = delta.getSource().getPosition() + sourceLines.size();
            String mismatchName = originalFile.get(indexOfMismatched);
            if (mismatchName.indexOf("@") == 0) {
              String mismatchFullForm = mismatchName + "_" + indexOfMismatched;
              int value = annoLocate.get(mismatchFullForm);
              annoLocate.put(mismatchFullForm, value + 1);
            }
          }
        }
        // get the position of the first line entry in the delta
        int position = delta.getSource().getPosition();
        String result = "";
        for (int j = 0; j < sourceLines.size(); j++) {
          String element = sourceLines.get(j);
          if (element.contains("@")) {
            // in case there are other components in the string element other than the
            // annotation itself
            List<String> annoList = extractString(element);
            for (String anno : annoList) {
              // this is the position of the current line entry
              int localPosition = position + j;
              result = "@" + anno + "_" + localPosition;
              // update the data of AnnoLocate
              if (annoLocate.containsKey(result)) {
                int value = annoLocate.get(result);
                annoLocate.put(result, value + 1);
              }
            }
          }
        }
      }
    }

    // Update the data of AnnoSimilar.
    for (Map.Entry<String, Integer> me : annoLocate.entrySet()) {
      String annoName = me.getKey();
      /* If the number of computer-written code missing that element is less than the total number of codes written
      by computer, the at least one of those computer-written code must have gotten the annotation correct. */
      if (me.getValue() < args.length - 1) {
        // For example, if we have @Option_345, we will only need "@Option" since we want the
        // general type here.
        int index = annoName.indexOf("_");
        if (index >= 0) {
          annoName = annoName.substring(0, index);
        }
        annoName = trimParen(annoName);
        int value = annoSimilar.get(annoName);
        value = value + 1;
        annoSimilar.put(annoName, value);
      }
    }

    // Output the results.
    System.out.println();
    for (Map.Entry<String, Integer> e : annoCount.entrySet()) {
      int totalCount = e.getValue();
      String value = e.getKey();
      if (listOfAnnoToIgnore.containsKey(value)) {
        totalCount = totalCount - listOfAnnoToIgnore.get(value);
      }
      int correctCount = annoSimilar.get(value);
      // totalCount being equal to 0 meaning that all of the annotations with this type are within
      // the bound of some SuppressWarnings
      if (totalCount != 0) {
        System.out.println(value + " got " + correctCount + "/" + totalCount);
      }
    }
  }
}
