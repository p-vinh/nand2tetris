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
	@R0
	r_zero=M // R0=0
	@R1
	r_one=M // R1=1

	@END
	r_zero; JGT // R0>=0
	r_one; JGT // R1>=0
	

(END)







