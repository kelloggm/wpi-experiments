This document describes the experimental procedure for adding a new project to the
experiments in the WPI paper.

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

##### A. Clone the project:
   1. Fork the project.
   2. Clone your fork in the experiments/projects/ directory (create it if necessary).
   3. In the file `projects.in`, record the URL (of your fork) and commit ID.
   4. Add a row to the "summary" sheet in the spreadsheet
      (https://docs.google.com/spreadsheets/d/1r_NhumolEp5CiOL7CmsvZaa4-FDUxCJXfswyJoKg8uM/)
      with both the original and forked url as well as the commit ID.

##### B. Create a new branch called "baseline" at that commit:
   1. `git checkout -b baseline ; git push origin baseline`
   2. Ensure that the project builds and typechecks (determine the appropriate command and record it in the spreadsheet).
   3. Inspect the project's build system and determine the list of typecheckers from the Checker
   Framework that the project runs. How this is expressed also varies by build system. For example, in
   a Gradle project that uses the checkerframework-gradle-plugin, you would check the `checkers` block.
   In other build systems, look for a `-processor` argument. Record the names of the typecheckers in the
   "Checkers" column of the spreadsheet.
   4. Change the project build file to use the HEAD of typetools/checkerframework:
      1. Pull the latest changes to your local working copy of the checker-framework respository.
      2. Run `./gradlew publishToMavenlocal` in your checker-framework working copy. This generates a SNAPSHOT of the current release version + 1.
         (ie, REL 3.28.0 would generate 3.28.1-SNAPSHOT).
      3. Modify the build file to use this snapshot. This can be project specific, find the line where checkerframeworkversion or similar is defined. You will have to 
         replace the version that is defined. 
         1. For Maven, you may find the checker-framework version under properties, the tag may vary. Modify the line to use the generated snapshot by replacing the version with your appropriate snapshot (ie, "3.28.1-SNAPSHOT").
         2. For Gradle, refer to the Gradle Plugin's README, https://github.com/kelloggm/checkerframework-gradle-plugin#specifying-a-checker-framework-version. 
    5. verify that the project still builds using the newest Checker Framework (try to fix any problems you encounter caused by updating the Checker Framework, but you may have to discard the project at this step if e.g. the project requires a Java version <= 7, which worked with some older versions of the CF but not with modern versions)
    6. commit the result to the `baseline` branch: `git commit -am "run with modern Checker Framework" ; git push origin baseline`

#### C. Run `RunWPI.py`

1. Set a temporary directory for $WPITEMPDIR: 

  export WPITEMPDIR=\`pwd\`/tmp

3. From wpi-paper copy the `RunWPI.py` file to the project directory

4. Run `RunWPI.py` from terminal and pass the name of the project as argument. `python RunWPI.py icalavailable`

5. Follow instructions of that program. (TODO: Restart if an error is encountered)

6. Go to `wpi-paper/experiments/inferred-annos-counter/` and run inferred-annos-counter: From terminal `python RunIAC.py project-name`.

7. A csv file `AnnotationStats.csv` will be generated in the `wpi-paper/experiments/inferred-annos-counter/inputExamples/project-name/`. Copy summary numbers from the project-specific spreadsheet page to the "summary" tab, and color code the project row green once it is finished.
