# Run this script by passing the name of the project as argument. 
# Example: python RunWPI.py icalavailable
import subprocess
import glob
import os
import sys
import pandas as pd
import shutil

project_name = str(sys.argv[1])
print("Enter the directory of wpi-paper\n\texample: /Users/ma/Desktop/wpi-paper")
wpidir = input("wpi-paper directory: ")
wpidir = wpidir.strip(" ")

# Pauses the program to allow the users make necessary changes
def pausePrg():
    print("Enter 0 to continue")
    ext = int(input())
    while ext != 0:
        print("Try again")
        ext = int(input())
        
# Returns the Human-written and wpi-generated stats as a dataframe 
def readOutput():
    annotationStats = pd.DataFrame(columns=['Annotation', 'Original count', 'Inferred', 'Original inferred count', 'Original inferred %'])
    resultSection = False
    f = open("Human-written.txt", "r")
    for x in f:
        if resultSection == True and x=='\n':
            resultSection = False
            break
        if "Found annotations:" in x:
            resultSection = True
            continue
        if resultSection:
            info = x.split("\t")
            annot = info[0].replace(' ', '')
            count = info[-1].replace('\n', '')
            annotationStats.loc[-1] = [annot, count, '0', '0', '0']
            annotationStats.index = annotationStats.index + 1
            annotationStats = annotationStats.sort_index()
        
    resultSection = False
    f = open("Generated.txt", "r")
    for x in f:
        if "====== COMBINED RESULTS =======" in x:
            resultSection = True
            continue
        if resultSection:
            info = x.split("\t")
            annot = info[0]
            count = info[1].replace('\n', '')
            if annotationStats.loc[annotationStats["Annotation"]==annot].empty:
                annotationStats.loc[-1] = [annot, '0', count, '0', '0']
                annotationStats.index = annotationStats.index + 1
                annotationStats = annotationStats.sort_index()
            else:
                annotationStats.loc[annotationStats["Annotation"]==annot, "Inferred"] = int(count) + int(annotationStats.loc[annotationStats["Annotation"]==annot, "Inferred"].values[0])
                      
    return annotationStats

# Get original annotation status and remove annotations
def part1():
    
    '''
    Step C-1:
        Create a new branch called "unannotated" starting at the same commit: 
            git checkout -b unannotated
    '''
    process = subprocess.run("git checkout -b unannotated", shell=True)
    '''
    Step C-2:
        Run the RemoveAnnotationsForInference program on the source, no ouput means it ran successfully: 
            java -cp "$CHECKERFRAMEWORK/checker/dist/checker.jar" org.checkerframework.framework.stub.RemoveAnnotationsForInference .
    
    '''
    process = subprocess.run("java -cp \"$CHECKERFRAMEWORK/checker/dist/checker.jar\" org.checkerframework.framework.stub.RemoveAnnotationsForInference .", shell=True)
    '''
    Step C-3:
        Push the unannotated code: 
            git commit -am "output of RemoveAnnotationsForInference" ; git push origin unannotated
    '''
    process = subprocess.run("git commit -am \"output of RemoveAnnotationsForInference\" ; git push origin unannotated", shell=True)
    
    print("\nVerify that, because the annotations have been removed, the program no longer typechecks. You should see an error from one of the Checker Framework checkers you recorded in step B3 when you re-run whatever command you used to run the typechecker before.")
    pausePrg()
    
    '''
    D. Collect the number of original annotations in the code:
        1. run git checkout -b annotation-statistics origin/baseline
    '''
    process = subprocess.run("git checkout -b annotation-statistics origin/baseline", shell=True)

    '''
        2. modify the project's build file so that it
            i. does not run the typecheckers that it was running before, and
            ii. does run the processor org.checkerframework.common.util.count.AnnotationStatistics
            iii. add the -Aannotations and -Anolocations options, and make sure that you remove any -Werror argument to javac.
        3. If the build system is maven, add <showWarnings>true</showWarnings> to the maven-compiler-plugin <configuration>.
        4. If the project is running a formatter (ex: Spotless), disable it in the build system.
        5. Stage your changes with git add (in case you missed a formatter).
        6. Compute annotation statistics.
            i. Clean the program, then compile the program.
    '''
    print("\nNow do the following:\n1. Open build file")
    print("2. Comment out all the typecheckers that it was running before")
    print("3. Add the following checker:\n\n\t'org.checkerframework.common.util.count.AnnotationStatistics',\n")
    print("4. Add the following options:\n\n\t'-Aannotations',\n\t'-Anolocations',\n")
    print("5. Comment out '-Werror'")
    print("6. If the build system is maven, add <showWarnings>true</showWarnings> to the maven-compiler-plugin <configuration>.")
    print("7. If the project is running a formatter (ex: Spotless), disable it in the build system.")
    print("8. Open another terminal in the same directory")
    print("9. Stage your changes with git add (in case you missed a formatter).")    
    '''
        6. Compute annotation statistics.
            i. Clean the program, then compile the program.
    '''
    
    print("10. Compile the project and save the output in 'Human-written.txt' file\n\n\texample:\n\t./gradlew build >> Human-written.txt\nIf the build system is Maven and no AnnotationStatistics output was produced, you'll need to use an alternative strategy to count the annotations. There are some notes on how to do so in the file `maven.md`.")
    pausePrg()

    '''
        7. run git commit -am "annotation statistics configuration" ; git push origin annotation-statistics
    '''
    process = subprocess.run("git commit -am \"annotation statistics configuration\" ; git push origin annotation-statistics", shell=True)

    '''
    E. Collect the number of lines of code: 
        1. run git checkout baseline 
        2. run scc . and record the number of non-comment, non-blanks lines of Java code (the "Code" column of the "Java" row) in the spreadsheet (in the "LoC" column), on the summary page (if you don't have scc installed, https://github.com/boyter/scc)
    '''
    process = subprocess.run("git checkout baseline", shell=True)
    process = subprocess.run("scc .", shell=True)
    print("Record the number of non-comment, non-blanks lines of Java code(the \"Code\" column of the \"Java\" row) in the spreadsheet (in the \"LoC\" column), on the summary page\n(if you don't have scc installed, https://github.com/boyter/scc)")
    pausePrg()
    print("End of part1 (original annotation status and remove original annotations)")
    
#Run WPI
def part2():
    '''
    F. Run WPI:
        1. run git checkout -b wpi-enabled origin/unannotated 
        2. choose any temporary directory for $WPITEMPDIR 
        3. modify the build file to run with -Ainfer=ajava, -Awarns, '-AinferOutputOriginal', and -Aajava=$WPITEMPDIR (modifying the latter as appropriate for project structure, Ex: '-Aajava=/path/to/temp/dir/'). Make sure that you remove any -Werror argument to javac, because otherwise WPI will fail. 
        4. write a short script based on the template in wpi-template.sh. The script should: 
            i. copy the content of build/whole-program-inference into $WPITEMPDIR 
            ii. compile the code 
            iii. compare build/whole-program-inference and $WPITEMPDIR. If they're the same, exit. Otherwise, go to step i. 
        5. add this script: chmod +x wpi.sh ; git add wpi.sh 
        6. commit the script and build changes: git commit -am "enable WPI" ; git push origin wpi-enabled 
        7. execute the script: ./wpi.sh 
        8. record the number of errors issued by the typecheckers (and which typechecker issued the error) after the script is finished
        9. Save the output of WPI (i.e., the warnings it produces) to the file `typecheck.out` in the project's top-level directory.
        10. Add `typecheck.out` to git and then commit to the `wpi-enabled` branch: `git add typecheck.out ; git commit -m "results of typechecking" ; git push origin wpi-enabled`
    '''
    process = subprocess.run("git checkout -b wpi-enabled origin/unannotated ", shell=True)
    
    os.system("mkdir tmp")
    
    print("\nNow do the following:\n1. modify the build file to run with")
    print("\n\t'-Ainfer=ajava',\n\t'-Awarns',\n\t'-AinferOutputOriginal',\n\t'-Aajava=$WPITEMPDIR',\n")
    print("2. Comment out '-Werror'")
    pausePrg()    
    
    os.system("cp " + wpidir + "/experiments/wpi-template.sh wpi.sh")
    print("Open wpi.sh and edit the first 5 variables.")
    pausePrg()
    process = subprocess.run("chmod +x wpi.sh ; git add wpi.sh", shell=True)
    process = subprocess.run("git commit -am \"enable WPI\" ; git push origin wpi-enabled ", shell=True)
    process = subprocess.run("./wpi.sh &> typecheck.out", shell=True)
    process = subprocess.run("git add typecheck.out ; git commit -m \"results of typechecking\" ; git push origin wpi-enabled", shell=True)
    print("Record, under \"Warnings after WPI\" in the project's tab in the spreadsheet, the number of errors issued by the typecheckers and which typechecker issued the error. The output of WPI is in the file typecheck.out.")
    pausePrg()
    '''
    G. Create a branch for the code with inferred annotations 
         1. Create a branch: git checkout -b wpi-annotations annotation-statistics 
         2. Create a new directory for the inferred ajava files: mkdir wpi-annotations 
         3. copy all the ajava files: rsync -r ${WPITEMPDIR}/ wpi-annotations 
         4. commit the results: git add wpi-annotations/** ; git commit -am "WPI annotations" ; git push origin wpi-annotations
    '''
    
    process = subprocess.run("git checkout -b wpi-annotations annotation-statistics", shell=True)
    process = subprocess.run("mkdir wpi-annotations", shell=True)
    process = subprocess.run("rsync -r ${WPITEMPDIR}/ wpi-annotations", shell=True)
    process = subprocess.run("git add wpi-annotations/** ; git commit -am \"WPI annotations\" ; git push origin wpi-annotations", shell=True)

#Evaluate WPI
def part3():
    '''
    H. Measure the number of annotations that WPI inferred, by checker: 
        1. switch to the wpi-annotations branch: git checkout wpi-annotations 
        2. create a copy of the script compute-annos-inferred.sh in the target project directory 
        3. modify the variables at the beginning of the script as appropriate for the target project 
        4. run the script 
        5. transcribe the output after "====== COMBINED RESULTS =======" is printed to the spreadsheet, combining rows that mention the same annotation (this happens when e.g., different @RequiresQualifier annotations are inferred by different checkers) 
        6. commit and push the script: git add compute-annos-inferred.sh ; git commit -m "inference output summarization script" ; git push origin wpi-annotations
    '''
    process = subprocess.run("git checkout wpi-annotations", shell=True)
    os.system("cp " + wpidir + "/experiments/compute-annos-inferred.sh compute-annos-inferred.sh")
    print("Open 'compute-annos-inferred.sh' and modify the variables at the beginning of the script as appropriate for the target project")
    pausePrg()
    
    process = subprocess.run("chmod +x compute-annos-inferred.sh ; git add compute-annos-inferred.sh", shell=True)
    process = subprocess.run("./compute-annos-inferred.sh >> Generated.txt", shell=True)
    pausePrg()
    
    '''
    Create Annotations Stats csv file
    '''
    df = readOutput()
    if os.path.exists(wpidir + "/experiments/inferred-annos-counter/inputExamples/"+project_name) == 0:
        os.system("mkdir " + wpidir + "/experiments/inferred-annos-counter/inputExamples/"+project_name)
    df.to_csv(wpidir+"/experiments/inferred-annos-counter/inputExamples/"+project_name+"/AnnotationStats.csv", index=False)

    process = subprocess.run("git add compute-annos-inferred.sh ; git commit -m \"inference output summarization script\" ; git push origin wpi-annotations", shell=True)

#Measure the percentage of hand-written annotations that WPI inferred:
def part4():
    '''
    Measure the percentage of hand-written annotations that WPI inferred 
        1. copy the formatter script as format-mycopy.sh found in the experiments directory into your project's top level directory.
    '''
    
    fread = open( wpidir + "/experiments/format.sh", "r")
    fwrite = open("format-mycopy.sh", "w")
    for x in fread:
        if "GJF=" in x:
           fwrite.write("GJF="+wpidir+"/google-java-format-1.15.0-all-deps.jar") 
        else:
            fwrite.write(x)
    fwrite.close()
    fread.close()
    '''
        2. download the google-java-format.jar. The link for it is in the script's documentation. Confirm the variable path in the script aligns with the location of your download.     
    '''
    if os.path.exists(wpidir+"/google-java-format-1.15.0-all-deps.jar") == False:
        os.system("wget -P "+wpidir+" https://github.com/google/google-java-format/releases/download/v1.15.0/google-java-format-1.15.0-all-deps.jar")
    '''
        3. change the variable, WPI_RESULTS_DIR to the path of your project's WPI annotations wpi-annotations. 
        4. confirm that the JAVA_SRC_DIR is appropriate to your project's file tree which may look something like ./src/main/java/ and should contain your-project.java in the lowest directory, modify if needed.     
    '''
    print("\n\nOpen 'format-mycopy.sh'.")
    print("Change the variable, WPI_RESULTS_DIR to the path of your project's WPI annotations wpi-annotations")
    print("Confirm that the JAVA_SRC_DIR is appropriate to your project's file tree which may look something like ./src/main/java/ and should contain your-project.java in the lowest directory, modify if needed.")
    pausePrg()
    '''
        5. run the script ./format-mycopy.sh.    
    '''
    process = subprocess.run("chmod +x format-mycopy.sh ; git add format-mycopy.sh", shell=True)
    process = subprocess.run("./format-mycopy.sh", shell=True)
    '''
        6. copy outputs of this experimental procedure into (/main/experiments/inferred-annos-counter/inputExamples). This can be done by creating a directory in (/inferred-annos-counter/inputExamples) with the name of your project and two sub folders, generated and human-written. (This and following steps are optional. Use as input for downstream tools on small projects only). 
        7. copy all of the contents in your $WPITEMPDIR directory used in the previous steps into the generated subfolder. 
        8. copy all of the human-written code (the human-annotated, i.e. original, code, but with a formatter run over it) from your project's source folder (e.g., ./src/main/java/ in a Gradle project) into the human-written directory that was created 

    '''
    if os.path.exists(wpidir + "/experiments/inferred-annos-counter/inputExamples/"+project_name+"/generated"):
        shutil.rmtree(wpidir + "/experiments/inferred-annos-counter/inputExamples/"+project_name+"/generated")
    if os.path.exists(wpidir + "/experiments/inferred-annos-counter/inputExamples/"+project_name+"/human-written"):
        shutil.rmtree(wpidir + "/experiments/inferred-annos-counter/inputExamples/"+project_name+"/human-written")
    
    source_dir = "wpi-annotations/"
    destination_dir = wpidir+"/experiments/inferred-annos-counter/inputExamples/"+project_name+"/generated"
    shutil.copytree(source_dir, destination_dir)
    
    fread = open("format-mycopy.sh", "r")
    
    for x in fread:
        if "JAVA_SRC_DIR=" in x:
            source_dir = x.split("=./")[1]
            source_dir = source_dir.replace("\n", "")
            destination_dir = wpidir+"/experiments/inferred-annos-counter/inputExamples/"+project_name+"/human-written"
            shutil.copytree(source_dir, destination_dir)
            break
    else:
        print("Failed to copy Human written files. Please do it manually.")
        pausePrg()
    
    '''
        9. Go to the working directory of InferredAnnosCounter. Run the InferredAnnosCounter, with the first argument being the absolute path of the .java file, and other arguments being the absolute path of the .ajava files produced by the WPI. The program assumes that a formatter has been applied, so it is important to do so before passing the files as input. InferredAnnosCounter only takes one .java file at a time. So in case it is needed to run multiple .java files with corresponding .ajava files, InferredAnnosCounter needs to be invoked multiple times. The way to run InferredAnnosCounter is like this: cd experiments/inferred-annos-counter (going to the working directory) and then ./gradlew run --args="(a path to the .java file) (one or more paths to the .ajava files)". The result will not be in alphabetical order. If order is important, sort it before recoding the result in the speadsheet.
    '''
    ## RunIAC.py

def resetAll():
    process = subprocess.run("git checkout baseline", shell=True)
    process = subprocess.run("git stash", shell=True)
    process = subprocess.run("git branch -D annotation-statistics &> /dev/null", shell=True)
    process = subprocess.run("git push origin --delete annotation-statistics &> /dev/null", shell=True)
    process = subprocess.run("git branch -D unannotated  &> /dev/null", shell=True)
    process = subprocess.run("git push origin --delete unannotated  &> /dev/null", shell=True)
    process = subprocess.run("git branch -D wpi-annotations  &> /dev/null", shell=True)
    process = subprocess.run("git push origin --delete wpi-annotations  &> /dev/null", shell=True)
    process = subprocess.run("git branch -D wpi-enabled  &> /dev/null", shell=True)
    process = subprocess.run("git push origin --delete wpi-enabled  &> /dev/null", shell=True)
    
    if os.path.exists("Human-written.txt"):
        os.remove("Human-written.txt")
    if os.path.exists("Generated.txt"):
        os.remove("Generated.txt")
    if os.path.exists("compute-annos-inferred-out.sh"):
        os.remove("compute-annos-inferred-out.sh")
    if os.path.exists("wpi.sh"):
        os.remove("wpi.sh")
    if os.path.exists("format-mycopy.sh"):
        os.remove("format-mycopy.sh")
    if os.path.exists("tmp"):
        shutil.rmtree("tmp")
    if os.path.exists("wpi-annotations"):
        shutil.rmtree("wpi-annotations")
        

    
if __name__ == "__main__":
    resetAll()
    part1()
    part2()
    part3()
    part4()
    
