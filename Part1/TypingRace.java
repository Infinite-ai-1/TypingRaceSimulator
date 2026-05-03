import java.util.concurrent.TimeUnit;
import java.lang.Math;

/**
 * This runs the actual typing race.
 * Was originally written by Ty Posaurus and had quite a few bugs in it.
 * I went through and fixed them all, each one is marked with [FIX] below.
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

    // these track if each typist mistyped this turn so we can show [<] on screen
    // [FIX: new] Ty never had these so the [<] marker was never shown at all
    private boolean seat1Mistyped;
    private boolean seat2Mistyped;
    private boolean seat3Mistyped;

    private static final double MISTYPE_CHANCE = 0.3;
    private static final int SLIDE_BACK = 2;
    private static final int BURNOUT_TURNS = 3;
    private static final double WIN_BOOST = 0.02;

    /**
     * Sets up the race with a passage length.
     *
     * @param passageLength how many characters long the passage is
     */
    public TypingRace(int passageLength)
    {
        this.passageLength = passageLength;
        seat1Typist = null;
        seat2Typist = null;
        seat3Typist = null;
    }

    /**
     * Puts a typist into one of the three seats.
     *
     * @param theTypist the typist to add
     * @param seatNumber which seat, 1 to 3
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
            System.out.println("No seat " + seatNumber + " exists.");
        }
    }

    /**
     * Starts the race. Resets all typists, runs turn by turn, then prints the winner.
     */
    public void startRace()
    {
        boolean finished = false;

        // [FIX 1: Ty only reset seat1 and seat2, seat3 kept its state from the last race]
        seat1Typist.resetToStart();
        seat2Typist.resetToStart();
        seat3Typist.resetToStart();

        while (!finished)
        {
            seat1Mistyped = false;
            seat2Mistyped = false;
            seat3Mistyped = false;

            doTurn(seat1Typist, 1);
            doTurn(seat2Typist, 2);
            doTurn(seat3Typist, 3);

            printRace();

            if (hasFinished(seat1Typist) || hasFinished(seat2Typist) || hasFinished(seat3Typist))
            {
                finished = true;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {}
        }

        // [FIX 4: Ty left a TODO here and never actually printed the winner]
        Typist winner = getWinner();
        double oldAccuracy = winner.getAccuracy();
        winner.setAccuracy(oldAccuracy + WIN_BOOST);

        System.out.println();
        System.out.println("  And the winner is... " + winner.getName() + "!");
        System.out.printf("  Final accuracy: %.2f (improved from %.2f)%n",
                          winner.getAccuracy(), oldAccuracy);
    }

    // returns whoever crossed the finish line, seat order breaks any ties
    private Typist getWinner()
    {
        if (hasFinished(seat1Typist)) return seat1Typist;
        if (hasFinished(seat2Typist)) return seat2Typist;
        return seat3Typist;
    }

    // runs one turn for a typist, handles burnout, typing, mistypes and burnout chance
    private void doTurn(Typist t, int seat)
    {
        if (t.isBurntOut())
        {
            t.recoverFromBurnout();
            return;
        }

        if (Math.random() < t.getAccuracy())
        {
            t.typeCharacter();
        }

        // [FIX 2: Ty had accuracy * MISTYPE_CHANCE which meant better typists mistyped MORE
        // that's completely backwards, fixed it so lower accuracy means higher mistype chance]
        if (Math.random() < (1.0 - t.getAccuracy()) * MISTYPE_CHANCE)
        {
            t.slideBack(SLIDE_BACK);
            if (seat == 1) seat1Mistyped = true;
            else if (seat == 2) seat2Mistyped = true;
            else if (seat == 3) seat3Mistyped = true;
        }

        // higher accuracy typists risk burnout more often but it's not a guarantee, adds some extra tension
        if (Math.random() < 0.05 * t.getAccuracy() * t.getAccuracy())
        {
            t.burnOut(BURNOUT_TURNS);
        }
    }

    // [FIX 3: Ty used == which breaks if progress goes past the passage length
    // changed to >= so it still catches them even if they overshoot]
    private boolean hasFinished(Typist t)
    {
        return t.getProgress() >= passageLength;
    }

    // prints the whole race track to the terminal
    private void printRace()
    {
        System.out.print('\u000C');
        System.out.println("  TYPING RACE : passage length: " + passageLength + " chars");
        repeatChar('=', passageLength + 3);
        System.out.println();

        printLane(seat1Typist, seat1Mistyped);
        System.out.println();
        printLane(seat2Typist, seat2Mistyped);
        System.out.println();
        printLane(seat3Typist, seat3Mistyped);
        System.out.println();

        repeatChar('=', passageLength + 3);
        System.out.println();
        System.out.println("  [~] = burnt out    [<] = just mistyped");
    }

    // prints one lane for a typist including their position and current status
    private void printLane(Typist t, boolean mistyped)
    {
        int pos = Math.min(t.getProgress(), passageLength);
        int spacesAfter = passageLength - pos;

        System.out.print('|');
        repeatChar(' ', pos);
        System.out.print(t.getSymbol());

        if (t.isBurntOut())
        {
            System.out.print('~');
            spacesAfter = spacesAfter - 1;
        }

        if (mistyped)
        {
            System.out.print("[<]");
            spacesAfter = spacesAfter - 3;
        }

        if (spacesAfter < 0) spacesAfter = 0;

        repeatChar(' ', spacesAfter);
        System.out.print("| ");

        if (t.isBurntOut())
        {
            System.out.print(t.getName() + " (Accuracy: " + t.getAccuracy() + ")"
                + " BURNT OUT (" + t.getBurnoutTurnsRemaining() + " turns)");
        }
        else
        {
            System.out.print(t.getName() + " (Accuracy: " + t.getAccuracy() + ")");
        }
    }

    // prints the same character n times
    private void repeatChar(char c, int n)
    {
        int i = 0;
        while (i < n)
        {
            System.out.print(c);
            i++;
        }
    }

    public static void main(String[] args)
    {
        TypingRace race = new TypingRace(40);
        race.addTypist(new Typist('1', "Speedyboi", 0.80), 1);
        race.addTypist(new Typist('2', "Speedygirl", 0.60), 2);
        race.addTypist(new Typist('3', "Slowboi", 0.30), 3);
        race.startRace();
    }
}