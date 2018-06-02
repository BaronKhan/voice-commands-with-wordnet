import sys
import os.path
import csv

def renderBeginningFile(outputFile, outputName):
    outputFile.write("\
import com.khan.baron.vcw.ContextActionMap;\n\
import com.khan.baron.vcw.GlobalState;\n\
/* TODO: insert action imports */\n\
\n\
public class "+outputName+" extends ContextActionMap {\n\
    public "+outputName+"(GlobalState state) {\n\
        super(state);\n"
    )

def renderEndFile(outputFile):
    outputFile.write("\
    }\n\
}"
    )

def renderJava(tableReader, outputName):
    outputFile = open(outputName+".java", 'w')

    renderBeginningFile(outputFile, outputName)

    rowNum = 0
    numActions = 0
    for row in tableReader:
        if rowNum < 2:
            if rowNum < 1:
                numActions = len(row)-1
                outputFile.write("        setActionList(")
                rowStr = ""
                for i in range(1, max(len(row), numActions+1)):
                    if i >= len(row) or row[i].strip()=="":
                        rowStr += "null"
                    else:
                        rowStr += "\""+str(row[i].strip())+"\""
                    if i != max(len(row), numActions+1)-1:
                        rowStr += ", "
                rowStr += ");\n"
                outputFile.write(rowStr)
                rowNum += 1
                continue
            else:
                outputFile.write("        addDefaultContextActions(")
        else:
            outputFile.write("        addContextActions(\""+row[0].strip()+"\", ")

        rowStr = ""
        for i in range(1, max(len(row), numActions+1)):
            if i >= len(row) or row[i].strip()=="":
                rowStr += "null"
            else:
                rowStr += "new "+str(row[i].strip())+"()"
            if i != max(len(row), numActions+1)-1:
                rowStr += ", "
        rowStr += ");\n"
        outputFile.write(rowStr)

        rowNum += 1

    renderEndFile(outputFile)

    outputFile.close()


if len(sys.argv) < 3:
    print("usage: python generateTable.py <csv-path> <output-file-without-.java>")
    exit(0)

if ".java" in sys.argv[2]:
    print("error: remove .java extension from file name")
    exit(0)

fileName = sys.argv[1]
outputName = sys.argv[2]

if not os.path.isfile(fileName):
    print("error: "+fileName+" does not exist")
    exit(0)

with open(fileName, 'rt') as f:
    tableReader = csv.reader(f, skipinitialspace=True, delimiter=',')
    renderJava(tableReader, outputName)