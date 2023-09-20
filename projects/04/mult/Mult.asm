// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

// Put your code here.

// MULT RAM[0] * RAM[R1] = RAM[R2]

(main)
	// Load the value from R0 into D
	@R0
	D=M // D=R0

	// Check if R0 >= 0
	@endR0Check
	D; JLT // R0<0
	// Load the value from R1 into D
	@R1
	D=M 
	// Check if R1 >= 0
	@endR1Check
	D; JLT // R1<0

	// Load 0 into R2
	@R2
	M=0
(loop)
	// Check if R0=0
	@R0
	D=M // D=R0

	@endLoopCheck
	D; JLE // R0<=0


	// Calculate R2=R2+R0
	@R2
	M=M+D // R2=R2+R0

	@R1
	M=M-1 // R1=R1-1

	@loop
	0; JMP // Jump to loop

(endLoopCheck)
    @end
    0; JMP // Jump back to the loop

(endR0Check)
    @end
    0; JMP // Jump to end

(endR1Check)
    @end
    0; JMP // Jump to end

(end)
	@end
	0 ; JMP 	// loop indefinitely






// 	// Check if R0*R1 < 32768
// 	@32768
// 	D=A // Load 32768 into D

// 	@R2
// 	D=M-D // D=R2-32768
// 	@SET
// 	D; JLT // R2<32768; if R2>0, jump to SET
// (SET)
// 	@R2
// 	M=0 // R2=0