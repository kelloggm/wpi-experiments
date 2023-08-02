#!/usr/bin/sh

# This script extracts the javac args of a maven project.
# It may be useful when annotation statistics swallows expected output 
# and the project has to be run manually as explained in maven.md

if [ "${JAVA_HOME}" = "" ]; then
  echo "Must set the JAVA_HOME environment variable."
  exit 1
fi

MVN_COMPILE="mvn -X clean compile"
${MVN_COMPILE} > compile-out.txt
echo -n '$JAVA_HOME/bin/javac' > JavacRaw.txt
grep -E '\s-d .*$' compile-out.txt | cut -c 8- >> JavacRaw.txt
RUN_ANNO_STATS=$(cat JavacRaw.txt)
echo $RUN_ANNO_STATS

