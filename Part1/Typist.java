/**
 * Represents a competitor in a typing race.
 *
 * Each typist has a name, a symbol for display, an accuracy rating,
 * a progress counter (which can go up or down), and a burnout state.
 *
 * @author Nikhil David Joseph
 * @version 1.0
 */
public class Typist
{
    // --- Fields ---
    private char   symbol;               // Single character shown on the race track
    private String name;                 // Display name (e.g. "TURBOFINGERS")
    private double accuracy;             // Typing accuracy, clamped to [0.0, 1.0]
    private int    progress;             // Characters correctly typed so far (>= 0)
    private boolean burntOut;            // True while the typist cannot type
    private int    burnoutTurnsRemaining; // Turns left in burnout state (0 when not burnt out)
 
    /**
     * Creates a new typist with the given symbol, name, and accuracy.
     * Progress starts at zero and the typist is not burnt out.
     *
     * @param typistSymbol   a single Unicode character (e.g. '①')
     * @param typistName     the typist's display name
     * @param typistAccuracy accuracy rating between 0.0 and 1.0
     */
    public Typist(char typistSymbol, String typistName, double typistAccuracy)
    {
        symbol   = typistSymbol;
        name     = typistName;
        progress = 0;
        burntOut = false;
        burnoutTurnsRemaining = 0;
        // Use setAccuracy so clamping logic is applied immediately
        setAccuracy(typistAccuracy);
    }
 
    // --- Mutators ---
 
    /**
     * Puts the typist into a burnt-out state for the given number of turns.
     * While burnt out the typist cannot type.
     *
     * @param turns how many turns the burnout lasts (should be > 0)
     */
    public void burnOut(int turns)
    {
        burntOut = true;
        burnoutTurnsRemaining = turns;
    }
 
    /**
     * Reduces the burnout counter by one turn.
     * When the counter reaches zero the typist fully recovers.
     * Has no effect if the typist is not currently burnt out.
     */
    public void recoverFromBurnout()
    {
        if (burntOut)
        {
            burnoutTurnsRemaining--;
            if (burnoutTurnsRemaining <= 0)
            {
                burnoutTurnsRemaining = 0;
                burntOut = false;
            }
        }
    }
 
    /**
     * Resets the typist to their initial race state.
     * Progress returns to zero and all burnout state is cleared.
     */
    public void resetToStart()
    {
        progress = 0;
        burntOut = false;
        burnoutTurnsRemaining = 0;
    }
 
    /**
     * Advances the typist forward by one character.
     * Should only be called when the typist is not burnt out.
     */
    public void typeCharacter()
    {
        progress++;
    }
 
    /**
     * Moves the typist backwards by the given number of characters (a mistype).
     * Progress is clamped at zero — the typist cannot slide off the start.
     *
     * Validation: if amount is negative this method does nothing, since a
     * negative slide-back would silently advance the typist, which is wrong.
     *
     * @param amount number of characters to slide back (must be positive)
     */
    public void slideBack(int amount)
    {
        if (amount <= 0) return; // Guard against nonsensical input
        progress -= amount;
        if (progress < 0)
        {
            progress = 0; // Clamp — cannot go behind the start line
        }
    }
 
    /**
     * Sets the accuracy rating, clamping values outside [0.0, 1.0].
     * Values below 0.0 are set to 0.0; values above 1.0 are set to 1.0.
     *
     * @param newAccuracy the new accuracy rating
     */
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
 
    /**
     * Sets the character used to represent this typist on the race display.
     *
     * @param newSymbol the new symbol character
     */
    public void setSymbol(char newSymbol)
    {
        symbol = newSymbol;
    }
 
    // --- Accessors ---
 
    /**
     * Returns the typist's accuracy rating.
     *
     * @return accuracy as a double between 0.0 and 1.0
     */
    public double getAccuracy()
    {
        return accuracy;
    }
 
    /**
     * Returns the typist's current progress through the passage.
     *
     * @return number of characters correctly typed so far (>= 0)
     */
    public int getProgress()
    {
        return progress;
    }
 
    /**
     * Returns the typist's display name.
     *
     * @return the name as a String
     */
    public String getName()
    {
        return name;
    }
 
    /**
     * Returns the character symbol used to represent this typist.
     *
     * @return the symbol as a char
     */
    public char getSymbol()
    {
        return symbol;
    }
 
    /**
     * Returns the number of burnout turns remaining.
     *
     * @return turns remaining (0 if not burnt out)
     */
    public int getBurnoutTurnsRemaining()
    {
        return burnoutTurnsRemaining;
    }
 
    /**
     * Returns true if this typist is currently burnt out.
     *
     * @return true if burnt out, false otherwise
     */
    public boolean isBurntOut()
    {
        return burntOut;
    }
}