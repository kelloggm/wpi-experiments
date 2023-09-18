# "Pluggable Type Inference for Free" (ASE 2023) experiments

This repository contains the scripts and experimental data for the
paper "Pluggable Type Inference for Free", which appeared at ASE 2023.

If you want to reproduce the experiments described in the paper, use this.

If you want to use the pluggable type inference on your own code, use the version that is
distributed with [Checker Framework](https://checkerframework.org) as ["Whole Program
Inference"](https://checkerframework.org/manual/#whole-program-inference).  That version is
maintained, whereas the instructions here are static and specific to the version of the tool that
was used to run the experiments in the paper).


## Running locally

In a clone of the Checker Framework:
  git checkout a274d91dc2016e83fc1741bc433d0d979a0532bb
  ./gradlew publishToMavenLocal

In the `wpi-experiments/` directory (which also contains this `README.md` file),
run `auto-wpi-projects.sh`.


## Running using a Docker container

To repeat the experiments using a Docker container, use the Docker container available [here](https://zenodo.org/record/8247517). Download
the container (`ase23.tar.gz`), and then run the following commands:
```
gunzip ase23.tar.gz
cat ase23.tar | docker import - ase23
docker run -it ase23 /bin/bash
```

The contents of this repository should already be present in the `/wpi-experiments` directory of the resulting docker container. Run the `auto-wpi-projects.sh` script from there to repeat the experiments.


## Contents of this repository

The repository contains the following:
* `auto-wpi-projects.sh`: a script that runs WPI on all of the experimental subjects (this corresponds
to table 2 in the paper). Note that your machine must have sufficient RAM available to run WPI on
the largest subject, Randoop (at least 64 GB) or this script will fail.
* `experimental-procedure.md`: a markdown file that describes how each project was modified to
use WPI. Each of the paper's authors followed the procedure at least once, so (we hope!) it should also be
relatively straightforward for someone else to use.
* `RunWpi.py`: a script that automates most of the manual work in the experimental procedure. It may
not work for complex projects, and we recommend that new users follow the procedure manually at
least once before attempting to use it. The file `experimental-procedure-short.md` contains
detailed instructions on how to use this script.
* `maven.md`: this file details some common problems that we encountered when using the experimental
procedure on Maven projects.
* `all-projects-data.xlsx`: this spreadsheet contains the data used to produce Table 2 in the paper. It has a "summary" tab that summarizes all of the projects (including those that didn't make it into the final paper for various reasons), as well as a tab for each project. Each project tab contains the output of the inferred annos counter, a list of all of the annotations that WPI did infer, and also links to the output of the typecheckers before and after annotations were inferred. The number of warnings was computed by hand from those outputs, disregarding warnings that did not come from the typechecker.
* `non-inferred-annotations.xlsx`: this spreadsheet contains our (manual) analysis of the reasons that annotations for each project in table 2 were missed by WPI. Its data was used to produce table 3, as well as the analysis in section 6.E.1. It also has a summary sheet that lists all of the causes we identified, and per-project tabs that list the specific missed annotations and line numbers, as well as our judgement of the causes..
* `compute-annos-inferred.sh`: a template script for computing the number of inferred annotations.
Instructions for how (and when) to use it are in `experimental-procedure.md`.
* `extractJavacArgs.sh`: a helper script for dealing with a problem that arises for some Maven projects.
See `maven.md`.
* `projects.in`: this file lists the repositories and commit ids of the projects that we used in our
experiments.
* `wpi-template.sh`: a template "outer-loop" script for running WPI on a project. Instructions for
using it can be found at the top of the file or in `experimental-procedure.md`.
* `wpi-subprojects.sh`: a variant of `wpi-template.sh` for use on projects that have subprojects,
which is common for large Gradle or Maven projects.
* `inferred-annos-counter`: a small Java project for counting the number of annotations that were inferred
that exactly match the annotations written by humans. See its `README.md` file for more details.
