//
// $Id: Solver.java,v 1.1 2001/07/09 09:27:40 mdb Exp $

package com.samskivert.chuchu;

import java.io.*;
import java.util.Arrays;

public class Solver
{
    public Solver (Board board, String arrows)
    {
        _board = board;

        // parse the arrow description
        _arrows = new int[arrows.length()];
        for (int i = 0; i < _arrows.length; i++) {
            _arrows[i] = Piece.charToDirection(arrows.charAt(i));
        }
    }

    public void solve ()
    {
        int[] arrows = new int[Board.SQUARES];
        Arrays.fill(arrows, -1);
        solve(arrows, 0);
    }

    protected void solve (int[] arrows, int aidx)
    {
        for (int i = 0; i < arrows.length; i++) {
            // skip occupied positions
            if (_board.isOccupied(i)) {
                continue;
            }

            // skip positions that already have an arrow in them
            if (arrows[i] >= 0) {
                continue;
            }

            // note our arrow position
            arrows[i] = _arrows[aidx];

            // and if we placed the last arrow, execute
            if (aidx+1 == _arrows.length) {
                // copy out arrow configuration into a temporary array
                System.arraycopy(arrows, 0, _tmparrows, 0, arrows.length);

                // execute
                try {
                    int iters = _board.execute(_tmparrows, MAX_ITERS);
                    if ((_count++ % 100000) == 99999) {
                        System.out.print(".");
                        System.out.flush();
                    }
                    if (iters >= 0) {
                        System.out.println("\nSOLUTION: " + iters);
                        printArrangement(arrows);
                    }

                } catch (MaximumIterationsExceeded mie) {
                    // System.out.println("INFINITE LOOP");
                    // printArrangement(arrows);
                }

            } else {
                // otherwise place the next arrow
                solve(arrows, aidx+1);
            }

            // clear out our arrow position candidate
            arrows[i] = -1;
        }
    }

    protected void printArrangement (int[] arrows)
    {
        for (int i = 0; i < arrows.length; i++) {
            if (arrows[i] < 0) {
                continue;
            }
            System.out.println(Piece.directionToChar(arrows[i], true) +
                               " => +" + (i % Board.WIDTH) +
                               "+" + (i / Board.WIDTH) + ".");
        }
    }

    protected void printArrows (int[] arrows)
    {
    }

    public static void main (String[] args)
    {
        if (args.length < 2) {
            System.err.println("Usage: chuchu.Solver board_file arrow_desc");
            System.err.println("       where arrow_desc = [NESW]+");
            System.exit(-1);
        }

        try {
            Board board = new Board(false);
            FileReader fin = new FileReader(args[0]);
            BufferedReader in = new BufferedReader(fin);
            board.readFrom(in);

            Solver solver = new Solver(board, args[1]);
            solver.solve();

        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    protected Board _board;
    protected int[] _arrows;
    protected int[] _tmparrows = new int[Board.SQUARES];
    protected int _count;

    // if we haven't seen a solution in this many iterations, bail
    protected static final int MAX_ITERS = 150;
}
