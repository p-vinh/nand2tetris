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

(MAIN)
	// Load 0 into R2
	@R2	
	M=0

	// Load the value from R0 into D
	@R0
	D=M // D=R0
	// Check if R0 > 0
	@LOOP
	D; JGT // R0>0


	@END
	0; JMP

(LOOP)

	@R2
	D=M // D=R2

	// Calculate R1=R1+R2
	@R1
	D=D+M // R1=R1+R2
	@R2
	M=D // R2=R1+R2


	@R0
	D=M-1 // D=R0-1
	M=D

	@LOOP
	D; JGT // Jump to loop


(END)
	@END
	0 ;JMP 	// loop indefinitely
