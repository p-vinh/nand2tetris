@256
D=A
@SP
M=D

@RETURN_LABEL0
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1

@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1

@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1

@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1

@SP
D=M
@5
D=D-A
@0
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Sys.init
0;JMP
(RETURN_LABEL0)
@111
D=A
@SP
A=M
M=D
@SP
M=M+1

@333
D=A
@SP
A=M
M=D
@SP
M=M+1

@888
D=A
@SP
A=M
M=D
@SP
M=M+1

@24
D=A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D

@19
D=A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D

@17
D=A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D

@19
D=M
@SP
A=M
M=D
@SP
M=M+1

@17
D=M
@SP
A=M
M=D
@SP
M=M+1

@SP
AM=M-1
D=M
A=A-1
M=M-D
@24
D=M
@SP
A=M
M=D
@SP
M=M+1

@SP
AM=M-1
D=M
A=A-1
M=M+D
