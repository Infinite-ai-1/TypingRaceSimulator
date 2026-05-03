import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

/**
 * GUI for the typing race simulator
 * used Swing for this, seemed the easiest option
 *
 * @author Nikhil David Joseph
 * @version 1.0
 */
public class TypingRaceGUI extends JFrame
{
    // passages to pick from
    static final String[] passageOptions = { "Short", "Medium", "Long", "Custom" };
    static final String[] passageText = {
        "The quick brown fox jumps over the lazy dog",
        "Pack my box with five dozen liquor jugs and watch the wizards fix the problem",
        "How vexingly quick daft zebras jump while peculiar foxes watched from their hiding spots with curiosity"
    };

    // typing styles - each one shifts accuracy up or down a bit
    static final String[] styleNames = { "Touch Typist", "Hunt and Peck", "Phone Thumbs", "Voice to Text" };
    static final double[] styleBonus = { 0.10, -0.10, -0.05, -0.15 };

    // keyboard types
    static final String[] kbNames = { "Mechanical", "Membrane", "Touchscreen", "Stenography" };
    static final double[] kbBonus = { 0.05, 0.00, -0.10, 0.15 };

    static final int[] pts = { 3, 2, 1 };

    static final double MISTYPE = 0.3;
    static final int SLIDEBACK = 2;
    static final int BURNOUT = 3;
    static final double WINBOOST = 0.02;
    static final int DELAY = 130;

    Map<String, Integer> scores = new LinkedHashMap<>();

    CardLayout cl = new CardLayout();
    JPanel mainPanel = new JPanel(cl);

    // config stuff
    JComboBox<String> passagePicker;
    JTextArea ownText;
    JSpinner howManyTypists;
    JCheckBox autocorrect;
    JCheckBox nightmode;
    JPanel typistArea;
    ArrayList<OneTypist> typistSetup = new ArrayList<>();

    // race stuff
    JLabel topLabel;
    JPanel trackArea;
    ArrayList<JProgressBar> progBars = new ArrayList<>();
    ArrayList<JLabel> sideLabels = new ArrayList<>();

    // results stuff
    JTextArea resultsText;
    DefaultTableModel tableData;

    // tracking the race
    ArrayList<Racer> allRacers = new ArrayList<>();
    String thePassage;
    boolean useAutocorrect;
    boolean useNightmode;
    java.util.Timer raceTimer;
    int currentTurn;
    boolean raceOn;

    public static void startRaceGUI()
    {
        SwingUtilities.invokeLater(() -> {
            TypingRaceGUI g = new TypingRaceGUI();
            g.setVisible(true);
        });
    }

    public static void main(String[] args)
    {
        startRaceGUI();
    }

    public TypingRaceGUI()
    {
        setTitle("Typing Race");
        setSize(870, 630);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel.add(setupScreen(), "setup");
        mainPanel.add(raceScreen(), "race");
        mainPanel.add(resultsScreen(), "results");
        add(mainPanel);
        cl.show(mainPanel, "setup");
    }

    JPanel setupScreen()
    {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBackground(new Color(25,25,35));
        p.setBorder(new EmptyBorder(15,15,15,15));

        JLabel heading = new JLabel("Typing Race Simulator", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(new Color(120,200,255));
        p.add(heading, BorderLayout.NORTH);

        // left panel for race options
        JPanel leftSide = makeBox("Race Settings");

        leftSide.add(makeLabel("Passage:"));
        passagePicker = new JComboBox<>(passageOptions);
        leftSide.add(passagePicker);

        ownText = new JTextArea(3, 16);
        ownText.setLineWrap(true);
        ownText.setEnabled(false);
        ownText.setBackground(new Color(45,45,60));
        ownText.setForeground(Color.WHITE);
        leftSide.add(new JScrollPane(ownText));

        passagePicker.addActionListener(e -> ownText.setEnabled(passagePicker.getSelectedIndex() == 3));

        leftSide.add(makeLabel("Number of typists:"));
        howManyTypists = new JSpinner(new SpinnerNumberModel(3, 2, 6, 1));
        leftSide.add(howManyTypists);
        howManyTypists.addChangeListener(e -> makeTypistRows((int) howManyTypists.getValue()));

        leftSide.add(makeLabel("Modifiers:"));
        autocorrect = makeCheckbox("Autocorrect on");
        nightmode   = makeCheckbox("Night shift (everyone -0.05 accuracy)");
        leftSide.add(autocorrect);
        leftSide.add(nightmode);

        // right panel for typist config
        JPanel rightSide = makeBox("Typist Setup");
        typistArea = new JPanel();
        typistArea.setLayout(new BoxLayout(typistArea, BoxLayout.Y_AXIS));
        typistArea.setOpaque(false);
        rightSide.add(new JScrollPane(typistArea));
        makeTypistRows(3);

        JPanel mid = new JPanel(new GridLayout(1,2,10,0));
        mid.setOpaque(false);
        mid.add(leftSide);
        mid.add(rightSide);
        p.add(mid, BorderLayout.CENTER);

        JButton startBtn = new JButton("Start Race");
        startBtn.setBackground(new Color(55,160,85));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        startBtn.setBorder(new EmptyBorder(10,25,10,25));
        startBtn.addActionListener(e -> goRace());

        JPanel bot = new JPanel();
        bot.setOpaque(false);
        bot.add(startBtn);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    void makeTypistRows(int n)
    {
        typistArea.removeAll();
        typistSetup.clear();

        String[] defaultSyms  = {"①","②","③","④","⑤","⑥"};
        String[] defaultNames = {"FASTBOI","FASTGIRL","SLOWBOI","SPEEDSTER","SLOWPOKE","BATMAN"};
        double[] defaultAcc   = {0.85, 0.60, 0.30, 0.75, 0.45, 0.55};

        for (int i = 0; i < n; i++)
        {
            OneTypist row = new OneTypist(defaultSyms[i], defaultNames[i], defaultAcc[i]);
            typistSetup.add(row);
            typistArea.add(row);
            typistArea.add(Box.createVerticalStrut(5));
        }
        typistArea.revalidate();
        typistArea.repaint();
    }

    JPanel raceScreen()
    {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBackground(new Color(20,20,30));
        p.setBorder(new EmptyBorder(15,15,15,15));

        topLabel = new JLabel("Race starting...", SwingConstants.CENTER);
        topLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        topLabel.setForeground(new Color(120,200,255));
        p.add(topLabel, BorderLayout.NORTH);

        trackArea = new JPanel();
        trackArea.setLayout(new BoxLayout(trackArea, BoxLayout.Y_AXIS));
        trackArea.setOpaque(false);
        p.add(new JScrollPane(trackArea), BorderLayout.CENTER);
        return p;
    }

    JPanel resultsScreen()
    {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBackground(new Color(20,20,30));
        p.setBorder(new EmptyBorder(15,15,15,15));

        JLabel heading = new JLabel("Results", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        heading.setForeground(new Color(120,200,255));
        p.add(heading, BorderLayout.NORTH);

        resultsText = new JTextArea();
        resultsText.setEditable(false);
        resultsText.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultsText.setBackground(new Color(28,28,38));
        resultsText.setForeground(Color.WHITE);

        tableData = new DefaultTableModel(new String[]{"Rank","Name","Points","Badge"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable lb = new JTable(tableData);
        lb.setBackground(new Color(28,28,38));
        lb.setForeground(Color.WHITE);
        lb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lb.getTableHeader().setBackground(new Color(45,45,65));
        lb.getTableHeader().setForeground(Color.WHITE);
        lb.setRowHeight(24);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(resultsText), new JScrollPane(lb));
        sp.setDividerLocation(190);
        p.add(sp, BorderLayout.CENTER);

        JPanel bot = new JPanel();
        bot.setOpaque(false);
        JButton again = new JButton("New Race");
        again.setBackground(new Color(55,160,85));
        again.setForeground(Color.WHITE);
        again.setBorder(new EmptyBorder(8,20,8,20));
        again.addActionListener(e -> cl.show(mainPanel, "setup"));
        bot.add(again);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    void goRace()
    {
        int pick = passagePicker.getSelectedIndex();
        thePassage = pick == 3 ? ownText.getText().trim() : passageText[pick];

        if (thePassage.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a passage first.");
            return;
        }

        useAutocorrect = autocorrect.isSelected();
        useNightmode   = nightmode.isSelected();

        allRacers.clear();
        for (OneTypist row : typistSetup)
            allRacers.add(row.toRacer(useNightmode));

        trackArea.removeAll();
        progBars.clear();
        sideLabels.clear();

        Color[] barColours = {
            new Color(100,200,255), new Color(255,175,75),
            new Color(115,225,115), new Color(255,95,145),
            new Color(195,145,255), new Color(255,215,75)
        };

        for (int i = 0; i < allRacers.size(); i++)
        {
            Racer r = allRacers.get(i);
            r.typist.resetToStart();
            r.burnouts = 0; r.mistypes = 0;
            r.done = false; r.finishedAt = Integer.MAX_VALUE;

            JPanel row = new JPanel(new BorderLayout(6,2));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(4,4,4,4));

            JLabel nm = new JLabel(r.sym + "  " + r.typist.getName());
            nm.setForeground(Color.WHITE);
            nm.setFont(new Font("SansSerif", Font.BOLD, 13));
            nm.setPreferredSize(new Dimension(175,20));

            JProgressBar bar = new JProgressBar(0, thePassage.length());
            bar.setForeground(barColours[i % barColours.length]);
            bar.setBackground(new Color(48,48,58));
            bar.setStringPainted(true);
            bar.setFont(new Font("SansSerif", Font.PLAIN, 11));

            JLabel info = new JLabel(" ");
            info.setForeground(new Color(255,155,75));
            info.setFont(new Font("SansSerif", Font.PLAIN, 12));
            info.setPreferredSize(new Dimension(155,20));

            row.add(nm, BorderLayout.WEST);
            row.add(bar, BorderLayout.CENTER);
            row.add(info, BorderLayout.EAST);

            trackArea.add(row);
            progBars.add(bar);
            sideLabels.add(info);
        }

        trackArea.revalidate();
        trackArea.repaint();
        cl.show(mainPanel, "race");
        topLabel.setText("Race in progress...");
        topLabel.setForeground(new Color(120,200,255));

        currentTurn = 0;
        raceOn = true;
        raceTimer = new java.util.Timer();
        raceTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> nextTurn());
            }
        }, 300, DELAY);
    }

    void nextTurn()
    {
        if (!raceOn) return;
        currentTurn++;

        Racer winner = null;

        for (int i = 0; i < allRacers.size(); i++)
        {
            Racer r = allRacers.get(i);
            if (r.done) continue;
            Typist t = r.typist;

            if (t.isBurntOut())
            {
                t.recoverFromBurnout();
                sideLabels.get(i).setText("burnt out (" + t.getBurnoutTurnsRemaining() + ")");
            }
            else
            {
                sideLabels.get(i).setText("");

                if (Math.random() < t.getAccuracy()) t.typeCharacter();

                int slide = useAutocorrect ? 1 : SLIDEBACK;
                if (Math.random() < (1.0 - t.getAccuracy()) * MISTYPE)
                {
                    t.slideBack(slide);
                    r.mistypes++;
                    sideLabels.get(i).setText("[<] mistyped");
                }

                if (Math.random() < 0.05 * t.getAccuracy() * t.getAccuracy())
                {
                    t.burnOut(BURNOUT);
                    r.burnouts++;
                    sideLabels.get(i).setText("BURNT OUT!");
                }
            }

            int p = Math.min(t.getProgress(), thePassage.length());
            progBars.get(i).setValue(p);
            progBars.get(i).setString(p + "/" + thePassage.length());

            if (t.getProgress() >= thePassage.length() && !r.done)
            {
                r.done = true;
                r.finishedAt = currentTurn;
                if (winner == null) winner = r;
            }
        }

        if (winner != null)
        {
            raceOn = false;
            raceTimer.cancel();
            doResults(winner);
        }
    }

    void doResults(Racer winner)
    {
        double prevAcc = winner.typist.getAccuracy();
        winner.typist.setAccuracy(Math.min(1.0, prevAcc + WINBOOST));
        topLabel.setText("Winner: " + winner.typist.getName() + "!");
        topLabel.setForeground(new Color(255,210,0));

        // sort everyone by when they finished
        ArrayList<Racer> sorted = new ArrayList<>(allRacers);
        sorted.sort((a,b) -> {
            if (a.done && b.done) return a.finishedAt - b.finishedAt;
            if (a.done) return -1;
            if (b.done) return 1;
            return b.typist.getProgress() - a.typist.getProgress();
        });

        StringBuilder sb = new StringBuilder("=== Race Results ===\n\n");

        for (int i = 0; i < sorted.size(); i++)
        {
            Racer r = sorted.get(i);
            int p = i < pts.length ? pts[i] : 0;
            double secs = (r.done ? r.finishedAt : currentTurn) * DELAY / 1000.0;
            double wpm  = (thePassage.length() / 5.0) / (secs / 60.0);
            int total = r.typist.getProgress() + r.mistypes;
            double accPct = total > 0 ? 100.0 * r.typist.getProgress() / total : 100.0;

            scores.merge(r.typist.getName(), p, Integer::sum);

            sb.append(String.format("#%d  %s  %s\n", i+1, r.sym, r.typist.getName()));
            sb.append(String.format("    WPM: %.1f  |  Accuracy: %.1f%%  |  Burnouts: %d  |  Points: +%d\n\n",
                wpm, accPct, r.burnouts, p));
        }

        sb.append("Accuracy boost to winner: " + String.format("%.2f -> %.2f", prevAcc, winner.typist.getAccuracy()));
        resultsText.setText(sb.toString());

        tableData.setRowCount(0);
        ArrayList<Map.Entry<String,Integer>> list = new ArrayList<>(scores.entrySet());
        list.sort((a,b) -> b.getValue() - a.getValue());
        int rank = 1;
        for (Map.Entry<String,Integer> e : list)
        {
            int v = e.getValue();
            String badge = v >= 9 ? "Speed Demon" : v >= 6 ? "Iron Fingers" : v >= 3 ? "Contender" : "Newcomer";
            tableData.addRow(new Object[]{rank++, e.getKey(), v, badge});
        }

        new java.util.Timer().schedule(new TimerTask() {
            public void run() { SwingUtilities.invokeLater(() -> cl.show(mainPanel, "results")); }
        }, 1400);
    }

    // inner class for each typist row in setup
    class OneTypist extends JPanel
    {
        JTextField nameBox, symBox;
        JSpinner accBox;
        JComboBox<String> styleBox, keyboardBox;

        OneTypist(String sym, String name, double acc)
        {
            setLayout(new GridLayout(0, 2, 3, 2));
            setOpaque(false);
            setBorder(BorderFactory.createMatteBorder(0,0,1,0, new Color(55,55,75)));

            add(makeLabel("Name:"));       nameBox = textField(name);  add(nameBox);
            add(makeLabel("Symbol:"));     symBox  = textField(sym);   add(symBox);
            add(makeLabel("Accuracy:"));
            accBox = new JSpinner(new SpinnerNumberModel(acc, 0.0, 1.0, 0.05));
            add(accBox);
            add(makeLabel("Style:"));      styleBox    = new JComboBox<>(styleNames);    add(styleBox);
            add(makeLabel("Keyboard:"));   keyboardBox = new JComboBox<>(kbNames);       add(keyboardBox);
        }

        Racer toRacer(boolean nightOn)
        {
            String n   = nameBox.getText().trim().isEmpty() ? "TYPIST" : nameBox.getText().trim();
            String s   = symBox.getText().trim().isEmpty()  ? "?"      : symBox.getText().trim();
            double acc = ((Number) accBox.getValue()).doubleValue();

            acc += styleBonus[styleBox.getSelectedIndex()];
            acc += kbBonus[keyboardBox.getSelectedIndex()];
            if (nightOn) acc -= 0.05;
            if (acc > 1.0) acc = 1.0;
            if (acc < 0.0) acc = 0.0;

            return new Racer(new Typist(s.charAt(0), n, acc), s);
        }

        JTextField textField(String val)
        {
            JTextField f = new JTextField(val);
            f.setBackground(new Color(48,48,62));
            f.setForeground(Color.WHITE);
            f.setCaretColor(Color.WHITE);
            f.setFont(new Font("SansSerif", Font.PLAIN, 12));
            f.setBorder(BorderFactory.createLineBorder(new Color(75,75,95)));
            return f;
        }
    }

    // holds race info for one typist
    static class Racer
    {
        Typist typist;
        String sym;
        boolean done = false;
        int finishedAt = Integer.MAX_VALUE;
        int burnouts   = 0;
        int mistypes   = 0;

        Racer(Typist t, String s) { typist = t; sym = s; }
    }

    JPanel makeBox(String title)
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(33,33,48));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(75,75,115)), title,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 13), new Color(155,185,255)));
        return p;
    }

    JLabel makeLabel(String txt)
    {
        JLabel l = new JLabel(txt);
        l.setForeground(Color.LIGHT_GRAY);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    JCheckBox makeCheckbox(String txt)
    {
        JCheckBox cb = new JCheckBox(txt);
        cb.setOpaque(false);
        cb.setForeground(Color.LIGHT_GRAY);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return cb;
    }
}