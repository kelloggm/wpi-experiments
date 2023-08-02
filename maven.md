## What to do when Maven swallows the output of Annotation Statistics

Maven doesn't permit annotation processors (or javac itself) to print to
standard out. I don't think there's a way to avoid this (based on web searches),
so we need an alternative to running AnnotationStatistics via the build system.

Regardless of how you get this working, make sure you commit a record of the strategy
that you used to the appropriate branch: `annotation-statistics` if you're using this
technique to count human-written annotations, or `wpi-annotations` if you're counting
inferred annotations. 

### Alternative 1: `mvn -X`, then `javac`

If this works, it's the easiest way to collect the numbers (but, it doesn't always
work). Follow these steps (after enabling AnnotationStatistics in the build system as usual):
The `extractJavacArgs.sh` script may be useful as it completes steps 1-3, partially completes 
step 4 (does not quote arguments) producing `JavacRaw.txt` containing the javac arguments.
1. clean the project (`mvn clean`)
2. re-run the compilation in debug mode and pipe the output to a file (`mvn -X compile &> out`)
3. locate the arguments passed to javac in `out`. Maven prints "Command line options:" right before it prints
this list, so I recommend searching for that. Warning: in multi-project builds, there might be more than one
of these in the output, so you should check all of them.
4. copy the options passed to javac into a new shell script named `count-annotations.sh` and precede them with `javac`. You might need to quote some arguments, such as any containing a parenthesis as in `-AskipDefs=(Test|BaseCase|DataSet|TestCase|PacketWithParser|OtherPacket|DummyGenerator|Accessors)$`.  If the project requires $JAVA_HOME set with the project specific version of java, you will need to precede the arguments with `$JAVA_HOME/bin/javac`.
5. run this script. You should get output from AnnotationStatistics.
6. if successful, commit the script.

### Applying alternative to `compute-annos-inferred.sh`
1. the above method will also need to be used when running `compute-annos-inferred.sh`, follow steps 1-3.  
2. replace the `RUN_ANNO_STATS` variable's value in `compute-annos-inferred.sh` with the javac line extracted from the debug output.
3. run `compute-anno-inferred.sh`, if successful, the output should be found in the most recent `compute-annos-out` file. 
4. if successful, commit your changes to `compute-annos-inferred.sh`

### Alternative 2: count by hand

If that doesn't work, the easiest way is to count annotations by hand. To do this, follow these steps:
1. compile a list of relevant annotations. To do that, first determine which checkers are running. Then,
visit their manual sections in the Checker Framework [manual](checkerframework.org/manual). Each manual
section lists the relevant annotations for the checker.
2. place these annotations, one per line, in a text file. Call this file `annos.txt`.
3. add the following annotations to `annos.txt` (these are inferred by all checkers):
   ```@Pure
   @SideEffectFree
   @Deterministic
   @RequiresQualifier
   @EnsuresQualifer
   @RequiresQualifierIf
   @EnsuresQualiferIf
   ```
4. then run something like the following: for anno in `cat annos.txt`; do rg $anno *; done
   * `rg` is [ripgrep](https://github.com/BurntSushi/ripgrep). `grep` is also fine (but much slower).
5. count the results "by hand"---that is, by using ripgrep to count the annotations, but verifying that everything
look okay yourself before fully automating it.
You may need to include the `-w` flag to exact match annotations (if the checker has annotations whose
names are substrings of each other, as most do). The `--stats` and `--count-matches` flags are also useful
to give total counts of the matched annotations, once you've verified by hand that the output looks correct.
But, be sure to run at least once without these flags to make sure the output looks correct: this is
an error-prone and approximate counting method, and you should expect to have to deal with special
cases: this isn't fully automatable.
6. if this works, commit the `annos.txt` file and a script containing the specific `rg` command
that you ran in step 4, along with a note explaining that you counted the results by hand (as a comment
in the script)

TODO: improve the second method: it's fine for dealing with hand-annotated projects, but doesn't
scale to ajava files. Needs to be automated.
