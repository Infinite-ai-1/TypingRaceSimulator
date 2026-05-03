/**
 * testing class for Typist
 * just runs through the main things to make sure they work
 *
 * @author Nikhil David Joseph
 */
public class TypistTest
{
    public static void main(String[] args)
    {
        System.out.println("running typist tests...");
        System.out.println();

        // test 1 - basic movement
        Typist t1 = new Typist('A', "Tester1", 0.8);
        t1.typeCharacter();
        t1.typeCharacter();
        t1.typeCharacter();
        System.out.println("test 1 - typeCharacter");
        System.out.println("typed 3 times, progress = " + t1.getProgress());
        System.out.println("expected 3, pass = " + (t1.getProgress() == 3));
        System.out.println();

        // test 2 - sliding back
        Typist t2 = new Typist('B', "Tester2", 0.7);
        for (int i = 0; i < 6; i++) t2.typeCharacter();
        t2.slideBack(2);
        System.out.println("test 2 - slideBack");
        System.out.println("typed 6, slid back 2, progress = " + t2.getProgress());
        System.out.println("expected 4, pass = " + (t2.getProgress() == 4));
        System.out.println();

        // test 3 - cant go below 0
        Typist t3 = new Typist('C', "Tester3", 0.5);
        t3.typeCharacter(); // only at 1
        t3.slideBack(50);   // way too much, should clamp
        System.out.println("test 3 - slideBack cant go below 0");
        System.out.println("progress after sliding back 50 from 1 = " + t3.getProgress());
        System.out.println("expected 0, pass = " + (t3.getProgress() == 0));
        System.out.println();

        // test 4 - burnout
        Typist t4 = new Typist('D', "Tester4", 0.9);
        t4.burnOut(3);
        System.out.println("test 4 - burnout countdown");
        System.out.println("burnt out = " + t4.isBurntOut() + " (should be true)");
        System.out.println("turns left = " + t4.getBurnoutTurnsRemaining() + " (should be 3)");
        t4.recoverFromBurnout();
        System.out.println("after 1 recover = " + t4.getBurnoutTurnsRemaining() + " (should be 2)");
        t4.recoverFromBurnout();
        t4.recoverFromBurnout();
        System.out.println("after 3 recovers, burntOut = " + t4.isBurntOut() + " (should be false)");
        System.out.println("pass = " + (!t4.isBurntOut() && t4.getBurnoutTurnsRemaining() == 0));
        System.out.println();

        // test 5 - reset
        Typist t5 = new Typist('E', "Tester5", 0.6);
        t5.typeCharacter();
        t5.typeCharacter();
        t5.burnOut(5);
        t5.resetToStart();
        System.out.println("test 5 - resetToStart");
        System.out.println("progress after reset = " + t5.getProgress() + " (should be 0)");
        System.out.println("burnt out after reset = " + t5.isBurntOut() + " (should be false)");
        System.out.println("turns left = " + t5.getBurnoutTurnsRemaining() + " (should be 0)");
        System.out.println("pass = " + (t5.getProgress() == 0 && !t5.isBurntOut()));
        System.out.println();

        // test 6 - accuracy clamping
        Typist t6 = new Typist('F', "Tester6", 0.5);
        t6.setAccuracy(2.0);
        System.out.println("test 6 - accuracy clamping");
        System.out.println("set to 2.0, got = " + t6.getAccuracy() + " (should be 1.0)");
        t6.setAccuracy(-1.0);
        System.out.println("set to -1.0, got = " + t6.getAccuracy() + " (should be 0.0)");
        t6.setAccuracy(0.65);
        System.out.println("set to 0.65, got = " + t6.getAccuracy() + " (should be 0.65)");
        System.out.println("pass = " + (t6.getAccuracy() == 0.65));
        System.out.println();

        System.out.println("Test Complete!");
    }
}