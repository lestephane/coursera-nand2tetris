// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.

// Pseudo code:
//
// result = 0
// for (i = 0; i<R0; i++) {
//   result = result + R1
// }
// R2 = result
//

@i          // i = 0
M=0

@result     // result = 0 
M=0

(LOOP)
@i          // i<R0
D=M

@R0
D=D-M

@END
D;JGE

@R1         // result = result + R1
D=M

@result
M=D+M

@i          // i++
M=M+1

@LOOP
0;JMP

@result     // R2 = result
D=M

@R2
M=D

@END
0;JMP