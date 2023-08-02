#!/bin/bash

# This script is a template for the WPI loop for a project with -Ainfer=ajava
# added to its build file. Fill in the variables at the beginning of the
# script with values that make sense for your project; the values there
# now are examples.


# The compile and clean commands for the project's build system.
BUILD_CMD="./gradlew compileJava"
CLEAN_CMD="./gradlew clean"
${BUILD_CMD} # Compile the program so that WPIOUTDIR is created.

# Where should the output be placed at the end? This directory is also
# used to store intermediate WPI results. The directory does not need to
# exist. If it does exist when this script starts, it will be deleted.
# If you are using the subprojects script, set WPITEMPDIR to "$1"
WPITEMPDIR=tmp
# Where is WPI's output placed by the Checker Framework? This is some
# directory ending in build/whole-program-inference. For most projects,
# this directory is just ./build/whole-program-inference .
# The example in this script is the output directory when running via the gradle plugin.
# (The CF automatically puts all WPI outputs in ./build/whole-program-inference,
# where . is the directory from which the javac command was invoked (ie, javac's
# working directory). In many build systems (e.g., Maven), that directory would be the project.
# But, some build systems, such as Gradle, cache build outputs in a central location
# per-machine, and as part of that it runs its builds from that central location.)
# The directory to use here might vary between build systems, between machines
# (e.g., depending on your local Gradle settings), and even between projects using the
# same build system (e.g., because of a project's settings.gradle file).

# Program needs to compiled before running script so WPI creates this directory.
# If you are using the subprojects script, set WPIOUTDIR to "$2"
WPIOUTDIR=~/.gradle/workers/build/whole-program-inference 

# Whether to run in debug mode. In debug mode, output is printed to the terminal
# at the beginning of each iteration, and the diff between each pair of iterations is
# saved in a file named iteration$count.diff, starting with iteration1.diff.
# (Note that these files are overwritten if they already exist.)
DEBUG=1

# End of variables. You probably don't need to make changes below this line.

rm -rf ${WPITEMPDIR}
mkdir -p ${WPITEMPDIR}

count=1
while : ; do
    if [[ ${DEBUG} == 1 ]]; then
	echo "entering iteration ${count}"
    fi
    ${BUILD_CMD}
    ${CLEAN_CMD}
    # This mkdir is needed when the project has subprojects.
    mkdir -p "${WPITEMPDIR}"
    mkdir -p "${WPIOUTDIR}"
    DIFF_RESULT=$(diff -r ${WPITEMPDIR} ${WPIOUTDIR})
    if [[ ${DEBUG} == 1 ]]; then
	echo "putting the diff for iteration $count into $(realpath iteration$count.diff)"
	echo ${DIFF_RESULT} > iteration$count.diff
    fi
    [[ "$DIFF_RESULT" != "" ]] || break
    rm -rf ${WPITEMPDIR}
    mv ${WPIOUTDIR} ${WPITEMPDIR}
    ((count++))
done
