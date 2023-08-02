#!/bin/sh
set -e

# This script uses runs the AnnotationStatistics utility of the Checker
# Framework on the results of WPI on each checker for which annotations
# were inferred. It should be run on the wpi-annotations branch.
# You should make a copy of this script for each project being analyzed
# and modify it so that it works on that project - this is just a template.

### you may need to change these constants, depending on the project:

# the root of the wpi output
WPI_RESULTS_DIR=./wpi-annotations/

# the root of the java source files
JAVA_SRC_DIR=./src/main/java/

# the command to run AnnotationStatistics (should be the same command
# that is used to compile the target program)
RUN_ANNO_STATS="./gradlew compileJava"

### no need to make changes below this line, usually

# TODO: compute this list from the names of the ajava files
AJAVA_FILE_LIST=$(cd ${WPI_RESULTS_DIR} && find . -name "*-*.ajava")
CHECKER_LIST=""

if [ -f compute-annos-inferred-out ]; then
    # from https://betterprogramming.pub/24-bashism-to-avoid-for-posix-compliant-shell-scripts-8e7c09e0f49a, in response to a shellcheck error noting that $RANDOM is not POSIX-compliant
    var1=$(date +%s)
    var2=$$
    random="$var1$var2"
    echo "making a backup of compute-annos-inferred-out in compute-annos-inferred-out-${random}"
    mv compute-annos-inferred-out compute-annos-inferred-out-"${random}"
fi

for ajava_filename in ${AJAVA_FILE_LIST};
do
    tmp=${ajava_filename##*-};
    CHECKER_LIST="${CHECKER_LIST} ${tmp%.ajava}"
done
CHECKER_LIST=$(echo "${CHECKER_LIST}" | tr " " "\n" | sort | uniq)

JAVA_FILES=$(cd ${JAVA_SRC_DIR} && find . -name "*.java")

for checker in ${CHECKER_LIST}; do
    # copy the ajava files generated for the relevant checker to the
    # corresponding places in the source tree
    echo "======== RUNNING ANNOTATION STATISTIC FOR INFERRED ANNOTATIONS FROM ${checker} ========"
    for java_file in ${JAVA_FILES}; do
	# strip the ".java" from the end of the file name
	base_filename="${java_file%.*}"
	ajava_file="${WPI_RESULTS_DIR}/${base_filename}-${checker}.ajava"
	if [ -f "${ajava_file}" ]; then
	    cp "${ajava_file}" "${JAVA_SRC_DIR}/${java_file}"
	fi
    done
    # run AnnotationStatistics and append the results to the output file
    eval "${RUN_ANNO_STATS}" >> compute-annos-inferred-out 2>&1
    # reset to the state before doing the copying
    git reset --hard wpi-annotations -- > /dev/null 2>&1
done

# find the lines from AnnotationStatistics listing annotations
echo "====== COMBINED RESULTS ======="
grep "^org.checkerframework" < compute-annos-inferred-out | sort | uniq
