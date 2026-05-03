/**
 * This is the Typist class, it stores everything about one typist in the race.
 * Things like their name, symbol, how far they've gone, and if they're burnt out.
 *
 * @author Nikhil David Joseph
 * @version 1.0
 */
public class Typist
{
    private char symbol;
    private String name;
    private double accuracy;
    private int progress;
    private boolean burntOut;
    private int burnoutTurnsLeft;

    /**
     * Makes a new typist. Takes a symbol, name and accuracy.
     * Progress starts at 0 and burnout is off by default.
     *
     * @param typistSymbol single character to show on the track
     * @param typistName their name
     * @param typistAccuracy number between 0.0 and 1.0
     */
    public Typist(char typistSymbol, String typistName, double typistAccuracy)
    {
        symbol = typistSymbol;
        name = typistName;
        progress = 0;
        burntOut = false;
        burnoutTurnsLeft = 0;
        setAccuracy(typistAccuracy);
    }

    // sets the typist as burnt out for however many turns
    public void burnOut(int turns)
    {
        burntOut = true;
        burnoutTurnsLeft = turns;
    }

    // counts burnout down each turn, resets it when it hits 0
    public void recoverFromBurnout()
    {
        if (burntOut)
        {
            burnoutTurnsLeft--;
            if (burnoutTurnsLeft <= 0)
            {
                burnoutTurnsLeft = 0;
                burntOut = false;
            }
        }
    }

    // resets everything so they can start a fresh race
    public void resetToStart()
    {
        progress = 0;
        burntOut = false;
        burnoutTurnsLeft = 0;
    }

    // moves forward by one character
    public void typeCharacter()
    {
        progress++;
    }

    // moves back by amount but never goes below 0
    public void slideBack(int amount)
    {
        if (amount <= 0) return;
        progress = progress - amount;
        if (progress < 0)
        {
            progress = 0;
        }
    }

    // sets accuracy, but clamps it so it stays between 0 and 1
    public void setAccuracy(double newAccuracy)
    {
        if (newAccuracy < 0.0)
        {
            accuracy = 0.0;
        }
        else if (newAccuracy > 1.0)
        {
            accuracy = 1.0;
        }
        else
        {
            accuracy = newAccuracy;
        }
    }

    public void setSymbol(char newSymbol)
    {
        symbol = newSymbol;
    }

    public double getAccuracy()
    {
        return accuracy;
    }

    public int getProgress()
    {
        return progress;
    }

    public String getName()
    {
        return name;
    }

    public char getSymbol()
    {
        return symbol;
    }

    public int getBurnoutTurnsRemaining()
    {
        return burnoutTurnsLeft;
    }

    public boolean isBurntOut()
    {
        return burntOut;
    }
}