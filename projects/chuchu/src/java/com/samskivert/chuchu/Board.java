//
// $Id: Board.java,v 1.1 2001/07/09 09:27:40 mdb Exp $

package com.samskivert.chuchu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The board contains information on wall placement, board features (holes
 * and rockets) and cat and mouse coordinates, which are maintained via
 * separate <code>Piece</code> object instances.
 */
public class Board
{
    /** The width of a standard Chu Chu board in squares. */
    public static final int WIDTH = 12;

    /** The height of a standard Chu Chu board in squares. */
    public static final int HEIGHT = 9;

    /** The total number of squares on the board. */
    public static final int SQUARES = WIDTH * HEIGHT;

    /**
     * Constructs a blank board ready to be manipulated or configured from
     * a file.
     */
    public Board (boolean debug)
    {
        _debug = debug;
    }

    /**
     * Returns true if the specified board position is occupied by a
     * feature (hole or rocket).
     */
    public boolean isOccupied (int index)
    {
        return (_board[index] & FEATURE_MASK) != 0;
    }

    /**
     * Sets the board in motion with the specified set of arrow tiles. The
     * board will continue executing until one of the following conditions
     * occurs:
     *
     * <ul>
     * <li>All mice have reached rockets.
     * <li>A mouse collides with a cat.
     * <li>A cat reaches a rocket.
     * <li>A mouse falls into a hole.
     * </ul>
     *
     * If the maximum number of iterations is reached before one of these
     * conditions occurs, an exception is thrown so that the caller can
     * make a note of this failure to terminate and potentially mark this
     * arrow configuration for a later repeat attempt with a higher number
     * of iterations.
     *
     * @param arrows an array that is <code>SQUARES</code> long with a
     * <code>Piece</code> direction constant at every coordinate where an
     * arrow exists facing in the specified direction. The array should be
     * set to -1 where there are no arrows. This array will be modified in
     * the course of executing the board.
     * @param maxiters the maximum number of iterations that the game is
     * allowed to execute before aborting.
     *
     * @return >0 (number of iterations needed for solution) if the game
     * terminated with all mice successfully reaching rockets, -1 if some
     * failure condition occurred.
     *
     * @exception MaximumIterationsExceeded thrown if the game does not
     * terminate within the maximum number of iterations.
     */
    public int execute (int[] arrows, int maxiters)
        throws MaximumIterationsExceeded
    {
        // clear out our position array
        Arrays.fill(_aboard, 0);

        // duplicate our pieces and make a note of their starting positions
        for (int i = 0; i < _cats.length; i++) {
            Piece piece = _cats[i];
            _aboard[piece.getIndex()] = CAT_ID;
            _acats[i].init(piece);
        }
        for (int i = 0; i < _mice.length; i++) {
            Piece piece = (Piece)_mice[i].clone();
            _aboard[piece.getIndex()] = MOUSE_ID;
            _amice[i].init(piece);
        }

        // eliminate frozen pieces

        // adjust the positions of all of the pieces as if they just
        // landed on their respective squares
        for (int i = 0; i < _acats.length; i++) {
            _acats[i].orientation = adjustOrientation(_acats[i], arrows, true);
        }
        for (int i = 0; i < _amice.length; i++) {
            _amice[i].orientation = adjustOrientation(_amice[i], arrows, false);
        }

        // start up the cellular automaton and see what happens
        for (int iter = 0; iter < maxiters; iter++) {
            // first move the cats
            for (int c = 0; c < _acats.length; c++) {
                Piece cat = _acats[c];
                // System.out.println("Examining cat: " + cat);

                // skip inactive cats
                if (!cat.active) {
                    continue;
                }

                // update the cat's offset
                cat.offset = (cat.offset + 2) % 3;

                // if the cat was in the limbo state, he remains in the
                // same square, otherwise he moves in the direction of his
                // orientation
                if (cat.offset == 1) {
                    continue;
                }

                // clear out our previous board position
                _aboard[cat.getIndex()] -= CAT_ID;

                // move to our new position
                cat.x += X_ADJUSTS[cat.orientation];
                cat.y += Y_ADJUSTS[cat.orientation];

                // check to see if we just moved into a hole or a rocket
                int cidx = cat.getIndex();
                if ((_board[cidx] & HOLE_MASK) != 0) {
                    cat.active = false;

                } else if ((_board[cidx] & ROCKET_MASK) != 0) {
                    // too bad, so sad
                    return -1;

                } else {
                    // we need to make sure we're not swapping spots with
                    // any mice because a precise orientation of a cat and
                    // mouse face to face will result in their swapping
                    // positions which is illegal
                    for (int m = 0; m < _amice.length; m++) {
                        Piece mouse = _amice[m];
                        if (mouse.active && (mouse.x == cat.x) &&
                            (mouse.y == cat.y) &&
                            (mouse.orientation == ((cat.orientation+2)%4))) {
                            // bang! howdy partner!
                            return -1;
                        }
                    }
                    // we didn't run into anything, so we update the board
                    // with our new position
                    _aboard[cidx] += CAT_ID;
                    // since we're in a new square, we need to potentially
                    // adjust the orientation of le chat
                    cat.orientation = adjustOrientation(cat, arrows, true);
                }
            }

            // now move the meeses
            int active = 0;
            for (int m = 0; m < _amice.length; m++) {
                Piece mouse = _amice[m];
                // System.out.println("Examining mouse: " + mouse);

                // skip inactive mice
                if (!mouse.active) {
                    continue;
                }

                // move to our new position
                mouse.x += X_ADJUSTS[mouse.orientation];
                mouse.y += Y_ADJUSTS[mouse.orientation];

                // check to see if we just moved into a hole or a rocket
                // or a cat
                int midx = mouse.getIndex();
                if ((_board[midx] & HOLE_MASK) != 0) {
                    // too bad, so sad
                    return -1;

                } else if ((_board[midx] & ROCKET_MASK) != 0) {
                    // whee!
                    mouse.active = false;

                } else if ((_aboard[midx] & CAT_MASK) != 0) {
                    // too bad, so sad
                    return -1;

                } else {
                    // since we're in a new square, we need to potentially
                    // adjust the orientation of le mouse
                    mouse.orientation = adjustOrientation(mouse, arrows, false);
                    // we're still active, make a note of it
                    active++;
                }
            }

            // if we have no more active mice, the board worked. yay!
            if (active == 0) {
                return iter;
            }

            // print out our board position
            if (_debug) {
                System.out.println("After iteration " + iter + ".");
                writeTo(System.out, _acats, _amice);
            }
        }

        throw new MaximumIterationsExceeded();
    }

    /**
     * Examines the walls and arrows in the vicinity of the supplied piece
     * and returns the new orientation for that piece. Depending on the
     * value of <code>deduct</code> this function will also reduce the
     * potency of arrows that reflect a piece 180 degrees.
     *
     * @param piece the piece whose orientation will be adjusted.
     * @param arrows the arrow information currently in effect.
     * @param deduct if true, deduct one unit of potency from an arrow
     * that reflects the piece 180 degrees. Arrows have only two units of
     * potency and will thus disappear after two such reflections.
     *
     * @return the new orientation for the piece in question.
     */
    protected int adjustOrientation (Piece piece, int[] arrows, boolean deduct)
    {
        int orient = piece.orientation;
        int pos = piece.getIndex();

        // if there's an arrow, we need to first account for that
        int aorient = arrows[pos];
        if (aorient >= 0) {
            aorient = (aorient & 0xF);
            if (aorient == ((orient + 2) % 4)) {
                // deduct one from the potency of this arrow
                if ((arrows[pos] & 0x10) != 0) {
                    // we've already deducted one, wipe it out
                    arrows[pos] = -1;
                } else {
                    arrows[pos] |= 0x10;
                }
            }
            orient = aorient;
            // System.out.println("Reoriented " + piece);
        }

        // now account for the walls. if there's a wall in front of us, we
        // need to examine things further
        int fpos = (pos + FRONT_OFFSET[orient]) % SQUARES;
        if ((_board[fpos] & FRONT_MASK[orient]) == 0) {
            // nothing in front, we're done
            return orient;
        }

        int rpos = (pos + RIGHT_OFFSET[orient]) % SQUARES;
        if ((_board[rpos] & RIGHT_MASK[orient]) == 0) {
            // nothing on our right, so we rotate right and we're done
            // System.out.println("Right turned " + piece);
            return (orient + 1) % 4;
        }

        int lpos = (pos + LEFT_OFFSET[orient]) % SQUARES;
        if ((_board[lpos] & LEFT_MASK[orient]) == 0) {
            // nothing on our left, so we rotate left and we're done
            // System.out.println("Left turned " + piece);
            return (orient + 3) % 4;
        }

        // otherwise we turn around
        // System.out.println("Reversed " + piece);
        return (orient + 2) % 4;
    }

    /**
     * Reads in a text description of a Chu Chu board which should first
     * be a grid of 12x9 characters containing the wall information, aside
     * a grid of 12x9 characters containing the feature information
     * (holes, rockets, cats, mice). The two grids are separated by one
     * space. The wall grid contains L, | and _ to indicate left and
     * bottom, left only and bottom only respectively. The feature grid
     * contains H, R, N/E/W/S and n/e/w/s. Uppercase compass directions
     * represent cats and lowercase directions represent mice. All
     * wall-less and featureless squares are represented by a dot. For
     * example, the first puzzle in the Mania stage would be represented
     * like so:
     *
     * <pre>
     * |._......... e...........
     * |L|.L......| ............
     * |._|._...L_. ............
     * |._..||_.._. ............
     * |L.....L|.|| ............
     * |||....._.L| ..SNS.NSN...
     * |L|_.....|.. ......R.....
     * |.._|....... ............
     * L___________ ............
     * </pre>
     *
     * Note that because the entire board is outlined in walls, the upper
     * and right walls need not be communicated in the grid. The lower and
     * left walls will automatically be added even if they are not
     * specified in the input.
     */
    public void readFrom (BufferedReader input)
        throws IOException
    {
        ArrayList cats = new ArrayList();
        ArrayList mice = new ArrayList();
        String line;

        for (int h = 0; h < HEIGHT; h++) {
            line = input.readLine();
            if (line == null) {
                throw new IOException("Ran out of input before parsing " +
                                      "entire board.");
            }

            // parse the wall definition
            for (int w = 0; w < WIDTH; w++) {
                char pos = line.charAt(w);
                int coord = h*WIDTH + w;
                switch (pos) {
                case 'L':
                    _board[coord] |= BOTH_WALLS_MASK;
                    break;

                case '|':
                    _board[coord] |= LEFT_WALL_MASK;
                    break;

                case '_':
                    _board[coord] |= BOTTOM_WALL_MASK;
                    break;

                case '.':
                case ' ':
                    // blank space, do nothing
                    break;

                default:
                    String msg = "Unexpected wall character '" + pos +
                        "' at position +" + w + "+" + h + ".";
                    throw new IOException(msg);
                }
            }

            // parse the board features
            for (int w = 0; w < WIDTH; w++) {
                char pos = line.charAt(w + WIDTH + 1);
                int coord = h*WIDTH + w;
                switch (pos) {
                case 'R':
                    _board[coord] |= ROCKET_MASK;
                    break;

                case 'H':
                    _board[coord] |= HOLE_MASK;
                    break;

                case 'N':
                case 'E':
                case 'W':
                case 'S':
                    cats.add(new Piece(Piece.charToDirection(pos), w, h));
                    break;

                case 'n':
                case 'e':
                case 'w':
                case 's':
                    mice.add(new Piece(Piece.charToDirection(pos), w, h));
                    break;

                case '.':
                case ' ':
                    // blank space, do nothing
                    break;

                default:
                    String msg = "Unexpected feature character '" + pos +
                        "' at position +" + w + "+" + h + ".";
                    throw new IOException(msg);
                }
            }
        }

        // stick our pieces into an array
        _cats = new Piece[cats.size()];
        cats.toArray(_cats);
        _mice = new Piece[mice.size()];
        mice.toArray(_mice);

        // we'll use these later
        _acats = new Piece[cats.size()];
        for (int i = 0; i < _cats.length; i++) {
            Piece piece = (Piece)_cats[i].clone();
            _acats[i] = piece;
        }
        _amice = new Piece[mice.size()];
        for (int i = 0; i < _mice.length; i++) {
            Piece piece = (Piece)_mice[i].clone();
            _amice[i] = piece;
        }
    }

    /**
     * Outputs the board in a format in which it can be read back in via
     * <code>readFrom()</code>.
     *
     * @see #readFrom
     */
    public void writeTo (PrintStream out)
    {
        writeTo(out, _cats, _mice);
    }

    protected void writeTo (PrintStream out, Piece[] cats, Piece[] mice)
    {
        for (int h = 0; h < HEIGHT; h++) {
            // print the wall description
            for (int w = 0; w < WIDTH; w++) {
                int pos = h*WIDTH + w;
                switch (_board[pos] & BOTH_WALLS_MASK) {
                case LEFT_WALL_MASK:
                    out.print("|");
                    break;
                case BOTTOM_WALL_MASK:
                    out.print("_");
                    break;
                case BOTH_WALLS_MASK:
                    out.print("L");
                    break;
                default:
                    out.print(".");
                    break;
                }
            }

            // separate the grids by a space
            out.print(" ");

            // print the feature description
            Piece p;
            for (int w = 0; w < WIDTH; w++) {
                int pos = h*WIDTH + w;
                if ((_board[pos] & ROCKET_MASK) != 0) {
                    out.print("R");
                } else if ((_board[pos] & HOLE_MASK) != 0) {
                    out.print("H");
                } else if ((p = pieceAtCoordinates(cats, w, h)) != null) {
                    out.print(Piece.directionToChar(p.orientation, true));
                } else if ((p = pieceAtCoordinates(mice, w, h)) != null) {
                    out.print(Piece.directionToChar(p.orientation, false));
                } else {
                    out.print(".");
                }
            }

            // output a newline
            out.println("");
        }
    }

    public void dump (PrintStream out)
    {
        for (int h = 0; h < HEIGHT; h++) {
            for (int w = 0; w < WIDTH; w++) {
                out.print(HEX[_board[h*WIDTH + w]]);
            }
            out.println("");
        }
    }

    /**
     * Returns any piece located at the requested coordinates, or null if
     * no piece is at those coordinates.
     */
    protected Piece pieceAtCoordinates (Piece[] pieces, int x, int y)
    {
        for (int i = 0; i < pieces.length; i++) {
            if (pieces[i].x == x && pieces[i].y == y) {
                return pieces[i];
            }
        }
        return null;
    }

    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: chuchu.Board board_file");
            System.exit(-1);
        }

        try {
            Board board = new Board(true);
            FileReader fin = new FileReader(args[0]);
            BufferedReader in = new BufferedReader(fin);
            board.readFrom(in);
            board.writeTo(System.out);

            int[] arrows = new int[SQUARES];
            Arrays.fill(arrows, -1);
            arrows[2*WIDTH+0] = Piece.EAST;
            arrows[0*WIDTH+1] = Piece.WEST;
            if (board.execute(arrows, 100) >= 0) {
                System.out.println("Success!");
            } else {
                System.out.println("Failure!");
            }

        } catch (MaximumIterationsExceeded mie) {
            System.out.println("Maximum iterations exceeded.");

        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    /**
     * The board is represented by an integer for each square. Individual
     * bits of that integer represent activated walls and features.
     */
    protected int[] _board = new int[SQUARES];

    // the pieces on the board (cats and mice) are maintained in these
    // arrays
    protected Piece[] _cats;
    protected Piece[] _mice;

    // temporary copies of our board and pieces for use when executing
    protected int[] _aboard = new int[SQUARES];
    protected Piece[] _acats;
    protected Piece[] _amice;

    protected boolean _debug;

    protected static final int LEFT_WALL_MASK = 0x01;
    protected static final int BOTTOM_WALL_MASK = 0x02;
    protected static final int BOTH_WALLS_MASK =
        LEFT_WALL_MASK|BOTTOM_WALL_MASK;

    protected static final int ROCKET_MASK = 0x04;
    protected static final int HOLE_MASK = 0x08;
    protected static final int FEATURE_MASK = ROCKET_MASK|HOLE_MASK;

    protected static final char[] HEX = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    protected static final int CAT_ID = 0x100;
    protected static final int MOUSE_ID = 0x01;
    protected static final int CAT_MASK = 0xFF00;

    protected static final int[] X_ADJUSTS = { 0, 1, 0, -1 };
    protected static final int[] Y_ADJUSTS = { -1, 0, 1, 0 };

    protected static final int[] FRONT_OFFSET = { WIDTH*(HEIGHT-1), 1, 0, 0 };
    protected static final int[] FRONT_MASK = {
        BOTTOM_WALL_MASK, LEFT_WALL_MASK, BOTTOM_WALL_MASK, LEFT_WALL_MASK };

    protected static final int[] RIGHT_OFFSET = { 1, 0, 0, WIDTH*(HEIGHT-1) };
    protected static final int[] RIGHT_MASK = {
        LEFT_WALL_MASK, BOTTOM_WALL_MASK, LEFT_WALL_MASK, BOTTOM_WALL_MASK };

    protected static final int[] LEFT_OFFSET = { 0, WIDTH*(HEIGHT-1), 1, 0 };
    protected static final int[] LEFT_MASK = {
        LEFT_WALL_MASK, BOTTOM_WALL_MASK, LEFT_WALL_MASK, BOTTOM_WALL_MASK };
}
