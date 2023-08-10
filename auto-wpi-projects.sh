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

function installCF(){
        # check whether the correct snapshot version is already in the local maven repo
	if [ ! -d "$HOME/.m2/repository/org/checkerframework/checker/${CFVERSION}" ];
	then
		mkdir -p "$HOME/checkerframework"
        	pushd $HOME/checkerframework
		echo "Checkerframework not found... cloning"
        	git clone $CF_REPO &> /dev/null
        	wait
        	echo "Checkerframework cloned successfully" 
        	cd checker-framework/
		git switch "$2"
		git pull origin HEAD
        	./gradlew build ; # TODO: Building CF has two potential issues, delete annotation tools and update hard coded stubparser-3.25.2.jar -> 3.25.3.jar
     		wait
		rm -rf ../annotation-tools
		sed -i 's/stubparserVersion = "3.25.2"/stubparserVersion = "3.25.3"/g' build.gradle
		./gradlew build || exit; 
		wait
		echo "Checkerframework build successful" 
        	./gradlew pushToMavenLocal || exit;
        	wait
        	popd
		return
	fi
}

function getTypecheck(){
	cp typecheck.out $PROJECT_PATH/$TYPECHECK_DIR/$1-typecheck.out
}


PROJECT_DIR="projects"
TYPECHECK_DIR="typecheckout"
PROJECT_PATH=$(realpath $PROJECT_DIR)
CF_VERSION="3.34.1-SNAPSHOT"
CF_REPO="https://github.com/typetools/checker-framework.git"
CF_COMMIT="0096e41a08f9ca4500f8b576faa62b0c60747346"

# Root directory for all projects.

# Java home should be set to the path for JAVA 11 as some projects only run under Java 11.
if [ -z "$JAVA_HOME" ]; then echo "JAVA_HOME enviornment variable is empty"; exit; fi;
# Change CF_VERSION to desired checkerframework version.

installCF $CF_VERSION $CF_COMMIT
if [ ! -d "$HOME/.m2/repository/org/checkerframework/checker/$CF_VERSION/" ]; then echo "Checkerframework Snapshot not found"; exit; fi;
if [ -z "$CHECKERFRAMEWORK" ]; then echo "Checker framework enviornment variable is empty"; exit; fi;

exit
if [ ! -d $PROJECT_DIR ]; then mkdir $PROJECT_DIR; fi
cd "$PROJECT_DIR"
if [ ! -d $TYPECHECK_DIR ]; then mkdir $TYPECHECK_DIR; fi

for repository in "${repositories[@]}"
do
  # Default
  BRANCH="wpi-enabled"
  WPI_SCRIPT="wpi.sh"
  repo_name=$(echo "$repository" | cut -d'/' -f5 | cut -d'.' -f1)

  # Change script name for dmn-check
  git clone -b $BRANCH "$repository" &> /dev/null
  wait
  echo "Cloned $repository sucessfully"

  # Traverse to subproject [Special Case Project]
  if [ "$repo_name" = "cache2k-wpi" ]; then pushd "cache2k-wpi/cache2k-api"; 
	./$WPI_SCRIPT
	wait
	getTypecheck $repo_name || echo "failed typecheck for $repo_name"
	popd
	continue
 fi

  # Use multi project script [Special Case Project]
  if [ "$repo_name" = "dmn-check-wpi" ]
  then
    pushd "$repo_name"
    WPI_SCRIPT="wpi-subprojects.sh" 
    source ./$WPI_SCRIPT
    wait
    getTypecheck "$repo_name"
    popd
    continue
  fi

  # If not special case project, continue standard operation.
  pushd "$repo_name"
  #Run WPI Script on wpi-enabled branch.
  ./$WPI_SCRIPT
  wait
  getTypecheck "$repo_name"
  popd
done
