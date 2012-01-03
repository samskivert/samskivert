//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * A wrapper around Sun's internal signal handling bits that can be used to avoid a) an explicit
 * dependency on Sun's internal stuff (assuming they deprecate that and replace it with something
 * actually supported some day) and b) a slew of unsuppressable warnings every time you compile
 * your project.
 */
public class SignalUtil
{
    public static enum Number {
        HUP(1), /* Hangup (POSIX). */
        INT(2), /* Interrupt (ANSI). */
        QUIT(3), /* Quit (POSIX). */
        ILL(4), /* Illegal instruction (ANSI). */
        TRAP(5), /* Trace trap (POSIX). */
        ABRT(6), /* Abort (ANSI). */
        IOT(6), /* IOT trap (4.2 BSD). */
        BUS(7), /* BUS error (4.2 BSD). */
        FPE(8), /* Floating-point exception (ANSI). */
        KILL(9), /* Kill, unblockable (POSIX). */
        USR1(10), /* User-defined signal 1 (POSIX). */
        SEGV(11), /* Segmentation violation (ANSI). */
        USR2(12), /* User-defined signal 2 (POSIX). */
        PIPE(13), /* Broken pipe (POSIX). */
        ALRM(14), /* Alarm clock (POSIX). */
        TERM(15), /* Termination (ANSI). */
        STKFLT(16), /* Stack fault. */
        CHLD(17), /* Child status has changed (POSIX). */
        CONT(18), /* Continue (POSIX). */
        STOP(19), /* Stop, unblockable (POSIX). */
        TSTP(20), /* Keyboard stop (POSIX). */
        TTIN(21), /* Background read from tty (POSIX). */
        TTOU(22), /* Background write to tty (POSIX). */
        URG(23), /* Urgent condition on socket (4.2 BSD). */
        XCPU(24), /* CPU limit exceeded (4.2 BSD). */
        XFSZ(25), /* File size limit exceeded (4.2 BSD). */
        VTALRM(26), /* Virtual alarm clock (4.2 BSD). */
        PROF(27), /* Profiling alarm clock (4.2 BSD). */
        WINCH(28), /* Window size change (4.3 BSD, Sun). */
        IO(29), /* I/O now possible (4.2 BSD). */
        POLL(29), /* Pollable event occurred (System V). */
        PWR(30), /* Power failure restart (System V). */
        SYS(31); /* Bad system call. */

        public int signo () {
            return _signo;
        }

        Number (int signo) {
            _signo = signo;
        }

        protected int _signo;
    }

    public static interface Handler {
        public void signalReceived (Number number);
    }

    public static final void register (final Number number, final Handler handler)
    {
        Signal.handle(new Signal(number.toString()), new SignalHandler() {
            public void handle (Signal sig) {
                handler.signalReceived(number);
            }
        });
    }
}
