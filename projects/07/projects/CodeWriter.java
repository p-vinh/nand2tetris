package projects;


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

import projects.Parser.CommandType;

import java.util.HashMap;
import java.io.IOException;

public class CodeWriter {
    private PrintWriter writer;
    private int mJumpNumber = 0;
    private int labelNum = 0;
    private static String fileName = "";
    private Map<String, String> segmentMap = new HashMap<String, String>();

    public CodeWriter(File fileName) {
        try {
            writer = new PrintWriter(new FileWriter(fileName));

        } catch (IOException e) {
            e.printStackTrace();
        }

        segmentMap.put("local", "LCL");
        segmentMap.put("argument", "ARG");
        segmentMap.put("this", "THIS");
        segmentMap.put("that", "THAT");

        // Arithmetic
        segmentMap.put("add", "M=M+D");
        segmentMap.put("sub", "M=M-D");
        segmentMap.put("and", "M=D&M");
        segmentMap.put("or", "M=D|M");
    }

    public void setFileName(File file) {
        fileName = file.getName();

    }

    public void writeArithmetic(String command) {
        try {
            switch (command) {
                case "add":
                    writer.println(formatArithLogic().append(segmentMap.get(command)).toString());
                    break;
                case "sub":
                    writer.println(formatArithLogic().append(segmentMap.get(command)).toString());
                    break;
                case "and":
                    writer.println(formatArithLogic().append(segmentMap.get(command)).toString());
                    break;
                case "or":
                    writer.println(formatArithLogic().append(segmentMap.get(command)).toString());
                    break;
                case "not":
                    writer.println("@SP\nA=M-1\nM=!M");
                    break;
                case "eq":
                    writer.println(formatArithLogic().append(formatArithLogic2("JNE")).toString());
                    mJumpNumber++;
                    break;
                case "gt":
                    writer.println(formatArithLogic().append(formatArithLogic2("JLE")).toString());
                    mJumpNumber++;
                    break;
                case "lt":
                    writer.println(formatArithLogic().append(formatArithLogic2("JGE")).toString());
                    mJumpNumber++;
                    break;
                case "neg":
                    writer.println("D=0\n@SP\nA=M-1\nM=D-M\n");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writePushPop(Parser.CommandType commandType, String segment, int index) {
        try {
            if (commandType == Parser.CommandType.C_PUSH) {
                switch (segment) {
                    case "constant":
                        writer.println(
                                "@" + index + "\n" + "D=A\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
                        break;
                    case "static":
                        writer.print(new StringBuilder().append("@").append(fileName).append(index).append("\n").append("D=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n").toString());
                        break;
                    case "pointer":
                        if (index == 0)
                            writer.println(formatPush2("THIS").toString());
                        else if (index == 1)
                            writer.println(formatPush2("THAT").toString());
                        break;
                    case "temp":
                        writer.println(formatPush("R5", index + 5).toString());
                        break;
                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                        writer.println(formatPush(segmentMap.get(segment), index).toString());
                        break;
                    default:
                        new Exception("Invalid segment type");
                        break;
                }

            } else if (commandType == Parser.CommandType.C_POP) {
                switch (segment) {
                    case "constant":
                        writer.println(
                                "@" + index + "\n" + "D=A\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
                        break;
                    case "static":
                        writer.println(new StringBuilder().append("@").append(fileName).append(index).append("\nD=A\n@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n").toString());
                        break;
                    case "pointer":
                        if (index == 0)
                            writer.println(formatPop2("THIS").toString());
                        else if (index == 1)
                            writer.println(formatPop2("THAT").toString());
                        break;
                    case "temp":
                        writer.println(formatPop("R5", index + 5).toString());
                        break;
                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                        writer.println(formatPop(segmentMap.get(segment), index).toString());
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StringBuilder formatArithLogic() {
        StringBuilder sb = new StringBuilder();
        sb.append("@SP\n"); // Get address of SP
        sb.append("AM=M-1\n"); // Decrement A and M by 1; A is now address of SP; M is now value of SP
        sb.append("D=M\n"); // Set D to value of SP (Get first value)
        sb.append("A=A-1\n"); // Decrement A by 1 (Get address of second value)
        return sb;
    }

    private StringBuilder formatArithLogic2(String strJump) {
        StringBuilder sb = new StringBuilder();
        sb.append("D=M-D\n");
        sb.append("@FALSE" + mJumpNumber + "\n");
        sb.append("D;" + strJump + "\n");
        sb.append("@SP\n");
        sb.append("A=M-1\n");
        sb.append("M=-1\n");
        sb.append("@CONTINUE" + mJumpNumber + "\n");
        sb.append("0;JMP\n");
        sb.append("(FALSE" + mJumpNumber + ")\n");
        sb.append("@SP\n");
        sb.append("A=M-1\n");
        sb.append("M=0\n");
        sb.append("(CONTINUE" + mJumpNumber + ")");
        return sb;

    }

    private StringBuilder formatPush(String strSegment, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("@" + strSegment + "\n");
        sb.append("D=M\n");
        sb.append("@" + index + "\n");
        sb.append("A=D+A\n");
        sb.append("D=M\n");
        sb.append("@SP\n");
        sb.append("A=M\n");
        sb.append("M=D\n");
        sb.append("@SP\n");
        sb.append("M=M+1\n");
        return sb;

    }

    private StringBuilder formatPush2(String strSegment) {
        StringBuilder sb = new StringBuilder();
        sb.append("@" + strSegment + "\n");
        sb.append("D=M\n");
        sb.append("@SP\n");
        sb.append("A=M\n");
        sb.append("M=D\n");
        sb.append("@SP\n");
        sb.append("M=M+1\n");
        return sb;
    }

    private StringBuilder formatPop(String strSegment, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("@" + strSegment + "\n");
        sb.append("D=M\n");
        sb.append("@" + index + "\n");
        sb.append("D=D+A\n");
        sb.append("@R13\n");
        sb.append("M=D\n");
        sb.append("@SP\n");
        sb.append("AM=M-1\n");
        sb.append("D=M\n");
        sb.append("@R13\n");
        sb.append("A=M\n");
        sb.append("M=D\n");

        return sb;
    }

    // get format for popping off of stack given the segment - for static & pointer
    private StringBuilder formatPop2(String strSegment) {
        StringBuilder sb = new StringBuilder();
        sb.append("@" + strSegment + "\n");
        sb.append("D=A\n");
        sb.append("@R13\n");
        sb.append("M=D\n");
        sb.append("@SP\n");
        sb.append("AM=M-1\n");
        sb.append("D=M\n");
        sb.append("@R13\n");
        sb.append("A=M\n");
        sb.append("M=D\n");

        return sb;

    }

    public void writeInit() {
        try {
            writer.println("@256");
            writer.println("D=A");
            writer.println("@SP");
            writer.println("M=D");
            writeCall("Sys.init", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeLabel(String label) {
        try {
            writer.println("(" + label + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeCall(String strFunctionName, int nNumArgs) {
        String strLabel = "RETURN_LABEL" + labelNum;
        labelNum++;
        try {
            writer.println(new StringBuilder().append("@").append(strLabel).append("\n")
                    .append("D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n").append(formatPush2("LCL"))
                    .append(formatPush2("ARG")).append(formatPush2("THIS")).append(formatPush2("THAT"))
                    .append("@SP\n").append("D=M\n").append("@5\n").append("D=D-A\n").append("@").append(nNumArgs)
                    .append("\n").append("D=D-A\n").append("@ARG\n").append("M=D\n").append("@SP\n").append("D=M\n")
                    .append("@LCL\n").append("M=D\n").append("@").append(strFunctionName).append("\n0;JMP\n(")
                    .append(strLabel).append(")\n").toString());
            // writer.println("@" + strLabel);
            // writer.println("D=A");
            // writer.println("@SP");
            // writer.println("A=M");
            // writer.println("M=D");
            // writer.println("@SP");
            // writer.println("M=M+1");
            // writer.println(formatPush2("LCL").toString());
            // writer.println(formatPush2("ARG").toString());
            // writer.println(formatPush2("THIS").toString());
            // writer.println(formatPush2("THAT").toString());
            // writer.println("@SP");
            // writer.println("D=M");
            // writer.println("@5");
            // writer.println("D=D-A");
            // writer.println("@" + nNumArgs);
            // writer.println("D=D-A");
            // writer.println("@ARG");
            // writer.println("M=D");
            // writer.println("@SP");
            // writer.println("D=M");
            // writer.println("@LCL");
            // writer.println("M=D");
            // writer.println("@" + strFunctionName);
            // writer.println("0;JMP");
            // writer.println("(" + strLabel + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeGoto(String label) {
        try {
            writer.println("@" + label + "\n" + "0;JMP");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeIf(String label) {
        try {
            writer.println(formatArithLogic().toString());
            writer.println("@" + label + "\n" + "D;JNE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeReturn() {
        try {
            writer.println(new StringBuilder().append("@LCL\n").append("D=M\n").append("@FRAME\n").append("M=D\n")
                    .append("@5\n").append("A=D-A\n").append("D=M\n").append("@RET\n").append("M=D\n")
                    .append(formatPop("ARG", 0)).append("@ARG\n").append("D=M\n").append("@SP\n").append("M=D+1\n")
                    .append("@FRAME\n").append("D=M-1\n").append("AM=D\n").append("D=M\n").append("@THAT\n")
                    .append("M=D\n").append("@FRAME\n").append("D=M-1\n").append("AM=D\n").append("D=M\n")
                    .append("@THIS\n").append("M=D\n").append("@FRAME\n").append("D=M-1\n").append("AM=D\n")
                    .append("D=M\n").append("@ARG\n").append("M=D\n").append("@FRAME\n").append("D=M-1\n")
                    .append("AM=D\n").append("D=M\n").append("@LCL\n").append("M=D\n").append("@RET\n").append("A=M\n")
                    .append("0;JMP\n").toString());

            // writer.println("@LCL"); // LCL address
            // writer.println("D=M"); // D = contents of LCL
            // writer.println("@FRAME");
            // writer.println("M=D"); // FRAME = contents of LCL
            // writer.println("@5"); // temp variable
            // writer.println("A=D-A");
            // writer.println("D=M");
            // writer.println("@RET");
            // writer.println("M=D");
            // writer.println(formatPop("ARG", 0).toString());
            // writer.println("@ARG"); // ARG address
            // writer.println("D=M"); // contents of ARG
            // writer.println("@SP");
            // writer.println("M=D+1");
            // writer.println("@FRAME");
            // writer.println("D=M-1");
            // writer.println("AM=D");
            // writer.println("D=M");
            // writer.println("@THAT");
            // writer.println("M=D");
            // writer.println("@FRAME");
            // writer.println("D=M-1");
            // writer.println("AM=D");
            // writer.println("D=M");
            // writer.println("@THIS");
            // writer.println("M=D");
            // writer.println("@FRAME");
            // writer.println("D=M-1");
            // writer.println("AM=D");
            // writer.println("D=M");
            // writer.println("@ARG");
            // writer.println("M=D");
            // writer.println("@FRAME");
            // writer.println("D=M-1");
            // writer.println("AM=D");
            // writer.println("D=M");
            // writer.println("@LCL");
            // writer.println("M=D");
            // writer.println("@RET");
            // writer.println("A=M");
            // writer.println("0;JMP");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeFunction(String strFunctionName, int locals) {
        try {
            writer.println("(" + strFunctionName + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < locals; i++) {
            writePushPop(Parser.CommandType.C_PUSH, "constant", 0);
        }
    }

    // Closes the output file
    public void close() {
        writer.close();
    }
}
