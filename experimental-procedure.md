This document describes the experimental procedure for adding a new project to the
experiments in the WPI paper.

For a shorter version of experimental-procedure, go to experimental-procedure-short.md.
The shorter version is more automated, but might be unreliable when working with complex
projects, so if you choose to use it you might need to fall back to this document if it fails.

The input to the procedure is a program
that typechecks using one or more Checker Framework typecheckers.

These instructions create the following branches in a git fork:
 * baseline -- the project with its human-written annotations
 * annotation-statistics -- the buildfile runs the AnnotationStatistics processor
 * unannotated -- the project with human-written annotations removed
 * wpi-enabled -- the buildfile runs WPI instead of just doing typechecking
 * wpi-annotations -- contains the inferred annotations

The outputs of the experimental procedure are the following, which are the
numbers in tables 1 & 2 in the WPI paper:
* the number of lines of code in the project
* the number of annotations in the project, before running WPI,
split by checker ("original annotations")
* the number of annotations inferred by WPI, split by checker
("inferred annotations")
* the number of remaining errors after running WPI
* the number of the annotations in the original source code that WPI was able to infer

Prerequisites:
* TODO: list required software. Incomplete list of software you must have installed:
git, a JDK, the Checker Framework (see next item)
* follow the "Building from Source" instructions in the Checker Framework manual
(https://checkerframework.org/manual/#build-source), especially making sure
that your $CHECKERFRAMEWORK environment variable is set, because that is used
in some of the steps of the experimental procedure below.

The procedure:

##### Choose a project
   1. Choose a project from https://docs.google.com/spreadsheets/d/1GavNOnEVl3n9SQ4q6OrDfRqmgHPeMIRHAhAWUzQsMPY/edit#gid=1441132849
   2. Put your name in column D.

##### A. Clone the project:
   1. Fork the project.
   2. Clone your fork in the experiments/projects/ directory (create it if necessary).
   3. In the file `projects.in`, record the URL (of your fork) and commit ID.
   4. Add a row to the "summary" sheet in the spreadsheet
      (https://docs.google.com/spreadsheets/d/1r_NhumolEp5CiOL7CmsvZaa4-FDUxCJXfswyJoKg8uM/)
      with both the original and forked url as well as the commit ID.

##### B. Create a new branch called "baseline" at that commit:
   1. `git checkout -b baseline ; git push origin baseline`
   2. Ensure that the project builds and typechecks (determine the appropriate command and record it in the spreadsheet).  You may need to update some @SuppressWarnings, for example, or possibly add new ones.
   3. Inspect the project's build system and determine the list of typecheckers from the Checker
   Framework that the project runs. How this is expressed also varies by build system. For example, in
   a Gradle project that uses the checkerframework-gradle-plugin, you would check the `checkers` block.
   In other build systems, look for a `-processor` argument. Record the names of the typecheckers in the
   "Checkers" column of the spreadsheet at https://docs.google.com/spreadsheets/d/1r_NhumolEp5CiOL7CmsvZaa4-FDUxCJXfswyJoKg8uM/ .
   4. Change the project build file to use the HEAD of typetools/checkerframework:
      1. Pull the latest changes to your local working copy of the checker-framework respository.
      2. Run `./gradlew publishToMavenlocal` in your checker-framework working copy. This generates a SNAPSHOT of the current release version + 1.
         (ie, REL 3.28.0 would generate 3.28.1-SNAPSHOT).
      3. Modify the build file to use this snapshot. This can be project specific, find the line where checkerframeworkversion or similar is defined. You will have to replace the version that is defined. 
         1. For Maven, you may find the checker-framework version under properties, the tag may vary. Modify the line to use the generated snapshot by replacing the version with your appropriate snapshot (ie, "3.28.1-SNAPSHOT").
         2. For Gradle, refer to the Gradle Plugin's README, https://github.com/kelloggm/checkerframework-gradle-plugin#specifying-a-checker-framework-version. 
         3. Confirm that the version of checker-qual is also using the appropriate snapshot.
    5. verify that the project still builds using the newest Checker Framework (try to fix any problems you encounter caused by updating the Checker Framework, but you may have to discard the project at this step if e.g. the project requires a Java version <= 7, which worked with some older versions of the CF but not with modern versions)
    6. commit the result to the `baseline` branch: `git commit -am "run with modern Checker Framework" ; git push origin baseline`


##### C. Create a new branch called "unannotated" from the new `baseline` commit, with annotations removed:
   1. `git checkout -b unannotated`
   2. Run the `RemoveAnnotationsForInference` program on the source; no ouput means it ran successfully:
      `java -cp "$CHECKERFRAMEWORK/checker/dist/checker.jar" org.checkerframework.framework.stub.RemoveAnnotationsForInference .`
   3. Push the unannotated code:
      `git commit -am "output of RemoveAnnotationsForInference" ; git push origin unannotated`
   4. Verify that, because the annotations have been removed, the program no longer typechecks. You should
   see an error from one of the Checker Framework checkers you recorded in step B3 when you re-run whatever
   command you used to run the typechecker before. Note: the `RemoveAnnotationsForInference` program might 
   remove annotations that it should not (e.g., annotations from non-Checker-Framework projects that are required 
   for the project to compile). If you see something else (e.g., a `symbol not found` error), follow these steps:
      1. Run `git diff origin/baseline` to see the removed annotations. Examine each removed annotation and check whether
      it belongs to the Checker Framework. You can do this by searching for the annotation's name in 
      the [Checker Framework manual](https://checkerframework.org/manual/).
      2. For each annotation that does not belong to the Checker Framework, re-add it to the project.
      3. For each annotation that does not belong to the Checker Framework, add it to the list of "trusted"
      annotations in the implementation of `RemoveAnnotationsForInference`, which you can find [here](https://github.com/typetools/checker-framework/blob/master/framework/src/main/java/org/checkerframework/framework/stub/RemoveAnnotationsForInference.java).
      The list of trusted annotations is in the `isTrustedAnnotation(String)` method. Make a PR to the Checker Framework with
      the new trusted annotations.

##### D. Collect the number of original annotations in the code:
   1. run `git checkout -b annotation-statistics origin/baseline`
   2. modify the project's build file so that it
        1. does not run the typecheckers that it was running before, and
        2. does run the processor org.checkerframework.common.util.count.AnnotationStatistics
        3. add the `-Aannotations` and `-Anolocations` options, and make sure that you remove any `-Werror` argument to javac.
   3. If the build system is maven, add `<showWarnings>true</showWarnings>` to the maven-compiler-plugin `<configuration>`.
   4. If the project is running a formatter (ex: Spotless), disable it in the build system. 
   5. Stage your changes with `git add` (in case you missed a formatter).
   6. Compute annotation statistics.
       1. Clean the program, then compile the program.
       2. Look in the output for "Found annotations:" or "No annotations found."
            TODO: Make the two tags searchable via a single string or simple regex.
       3. Create a new "sheet" in the spreadsheet for the project, by copying an existing
            sheet, changing its title, and deleting the data in it.
            TODO: All the current ones have different formats; they should be made uniform.
       4. Record the output in the spreadsheet (only Checker Framework annotations need to be recorded. Checker Framework
            annotations usually are in a package that starts with "org.checkerframework.*", but if the project uses a custom
	         checker you will need to figure out what its annotations are.).
         TODO: sometimes there are mulitple projects, so there are multiple occurrences of "Found annotations:".  The "Found annotations:" output should indicate in which directory or project the annotations were found, or a script should combine all the tables in the output into a single table.
         TODO: consider writing a script for interpreting the output of AnnotationStatistics by checker?
   7. If the build system is Maven and no AnnotationStatistics output was produced in step 6, you'll need to use an alternative strategy to count the annotations. There are some notes on how to do so in the file `maven.md` in this directory.
   8. run `git commit -m "annotation statistics configuration" ; git push origin annotation-statistics`.

##### E. Collect the number of lines of code:
   1. run `git checkout baseline`
   2. run `scc .` and record the number of non-comment, non-blanks lines of Java code (the "Code" column of the "Java" row) in the spreadsheet at https://docs.google.com/spreadsheets/d/1r_NhumolEp5CiOL7CmsvZaa4-FDUxCJXfswyJoKg8uM (in the "LoC" column, on the summary page)
   If you don't have scc installed, see https://github.com/boyter/scc .
   
##### F. Run WPI:
   1. run `git checkout -b wpi-enabled origin/unannotated`
   2. run `export WPITEMPDIR=/scratch/$USER/wpi-output/PROJECTNAME-wpi` (change PROJECTNAME, don't use it literally).
   3. modify the build file:
       1. run with `-Ainfer=ajava`, `-Awarns`, `-AshowPrefixInWarningMessages`, and `-Aajava=$WPITEMPDIR`
          (the latter should be explicit, not the variable name,, Ex: '-Aajava=/scratch/mernst/wpi-output/Araknemu-wpi').
       2. Remove any `-Werror` argument to javac, because otherwise WPI will fail.
       3. Disable any non-Checker-Framework annotation processors (e.g., user-defined ones)
   4. Copy `wpi-template.sh` to `wpi.sh` in the project directory.
      Edit the first 5 variables.
      This script should achieve the following effect:
      1. copy the content of `build/whole-program-inference` into $WPITEMPDIR
      2. compile the code 
      3. compare `build/whole-program-inference` and $WPITEMPDIR. If they're the same, exit. Otherwise, go to step i.
   5. make git store this script: `chmod +x wpi.sh ; git add wpi.sh`
   6. commit the script and build changes: `git commit -am "enable WPI" ; git push origin wpi-enabled`
   7. execute the script (this may take a while): `./wpi.sh`
      If the Checker Framework crashes, you might need to update to a newer version (on all branches).
   8. Record, under "Warnings after WPI" in the project's tab in the spreadsheet at
      https://docs.google.com/spreadsheets/d/1r_NhumolEp5CiOL7CmsvZaa4-FDUxCJXfswyJoKg8uM/ ,
      the number of errors issued by the typecheckers and which 
      typechecker issued the error.
      This is the output after the last "entering a new iteration" in the output.
      TODO: Say how to compute this.
   9. Save the output of WPI (i.e., the warnings it produces) to the file `typecheck.out` in the project's top-level directory.
   10. Add `typecheck.out` to git and then commit to the `wpi-enabled` branch: `git add typecheck.out ; git commit -m "results of typechecking" ; git push origin wpi-enabled`

##### G. Create a branch for the code with inferred annotations:
   1. Create a branch: `git checkout -b wpi-annotations annotation-statistics`
   2. Create a new directory for the inferred ajava files: `mkdir wpi-annotations`
   3. copy all the ajava files: `rsync -r ${WPITEMPDIR}/ wpi-annotations`
   4. commit the results: `git add wpi-annotations/** ; git commit -am "WPI annotations" ; git push origin wpi-annotations`

##### H. Measure the number of annotations that WPI inferred, by checker:
   1. switch to the `wpi-annotations` branch: `git checkout wpi-annotations`
   2. create a copy of the script `compute-annos-inferred.sh` in the target project directory
   3. modify the variables at the beginning of the script as appropriate for the target project
      [[TODO: I think it would be better to take those variables as arguments if possible, to avoid the need to make a new version of the script.  The advantage of having a concrete script is that in the future it would not be necessary to know which arguments to pass.  But the concrete script could also be just an invocation of the master `compute-annos-inferred.sh` in the paper repository.]]
    7. If the build system is Maven and no AnnotationStatistics output was produce, you'll need to use an alternative strategy to count the 	 annotations. There are some notes on how to do so in the file `maven.md` in this directory.
   4. run the script
   5. transcribe the output after "====== COMBINED RESULTS =======" is printed to the spreadsheet, combining rows that mention the same annotation 
      (this happens when e.g., different @RequiresQualifier annotations are inferred by different checkers)
   6. commit and push the script: `git add compute-annos-inferred.sh ; git commit -m "inference output summarization script" ; git push origin wpi-annotations`

##### I. Measure the percentage of hand-written annotations that WPI inferred:
   1. Run the following command, modifying the paths as appropriate: `bash /path/to/run-iac.sh absolute/path/to/java/source/tree/src/main/java /absolute/path/to/generated/ajava/files/wpi-annotations`
   2. Compute summary numbers by adding the results for each annotation (TODO: automate this step in the run-iac.sh script)
   3. Record the result in project-specific tab of the speadsheet at https://docs.google.com/spreadsheets/d/1r_NhumolEp5CiOL7CmsvZaa4-FDUxCJXfswyJoKg8uM/edit#gid=0.  
    
##### J:
   1. Copy summary numbers from the project-specific spreadsheet page to the "summary" tab at https://docs.google.com/spreadsheets/d/1r_NhumolEp5CiOL7CmsvZaa4-FDUxCJXfswyJoKg8uM/edit#gid=0, and color code the project row green once it is finished.

##### K (An optional step to use a given project as an IAC example)
   1. copy [[TODO: Here and elsewhere, instead of giving an English description of the high-level operation to perform, give a concrete command line that can be cut-and-pasted, reducing errors.]] outputs of this experimental procedure into (`/main/experiments/inferred-annos-counter/inputExamples`). This can be done by creating a directory in (`/inferred-annos-counter/inputExamples`) with the name of your project and two sub folders, `generated` and `human-written`. (This and following steps are optional. Use as input for downstream tools on small projects only).
   2. copy all of the contents in your `$WPITEMPDIR` directory used in the previous steps into the `generated` subfolder. 
   3. copy all of the human-written code from your project's source folder (e.g., ./src/main/java/ in a Gradle project) into the `human-written` directory that was created
