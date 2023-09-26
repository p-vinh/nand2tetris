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

(LOOP)
	@SCREEN
	D=A
	@R0
	M=D
	

	@KBD
	D=M
	// If keyboard is pressed, then D > 0, jump to BLACK
	@BLACK
	D; JGT // If D > 0, then jump to BLACK

	// If keyboard is not pressed, then D = 0 and jump to WHITE
	@WHITE
	D; JEQ // If D = 0, then jump to WHITE

(BLACK)
	@R1
	M=-1 // Turn row into black
	@FILL
	0; JMP

(WHITE)
	@R1
	M=1 // Turn row into white
	@FILL
	0; JMP

(FILL)
	// Load 0 or 1 from R1 into D
	@R1
	D=M

	// Load the address of the screen into A
	// Set memory address of the screen to black or white
	@R0
	A=M
	M=D

	@R0 // Increment to next pixel
	D=M+1
	M=M+1

	// We access keyboard because it is the last memory address in the screen and subtract it from D
	@KBD // If keyboard is pressed, then D > 0, jump to BLACK
	D=A-D

	// Increment R0 to next pixel
	@R0
	A=M

	@FILL
	D; JGT // IF A=0 EXIT AS THE WHOLE SCREEN HAS BEEN FILLED
	@LOOP
	0; JMP