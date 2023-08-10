#!/bin/bash

# This script runs wpi.sh on projects that contain multiple  
# sub projects under their respective directories within your project.

# You must make the following changes:
# 1. Add the name of each subproject directory to the `SUBPROJECTS` array. 
# 2. Change WPITEMPDIR to "$1" and WPIOUTDIR to "$2" in your copy 
#    of the wpi-template.sh script.
# 3. Set -Ajava=${env.WPITEMPDIR} in the pom.xml.
# 4. Run this script using the `source` command.

# Other changes (to this script, the wpi.sh script, the project's 
# build system, or any combination of the former) may also 
# be required, to account for project-specific complexity.


# This variable is where the WPI Project root directory will be made and will store the entire project output (PROJECT_TEMP_ROOT).
TOP_LEVEL=/tmp
WPI_SCRIPT_NAME="wpi.sh"

SUBPROJECTS=(
# Add the subdirectory names here.
)

# No changes needed below this point
PROJECT_SPACE=$(realpath .)
PROJECT_NAME=$(basename "${PROJECT_SPACE}")
PROJECT_TEMP_ROOT="${TOP_LEVEL}"/"${PROJECT_NAME}"

mkdir "${PROJECT_TEMP_ROOT}"

for subProject in "${SUBPROJECTS[@]}"
do
    if [ ! -d "${PROJECT_SPACE}"/"${subProject}" ]
    then 
        echo "$subProject not found, check your array list.";
        return
    fi 
done 

for subProject in "${SUBPROJECTS[@]}"
do
    mkdir -p "${PROJECT_TEMP_ROOT}"/"${subProject}"
    WPITEMPDIR="${PROJECT_TEMP_ROOT}"/"${subProject}"
    WPIOUTDIR="$PROJECT_SPACE/$subProject/build/"
    ./$WPI_SCRIPT_NAME $WPITEMPDIR $WPIOUTDIR
done
