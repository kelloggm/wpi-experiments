This repository contains the scripts and experimental data for the
paper "Pluggable Type Inference for Free", which appeared at ASE 2023.
The tool the paper describes is integrated into the [Checker Framework](checkerframework.org)
as ["Whole Program Inference"](https://checkerframework.org/manual/#whole-program-inference);
the scripts and data in this repository only concern the experiments described in the paper.
If you are interested in using the described inference tool rather than reproducing or extending
the paper's experiments, you should use the instructions there (which are maintained and should
remain up-to-date as the Checker Framework changes and the tool improves) rather than the
instructions here (which are static and specific to the version of the tool that was used
to run the experiments in the paper).

The repository contains the following:
* `experimental-procedure.md`: a markdown file that describes how each project was modified to
use WPI. Each of the paper's authors followed the procedure at least once, so (we hope!) it should also be
relatively straightforward for someone else to use.
* `RunWpi.py`: a script that automates most of the manual work in the experimental procedure. It may
not work for complex projects, and we recommend that new users follow the procedure manually at
least once before attempting to use it. The file `experimental-procedure-short.md` contains
detailed instructions on how to use this script.
* `maven.md`: this file details some common problems that we encountered when using the experimental
procedure on Maven projects.
* `auto-wpi-projects.sh`: a script that runs WPI on all of the experimental subjects (this corresponds
to table 2 in the paper). Note that your machine must have sufficient RAM available to run WPI on
the largest subject, Randoop (TODO: how much is required?) or this script will fail.
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