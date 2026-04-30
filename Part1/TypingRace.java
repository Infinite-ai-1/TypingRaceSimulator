import java.util.concurrent.TimeUnit;
import java.lang.Math;
 
/**
 * A typing race simulation. Three typists race to complete a passage of text,
 * advancing character by character — or sliding backwards when they mistype.
 *
 * Originally written by Ty Posaurus. Bugs found and fixed — see comments marked [FIX].
 *
 * @author TyPosaurus (original), fixed by Nikhil David Joseph
 * @version 1.0
 */
public class TypingRace
{
    private int passageLength;
    private Typist seat1Typist;
    private Typist seat2Typist;
    private Typist seat3Typist;
 
    // Per-typist "just mistyped" flags — used to show [<] marker in the display
    // [FIX - new] Ty's version never tracked whether a typist had just mistyped,
    // so the [<] indicator mentioned in the spec was never shown.
    private boolean seat1JustMistyped;
    private boolean seat2JustMistyped;
    private boolean seat3JustMistyped;
 
    private static final double MISTYPE_BASE_CHANCE = 0.3;
    private static final int    SLIDE_BACK_AMOUNT   = 2;
    private static final int    BURNOUT_DURATION     = 3;
 
    // Accuracy adjustment applied to the winner after the race (spec requirement)
    private static final double WIN_ACCURACY_BOOST   = 0.02;
 
    /**
     * Constructor for objects of class TypingRace.
     *
     * @param passageLength the number of characters in the passage
     */
    public TypingRace(int passageLength)
    {
        this.passageLength = passageLength;
        seat1Typist = null;
        seat2Typist = null;
        seat3Typist = null;
    }
 
    /**
     * Seats a typist at the given seat number (1, 2, or 3).
     *
     * @param theTypist  the typist to seat
     * @param seatNumber the seat to place them in (1–3)
     */
    public void addTypist(Typist theTypist, int seatNumber)
    {
        if (seatNumber == 1)
        {
            seat1Typist = theTypist;
        }
        else if (seatNumber == 2)
        {
            seat2Typist = theTypist;
        }
        else if (seatNumber == 3)
        {
            seat3Typist = theTypist;
        }
        else
        {
            System.out.println("Cannot seat typist at seat " + seatNumber + " — there is no such seat.");
        }
    }
 
    /**
     * Starts the typing race.
     * All typists are reset, then the simulation runs turn by turn until
     * one typist completes the full passage. The winner is then announced.
     */
    public void startRace()
    {
        boolean finished = false;
 
        // [FIX 1] Ty only called resetToStart() on seat1 and seat2.
        // seat3Typist was never reset, so it could carry over state from a
        // previous race (wrong progress, lingering burnout). Added seat3 reset.
        seat1Typist.resetToStart();
        seat2Typist.resetToStart();
        seat3Typist.resetToStart(); // FIX: was missing
 
        while (!finished)
        {
            // Clear mistyped flags before each turn
            seat1JustMistyped = false;
            seat2JustMistyped = false;
            seat3JustMistyped = false;
 
            advanceTypist(seat1Typist, 1);
            advanceTypist(seat2Typist, 2);
            advanceTypist(seat3Typist, 3);
 
            printRace();
 
            if ( raceFinishedBy(seat1Typist) || raceFinishedBy(seat2Typist) || raceFinishedBy(seat3Typist) )
            {
                finished = true;
            }
 
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {}
        }
 
        // [FIX 4 / Task 2a] Print the winner and apply accuracy boost.
        // Ty left a TODO here and never implemented it.
        Typist winner = determineWinner();
        double previousAccuracy = winner.getAccuracy();
        winner.setAccuracy(previousAccuracy + WIN_ACCURACY_BOOST);
 
        System.out.println();
        System.out.println("  And the winner is... " + winner.getName() + "!");
        System.out.printf("  Final accuracy: %.2f (improved from %.2f)%n",
                          winner.getAccuracy(), previousAccuracy);
    }
 
    /**
     * Returns whichever typist has finished (progress >= passageLength).
     * If somehow multiple finish on the same turn, seat order is the tiebreak.
     *
     * @return the winning Typist
     */
    private Typist determineWinner()
    {
        if (raceFinishedBy(seat1Typist)) return seat1Typist;
        if (raceFinishedBy(seat2Typist)) return seat2Typist;
        return seat3Typist;
    }
 
    /**
     * Simulates one turn for a typist.
     *
     * @param theTypist  the typist to advance
     * @param seatNumber 1, 2, or 3 — used to record the mistyped flag
     */
    private void advanceTypist(Typist theTypist, int seatNumber)
    {
        if (theTypist.isBurntOut())
        {
            theTypist.recoverFromBurnout();
            return;
        }
 
        if (Math.random() < theTypist.getAccuracy())
        {
            theTypist.typeCharacter();
        }
 
        // [FIX 2] Ty's mistype probability was (accuracy * MISTYPE_BASE_CHANCE).
        // That makes HIGH-accuracy typists mistype MORE, which is backwards.
        // Correct formula: low accuracy → high mistype chance.
        // Fixed to: (1 - accuracy) * MISTYPE_BASE_CHANCE
        if (Math.random() < (1.0 - theTypist.getAccuracy()) * MISTYPE_BASE_CHANCE)
        {
            theTypist.slideBack(SLIDE_BACK_AMOUNT);
            // [FIX - new] Record that this typist just mistyped so [<] can be shown
            if (seatNumber == 1) seat1JustMistyped = true;
            else if (seatNumber == 2) seat2JustMistyped = true;
            else if (seatNumber == 3) seat3JustMistyped = true;
        }
 
        // Burnout check — probability scales with accuracy^2, capped near 0.05
        if (Math.random() < 0.05 * theTypist.getAccuracy() * theTypist.getAccuracy())
        {
            theTypist.burnOut(BURNOUT_DURATION);
        }
    }
 
    /**
     * Returns true if the given typist has completed the full passage.
     *
     * [FIX 3] Ty used == to compare progress against passageLength.
     * Progress can overshoot (e.g. typist at 39/40 types a character → progress
     * becomes 40, but if typeCharacter() is called again it could reach 41).
     * With ==, a typist at 41 would never be detected as finished and the race
     * would loop forever. Fixed to >= so any overshoot is caught correctly.
     *
     * @param theTypist the typist to check
     * @return true if their progress has reached or passed the passage length
     */
    private boolean raceFinishedBy(Typist theTypist)
    {
        // FIX: was == , must be >=
        return theTypist.getProgress() >= passageLength;
    }
 
    /**
     * Prints the current state of the race to the terminal.
     */
    private void printRace()
    {
        System.out.print('\u000C');
 
        System.out.println("  TYPING RACE — passage length: " + passageLength + " chars");
        multiplePrint('=', passageLength + 3);
        System.out.println();
 
        printSeat(seat1Typist, seat1JustMistyped);
        System.out.println();
 
        printSeat(seat2Typist, seat2JustMistyped);
        System.out.println();
 
        printSeat(seat3Typist, seat3JustMistyped);
        System.out.println();
 
        multiplePrint('=', passageLength + 3);
        System.out.println();
        System.out.println("  [~] = burnt out    [<] = just mistyped");
    }
 
    /**
     * Prints a single typist's lane, including burnout and mistype indicators.
     *
     * @param theTypist      the typist whose lane to print
     * @param justMistyped   true if this typist mistyped on the current turn
     */
    private void printSeat(Typist theTypist, boolean justMistyped)
    {
        // Clamp display progress so the typist never renders outside the track
        int displayProgress = Math.min(theTypist.getProgress(), passageLength);
        int spacesBefore = displayProgress;
        int spacesAfter  = passageLength - displayProgress;
 
        System.out.print('|');
        multiplePrint(' ', spacesBefore);
 
        System.out.print(theTypist.getSymbol());
        if (theTypist.isBurntOut())
        {
            System.out.print('~');
            spacesAfter--;
        }
        // [FIX - new] Show [<] when the typist just mistyped, as described in the spec
        if (justMistyped)
        {
            System.out.print('[');
            System.out.print('<');
            System.out.print(']');
            spacesAfter -= 3;
        }
 
        // Guard against negative spacesAfter (can happen at end of track)
        if (spacesAfter < 0) spacesAfter = 0;
 
        multiplePrint(' ', spacesAfter);
        System.out.print('|');
        System.out.print(' ');
 
        if (theTypist.isBurntOut())
        {
            System.out.print(theTypist.getName()
                + " (Accuracy: " + theTypist.getAccuracy() + ")"
                + " BURNT OUT (" + theTypist.getBurnoutTurnsRemaining() + " turns)");
        }
        else
        {
            System.out.print(theTypist.getName()
                + " (Accuracy: " + theTypist.getAccuracy() + ")");
        }
    }
 
    /**
     * Prints a character a given number of times.
     */
    private void multiplePrint(char aChar, int times)
    {
        int i = 0;
        while (i < times)
        {
            System.out.print(aChar);
            i = i + 1;
        }
    }
 
    /**
     * A simple main method to run the race.
     */
    public static void main(String[] args)
    {
        TypingRace race = new TypingRace(40);
        race.addTypist(new Typist('①', "TURBOFINGERS", 0.85), 1);
        race.addTypist(new Typist('②', "QWERTY_QUEEN",  0.60), 2);
        race.addTypist(new Typist('③', "HUNT_N_PECK",   0.30), 3);
        race.startRace();
    }
}
 