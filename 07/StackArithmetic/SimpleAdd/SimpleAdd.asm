// // This file is part of www.nand2tetris.org
// // and the book "The Elements of Computing Systems"
// // by Nisan and Schocken, MIT Press.
// // File name: projects/07/StackArithmetic/SimpleAdd/SimpleAdd.vm
// 
// // Pushes and adds two constants.
// push constant 7
@7
D=A
@SP
AM=M+1
A=A-1
M=D
// push constant 8
@8
D=A
@SP
AM=M+1
A=A-1
M=D
// add
@5
D=A
@0
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
@5
D=A
@1
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
@6
D=M
@5
M=D+M
@5
D=A
@0
A=D+A
D=M
@SP
AM=M+1
A=A-1
M=D
