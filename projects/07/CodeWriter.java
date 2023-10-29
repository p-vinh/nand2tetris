import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;

public class CodeWriter {
    private PrintWriter writer;
    private int mJumpNumber = 0;
    private String file;
    private Map<String, String> segmentMap = new HashMap<String, String>();

    public CodeWriter(File file) {
        try {
            File outputFile = new File(file.getAbsolutePath().split(".vm")[0] + ".asm");
            writer = new PrintWriter(new FileWriter(outputFile));
            this.file = file.getName();
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

    public void setFileName(String fileName) {
        this.file = fileName;
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
                        writer.println("@" + index + "\n" + "D=A\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
                        break;
                    case "static":
                        writer.println(formatPush2(String.valueOf(16 + index)).toString());
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
                        writer.println("@" + index + "\n" + "D=A\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n");
                        break;
                    case "static":
                        writer.println(formatPop2(String.valueOf(16 + index)).toString());
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

    // Closes the output file
    public void close() {
        writer.close();
    }
}
