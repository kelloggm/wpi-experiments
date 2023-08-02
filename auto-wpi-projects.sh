#!/bin/bash

# This script clones, checks out the "wpi-enabled" branch and runs the wpi script over 
# every project used as benchmarks in the "Pluggable Type Inference for Free" paper 
# submitted to ASE2023. 

repositories=(
  "https://github.com/dd482IT/cache2k-wpi.git"
  "https://github.com/dd482IT/dmn-check-wpi.git"
  "https://github.com/kelloggm/icalavailable" 
  "https://github.com/dd482IT/require-javadoc-WPI"
  "https://github.com/dd482IT/randoop-wpi.git"
  "https://github.com/dd482IT/table-wrapper-csv-impl-wpi.git"
  "https://github.com/dd482IT/table-wrapper-api-WPI.git"
  "https://github.com/dd482IT/RxNorm-explorer-wpi.git"
  "https://github.com/dd482IT/reflection-util-wpi"
  "https://github.com/dd482IT/multi-version-control-wpi"
  "https://github.com/dd482IT/lookup-wpi" 
  "https://github.com/dd482IT/Nameless-Java-API-WPI.git"
)

PROJECT_DIR="projects"
PROJECT_PATH=$(realpath $PROJECT_DIR)
# Root directory for all projects.
if [ ! -d $PROJECT_DIR ]; then mkdir $PROJECT_DIR; fi 
pushd "$PROJECT_DIR"

# Java home should be set to the path for JAVA 11 as some projects only run under Java 11.
if [ -z "$JAVA_HOME" ]; then echo "JAVA_HOME enviornment variable is empty"; exit; fi; 
# This path will only be used by gradle projects running them with the -PcfLocal argument.
if [ -z "$CHECKERFRAMEWORK" ]; then echo "checkerframework enviornment variable is empty"; exit; fi; 
for repository in "${repositories[@]}"
do
  # Default
  BRANCH="wpi-enabled"
  WPI_SCRIPT="wpi.sh"
  repo_name=$(echo "$repository" | cut -d'/' -f5 | cut -d'.' -f1)
  # Chane script name for dmn-check
  git clone "$repository"
  pushd "$repo_name"
  # Traverse to subproject [Special Case Project]
  if [ "$repo_name" = "cache2k-wpi" ]; then pushd "cache2k-api"; fi
  git checkout $BRANCH
  # Use multi project script [Special Case Project]
  if [ "$repo_name" = "dmn-check-wpi" ]
  then 
    WPI_SCRIPT="wpi-subprojects.sh" 
    source ./$WPI_SCRIPT
    wait
    continue
  fi
  # Run WPI Script on wpi-enabled branch.
  ./$WPI_SCRIPT 
  wait
  # Return back to home project directory.
  pushd "$PROJECT_PATH"
done