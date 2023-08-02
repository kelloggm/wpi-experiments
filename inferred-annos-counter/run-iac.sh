#!/bin/bash

# This script invokes the InferredAnnosCounter repeatedly on each java file
# and its corresponding ajava files in the given pair of similarly-structured
# directories.

# inputs:
# $1: the absolute path to the root source directory of the Java files
# $2: the absolute path to the root source directory of the generated ajava files

ROOT_IAC_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# make pushd/popd quiet
pushd () {
    command pushd "$@" > /dev/null
}

popd () {
    command popd "$@" > /dev/null
}

# this runs the IAC, passing a single java file and then the list of ajava files as the arguments
run_iac () {
    pushd "${ROOT_IAC_DIR}" || exit
    ./gradlew -q run --args="$*"
    popd || exit
}

JAVA_FILES=$(find "${1}" -name "*.java")

for file in ${JAVA_FILES}; do
    echo "${file}:"
    filenodirname="${file##*/}"
    filebasename="${filenodirname%.*}"
    AJAVA_FILES=$(find "${2}" -name "${filebasename}-*.ajava")
    AJAVA_SPACE=$(echo "${AJAVA_FILES}" | tr "\n" " ")
    run_iac "${file}" "${AJAVA_SPACE}"
done

# TODO: combine the outputs
