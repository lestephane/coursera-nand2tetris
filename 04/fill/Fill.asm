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

// Pseudo code:
//
// color = 0        // 0 = white, -1 = black
//  while (true) {
//      key = RAM[KBD]
//      if (key == 0)
//          color = 0
//      else
//          color = -1
//
//      for (int i = screen_begin_address ; i < (screen_begin_address + 8192) ; i++) { // 256 lines * 512 columns / 16 bits (1 bit per pixel)
//          RAM[i] = color
//      }
//  }
//
(BEGIN)
    @color              // color = 0
    M=0

    @SCREEN
    D=A

    @screen_begin_address
    M=D

    @8192
    D=D+A

    @screen_end_address
    M=D

(LOOP)

    @KBD
    D=M

    @USE_WHITE_COLOR
    D;JEQ               // if ( key == 0 ) ...

(USE_BLACK_COLOR)

    @color              // else
    M=-1                //    color = -1

    @DRAW_SCREEN
    0;JMP

(USE_WHITE_COLOR)       // ...then color = 0

    @color
    M=0;

(DRAW_SCREEN)

    @screen_begin_address
    D=M

    @i                  // i = screen_begin_address
    M=D

(DRAW_LOOP)
    @i
    D=M

    @screen_end_address
    D=D-M

    @DRAW_LOOP_END    // i < (screen_begin_address + 8192)
    D;JGE

    @color
    D=M

    @i
    A=M                 // SCREEN[<current position>] = color
    M=D

    @i
    M=M+1               // i++

    @DRAW_LOOP
    0;JMP

(DRAW_LOOP_END)

    @LOOP
    0;JMP
