// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

// Writes the pixels from the bottom right corner to the top left corner
// Since we are adding 8192 to the SCREEN's first address, we are writing the pixels from the bottom right corner to the top left corner
(INIT)
	@8192 // We load D with 8192 because 32 * 256 = 8192. 32 is the number of pixels in a row, and 256 is the number of rows
	D=A
	@0 // Count of the max number of pixels that can be colored 8192
	M=D

(LOOP)
	@0
	M=M-1 // Decrement count by 1
	D=M // D = Count

	@INIT
	D; JLT // If D < 0, then jump to INIT, to reset the count

	@KBD
	D=M
	@WHITE
	D; JEQ // If D = 0, then jump to WHITE
	// If keyboard is pressed, then D > 0, jump to BLACK
	@BLACK
	0; JMP

(BLACK)
	@SCREEN  // Load D with SCREEN's first address
	D=A

	@0
	A=D+M // A = 16384 + Memory = New Address. Adds SCREEN's first address to the count in order to color the current pixel black
	M=-1 // Color pixel black

	@LOOP
	0; JMP

(WHITE)
	@SCREEN // Load D with SCREEN's first address
	D=A

	@0
	A=D+M // A = 16384 + Memory = New Address. Adds SCREEN's first address to the count in order to color the current pixel white
	M=0 // Color pixel white

	@LOOP
	0; JMP
