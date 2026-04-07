import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.*;


// ============================================================
// GEOMETRIC WORLD
// ============================================================

enum ShapeType { CIRCLE, TRIANGLE, SQUARE, PENTAGON, HEXAGON }
enum ShapeColor { RED, GREEN, BLUE, YELLOW, CYAN, MAGENTA, WHITE }

class GeometricShape {
    ShapeType type;
    ShapeColor color;
    double x, y;       // 0..1 normalised
    double size;       // 0..1
    double speed;
    double angle;
    boolean isTarget;  // marked as attended-to

    GeometricShape(ShapeType t, ShapeColor c, double x, double y, double size) {
        this.type = t; this.color = c;
        this.x = x; this.y = y; this.size = size;
        this.speed = 0.002 + Math.random() * 0.004;
        this.angle = Math.random() * Math.PI * 2;
    }

    // encode this shape as a sensor vector [5 dims]
    double[] encode() {
        return new double[]{
            type.ordinal()  / (double)(ShapeType.values().length  - 1) * 2 - 1,
            color.ordinal() / (double)(ShapeColor.values().length - 1) * 2 - 1,
            x * 2 - 1,
            y * 2 - 1,
            size * 2 - 1
        };
    }

    void step() {
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;
        if (x < 0 || x > 1) { angle = Math.PI - angle; x = Math.max(0, Math.min(1, x)); }
        if (y < 0 || y > 1) { angle = -angle;           y = Math.max(0, Math.min(1, y)); }
    }

    Color toAwtColor(float alpha) {
        Color base = switch (color) {
            case RED     -> new Color(220, 60,  60);
            case GREEN   -> new Color(60,  200, 100);
            case BLUE    -> new Color(60,  120, 220);
            case YELLOW  -> new Color(230, 200, 50);
            case CYAN    -> new Color(50,  210, 210);
            case MAGENTA -> new Color(200, 60,  200);
            case WHITE   -> new Color(220, 220, 220);
        };
        int a = Math.max(0, Math.min(255, (int)(alpha * 255)));
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), a);
    }
}

class GeometricWorld {
    List<GeometricShape> shapes = new ArrayList<>();
    private final Random rng = new Random();

    GeometricWorld(int n) {
        ShapeType[]  types  = ShapeType.values();
        ShapeColor[] colors = ShapeColor.values();
        for (int i = 0; i < n; i++) {
            shapes.add(new GeometricShape(
                types [rng.nextInt(types.length)],
                colors[rng.nextInt(colors.length)],
                rng.nextDouble(), rng.nextDouble(),
                0.04 + rng.nextDouble() * 0.10
            ));
        }
    }

    void step() { shapes.forEach(GeometricShape::step); }

    // flatten all shapes into one sensor vector
    double[] getSensorData() {
        double[] out = new double[shapes.size() * 5];
        for (int i = 0; i < shapes.size(); i++) {
            double[] enc = shapes.get(i).encode();
            System.arraycopy(enc, 0, out, i * 5, 5);
        }
        return out;
    }
}

// ============================================================
// CONSCIOUSNESS ENGINE  (adapted from SyntheticConsciousness.java)
// ============================================================

class NeuralState2 {
    double[] pattern;
    int hash;
    NeuralState2(double[] p) {
        this.pattern = p.clone();
        int[] d = new int[p.length];
        for (int i = 0; i < p.length; i++) d[i] = (int)(p[i] * 10);
        this.hash = Arrays.hashCode(d);
    }
    @Override public boolean equals(Object o) {
        return o instanceof NeuralState2 && hash == ((NeuralState2)o).hash;
    }
    @Override public int hashCode() { return hash; }
}

class ConsciousnessEngine {
    private Map<NeuralState2, Double> stateValues = new HashMap<>();
    private double mood = 0;
    private double[] lastFocusWeights;
    private int lastAttendedIndex = -1;
    private List<String> log = new ArrayList<>();

    // returns index of shape that received peak attention
    int update(double[] sensorData, int numShapes) {
        NeuralState2 state = new NeuralState2(sensorData);

        // ATTENTION: compute salience per shape
        double[] salience = new double[numShapes];
        for (int i = 0; i < numShapes; i++) {
            double s = 0;
            for (int j = 0; j < 5; j++) {
                double v = sensorData[i * 5 + j];
                s += Math.abs(v) * (1 + mood * 0.5);
            }
            salience[i] = s / 5.0;
        }

        // find most salient shape
        int best = 0;
        for (int i = 1; i < numShapes; i++) if (salience[i] > salience[best]) best = i;
        lastFocusWeights = salience;
        lastAttendedIndex = best;

        // IMAGINATION: simulate 3 nearby states
        double bestImaginedValue = -Double.MAX_VALUE;
        Random rng = new Random();
        for (int s = 0; s < 3; s++) {
            double[] perturbed = sensorData.clone();
            for (int j = 0; j < perturbed.length; j++)
                perturbed[j] = Math.max(-1, Math.min(1, perturbed[j] + rng.nextGaussian() * 0.1));
            NeuralState2 img = new NeuralState2(perturbed);
            double v = stateValues.getOrDefault(img, 0.0);
            if (v > bestImaginedValue) bestImaginedValue = v;
        }

        // EMOTION: reward attending to high-salience shape
        double reward = salience[best];
        double delta = reward - 0;
        mood = Math.max(-1, Math.min(1, mood * 0.8 + delta * 0.2));

        // LEARN
        double cur = stateValues.getOrDefault(state, 0.0);
        stateValues.put(state, cur + 0.1 * reward);

        log.add(String.format("Attended shape #%d  salience=%.3f  mood=%.3f", best, salience[best], mood));
        if (log.size() > 200) log.remove(0);

        return best;
    }

    double[] getFocusWeights() { return lastFocusWeights; }
    int      getAttendedIndex() { return lastAttendedIndex; }
    double   getMood()          { return mood; }
    List<String> getLog()       { return log; }
}

// ============================================================
// SWING GUI
// ============================================================

class WorldPanel extends JPanel {
    public GeometricWorld world;
    public ConsciousnessEngine engine;
    private int attended = -1;
    private float pulse = 0;

    // colour palette
    static final Color BG       = new Color(10, 12, 20);
    static final Color GRID     = new Color(25, 30, 50);
    static final Color ATTENDED = new Color(255, 230, 80);
    static final Color SCANLINE = new Color(255, 255, 255, 6);

    WorldPanel(GeometricWorld w, ConsciousnessEngine e) {
        this.world = w; this.engine = e;
        setBackground(BG);
        setPreferredSize(new Dimension(600, 600));
    }

    void setAttended(int idx) { this.attended = idx; }
    void setPulse(float p)    { this.pulse = p; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);

        int W = getWidth(), H = getHeight();

        // subtle grid
        g2.setColor(GRID);
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < W; x += 40) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 40) g2.drawLine(0, y, W, y);

        // scanlines
        for (int y = 0; y < H; y += 4) {
            g2.setColor(SCANLINE);
            g2.fillRect(0, y, W, 2);
        }

        double[] weights = engine.getFocusWeights();
        List<GeometricShape> shapes = world.shapes;

        // draw attended halo FIRST (behind the shape)
        if (attended >= 0 && attended < shapes.size()) {
            GeometricShape sh = shapes.get(attended);
            int cx = (int)(sh.x * W);
            int cy = (int)(sh.y * H);
            int r  = (int)(sh.size * Math.min(W, H) * 0.5);
            // pulse is -1..+1; map radius offset to 4..16 (always positive)
            int haloR = r + 10 + (int)((pulse + 1.0f) * 0.5f * 12);
            // alpha oscillates between 60 and 160
            int haloAlpha = 60 + (int)((pulse + 1.0f) * 0.5f * 100);
            // outer glow fill
            g2.setColor(new Color(255, 200, 40, haloAlpha / 4));
            g2.fillOval(cx - haloR, cy - haloR, haloR * 2, haloR * 2);
            // bright ring
            g2.setColor(new Color(255, 200, 40, haloAlpha));
            g2.setStroke(new BasicStroke(3.0f));
            g2.drawOval(cx - haloR, cy - haloR, haloR * 2, haloR * 2);
            // second subtler outer ring
            int haloR2 = haloR + 10;
            g2.setColor(new Color(255, 200, 40, haloAlpha / 3));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(cx - haloR2, cy - haloR2, haloR2 * 2, haloR2 * 2);
        }

        // draw shapes — non-attended shapes dimmed
        for (int i = 0; i < shapes.size(); i++) {
            GeometricShape sh = shapes.get(i);
            boolean isAttended = (i == attended);
            float alpha = isAttended ? 1.0f : 0.25f;
            if (weights != null && weights.length > i)
                alpha = Math.max(0.12f, (float) weights[i] * 1.4f);
            if (isAttended) alpha = 1.0f;  // always full brightness for attended

            int cx = (int)(sh.x * W);
            int cy = (int)(sh.y * H);
            int r  = (int)(sh.size * Math.min(W, H) * 0.5);

            drawShape(g2, sh, cx, cy, r, alpha, isAttended);
        }

        // ATTENDED label drawn on top of everything
        if (attended >= 0 && attended < shapes.size()) {
            GeometricShape sh = shapes.get(attended);
            int cx = (int)(sh.x * W);
            int cy = (int)(sh.y * H);
            int r  = (int)(sh.size * Math.min(W, H) * 0.5);
            int haloR = r + 12 + (int)(pulse * 6);
            g2.setFont(new Font("Monospaced", Font.BOLD, 11));
            g2.setColor(ATTENDED);
            g2.drawString("ATTENDED", cx - 28, cy - haloR - 6);
        }

        // corner label
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(new Color(80, 100, 140));
        g2.drawString("GEOMETRIC WORLD  |  " + shapes.size() + " OBJECTS", 10, H - 10);
    }

    private void drawShape(Graphics2D g2, GeometricShape sh, int cx, int cy, int r, float alpha, boolean attended) {
        Color fill    = sh.toAwtColor(alpha * 0.55f);
        Color outline = sh.toAwtColor(alpha);

        Shape shape = switch (sh.type) {
            case CIRCLE   -> new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
            case SQUARE   -> new Rectangle2D.Double(cx - r, cy - r, r * 2, r * 2);
            case TRIANGLE -> polygon(cx, cy, r, 3, -Math.PI / 2);
            case PENTAGON -> polygon(cx, cy, r, 5, -Math.PI / 2);
            case HEXAGON  -> polygon(cx, cy, r, 6, 0);
        };

        g2.setColor(fill);
        g2.fill(shape);
        g2.setColor(outline);
        g2.setStroke(new BasicStroke(attended ? 2.0f : 1.0f));
        g2.draw(shape);
    }

    private Shape polygon(int cx, int cy, int r, int sides, double startAngle) {
        int[] xs = new int[sides], ys = new int[sides];
        for (int i = 0; i < sides; i++) {
            double a = startAngle + 2 * Math.PI * i / sides;
            xs[i] = cx + (int)(r * Math.cos(a));
            ys[i] = cy + (int)(r * Math.sin(a));
        }
        return new Polygon(xs, ys, sides);
    }
}

class MoodBar extends JComponent {
    private double mood = 0;
    MoodBar() { setPreferredSize(new Dimension(200, 18)); }
    void setMood(double m) { this.mood = m; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int W = getWidth(), H = getHeight();
        // track
        g2.setColor(new Color(30, 35, 55));
        g2.fillRoundRect(0, 0, W, H, H, H);
        // fill: negative=red, positive=green
        int mid = W / 2;
        if (mood >= 0) {
            int w = (int)(mood * mid);
            g2.setColor(new Color(60, 200, 100));
            g2.fillRoundRect(mid, 2, w, H - 4, H - 4, H - 4);
        } else {
            int w = (int)(-mood * mid);
            g2.setColor(new Color(220, 60, 60));
            g2.fillRoundRect(mid - w, 2, w, H - 4, H - 4, H - 4);
        }
        // centre line
        g2.setColor(new Color(100, 120, 160));
        g2.drawLine(mid, 0, mid, H);
    }
}

class SaliencePanel extends JPanel {
    private double[] weights;
    private int attended = -1;
    private List<GeometricShape> shapes;

    SaliencePanel() {
        setBackground(new Color(10, 12, 20));
        setPreferredSize(new Dimension(200, 300));
    }

    void update(double[] w, int att, List<GeometricShape> sh) {
        this.weights = w; this.attended = att; this.shapes = sh; repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (weights == null || shapes == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int W = getWidth();
        int barH = 14, gap = 4, topPad = 8;
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));

        // find max for normalisation
        double max = 1e-9;
        for (double w : weights) if (w > max) max = w;

        for (int i = 0; i < Math.min(weights.length, shapes.size()); i++) {
            int y = topPad + i * (barH + gap);
            float norm = (float)(weights[i] / max);
            boolean isAtt = (i == attended);

            // bar background
            g2.setColor(new Color(20, 25, 40));
            g2.fillRoundRect(2, y, W - 4, barH, 4, 4);

            // bar fill
            Color barColor = isAtt ? new Color(255, 230, 80) : shapes.get(i).toAwtColor(0.8f);
            g2.setColor(barColor);
            int bw = Math.max(4, (int)((W - 8) * norm));
            g2.fillRoundRect(2, y, bw, barH, 4, 4);

            // label
            g2.setColor(isAtt ? Color.BLACK : new Color(180, 190, 210));
            String lbl = String.format("#%02d %-8s %.2f", i,
                shapes.get(i).type.name().substring(0, Math.min(4, shapes.get(i).type.name().length())),
                weights[i]);
            g2.drawString(lbl, 6, y + barH - 3);
        }
    }
}

class LogPanel extends JPanel {
    private JTextArea area;
    LogPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(8, 10, 18));
        area = new JTextArea();
        area.setBackground(new Color(8, 10, 18));
        area.setForeground(new Color(80, 180, 120));
        area.setFont(new Font("Monospaced", Font.PLAIN, 11));
        area.setEditable(false);
        area.setLineWrap(true);
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(new Color(8, 10, 18));
        add(sp, BorderLayout.CENTER);
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(40, 55, 80)),
            "CONSCIOUSNESS LOG",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 10),
            new Color(80, 120, 180)
        ));
    }

    void append(List<String> lines) {
        int show = Math.min(lines.size(), 60);
        List<String> recent = lines.subList(lines.size() - show, lines.size());
        area.setText(String.join("\n", recent));
        area.setCaretPosition(area.getDocument().getLength());
    }
}

public class ConsciousnessExplorer extends JFrame {

    private GeometricWorld world;
    private ConsciousnessEngine engine;
    private WorldPanel worldPanel;
    private SaliencePanel saliencePanel;
    private LogPanel logPanel;
    private MoodBar moodBar;
    private JLabel moodLabel, cycleLabel, attendLabel;
    private javax.swing.Timer timer;
    private int cycle = 0;
    private float pulse = 0;
    private boolean running = false;

    public ConsciousnessExplorer() {
        super("Synthetic Consciousness Explorer  —  Aleksander's Five Axioms");
        world  = new GeometricWorld(12);
        engine = new ConsciousnessEngine();

        buildUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void buildUI() {
        // overall dark background
        Color BG    = new Color(10, 12, 20);
        Color PANEL = new Color(14, 17, 28);
        Color BORD  = new Color(35, 45, 75);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- TITLE BAR ----
        JPanel title = new JPanel(new BorderLayout());
        title.setBackground(PANEL);
        title.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORD),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        JLabel heading = new JLabel("SYNTHETIC CONSCIOUSNESS EXPLORER");
        heading.setFont(new Font("Monospaced", Font.BOLD, 14));
        heading.setForeground(new Color(180, 200, 255));
        JLabel sub = new JLabel("Igor Aleksander · Five Axioms · Magnus Architecture");
        sub.setFont(new Font("Monospaced", Font.PLAIN, 10));
        sub.setForeground(new Color(80, 100, 150));
        title.add(heading, BorderLayout.WEST);
        title.add(sub, BorderLayout.EAST);

        // ---- WORLD PANEL ----
        worldPanel = new WorldPanel(world, engine);
        worldPanel.setBorder(BorderFactory.createLineBorder(BORD));

        // ---- RIGHT SIDEBAR ----
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG);
        sidebar.setPreferredSize(new Dimension(210, 600));

        // status block
        JPanel statusBlock = new JPanel(new GridLayout(0, 1, 2, 4));
        statusBlock.setBackground(PANEL);
        statusBlock.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORD),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        cycleLabel  = styledLabel("CYCLE:  0", new Color(140, 160, 220));
        attendLabel = styledLabel("ATTENDED:  —", new Color(255, 230, 80));
        moodLabel   = styledLabel("MOOD:  0.000", new Color(100, 200, 140));

        statusBlock.add(sectionHeader("STATE"));
        statusBlock.add(cycleLabel);
        statusBlock.add(attendLabel);
        statusBlock.add(sectionHeader("EMOTIONAL VALENCE"));
        statusBlock.add(moodLabel);
        moodBar = new MoodBar();
        statusBlock.add(moodBar);

        // axiom legend
        JPanel axiomBlock = new JPanel(new GridLayout(0, 1, 1, 3));
        axiomBlock.setBackground(PANEL);
        axiomBlock.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORD),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        axiomBlock.add(sectionHeader("ACTIVE AXIOMS"));
        String[][] axioms = {
            {"1", "PRESENCE",    "60,180,255"},
            {"2", "ATTENTION",   "255,220,60"},
            {"3", "IMAGINATION", "180,100,255"},
            {"4", "PLANNING",    "60,220,140"},
            {"5", "EMOTION",     "255,100,100"},
        };
        for (String[] ax : axioms) {
            JLabel l = new JLabel("  [" + ax[0] + "] " + ax[1]);
            String[] rgb = ax[2].split(",");
            l.setForeground(new Color(Integer.parseInt(rgb[0].trim()),
                Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim())));
            l.setFont(new Font("Monospaced", Font.PLAIN, 10));
            axiomBlock.add(l);
        }

        // salience panel
        JPanel salWrapper = new JPanel(new BorderLayout());
        salWrapper.setBackground(PANEL);
        salWrapper.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORD),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        JLabel salTitle = sectionHeader("ATTENTION SALIENCE");
        salWrapper.add(salTitle, BorderLayout.NORTH);
        saliencePanel = new SaliencePanel();
        saliencePanel.setBackground(PANEL);
        salWrapper.add(saliencePanel, BorderLayout.CENTER);

        // controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        controls.setBackground(PANEL);
        controls.setBorder(BorderFactory.createLineBorder(BORD));

        JButton startBtn = darkButton("START", new Color(60, 200, 100));
        JButton stepBtn  = darkButton("STEP",  new Color(60, 120, 220));
        JButton resetBtn = darkButton("RESET", new Color(200, 80,  60));

        startBtn.addActionListener(e -> toggleRun(startBtn));
        stepBtn .addActionListener(e -> step());
        resetBtn.addActionListener(e -> reset());

        controls.add(startBtn);
        controls.add(stepBtn);
        controls.add(resetBtn);

        sidebar.add(statusBlock);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(axiomBlock);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(salWrapper);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(controls);

        // ---- LOG PANEL ----
        logPanel = new LogPanel();
        logPanel.setPreferredSize(new Dimension(810, 120));

        root.add(title,      BorderLayout.NORTH);
        root.add(worldPanel, BorderLayout.CENTER);
        root.add(sidebar,    BorderLayout.EAST);
        root.add(logPanel,   BorderLayout.SOUTH);

        setContentPane(root);

        // animation timer — javax.swing.Timer fires on the EDT, no threading conflicts
        timer = new javax.swing.Timer(80, e -> {
            // always move shapes every tick so they drift continuously
            world.step();
            // advance pulse angle through full 2π cycle
            pulse = (pulse + 0.15f) % (2.0f * (float)Math.PI);
            worldPanel.setPulse((float)Math.sin(pulse));
            // run consciousness step if active
            if (running) step();
            worldPanel.repaint();
        });
        timer.start();
    }

    private void step() {
        double[] sensor = world.getSensorData();
        int att = engine.update(sensor, world.shapes.size());
        cycle++;

        for (int i = 0; i < world.shapes.size(); i++)
            world.shapes.get(i).isTarget = (i == att);

        worldPanel.setAttended(att);
        saliencePanel.update(engine.getFocusWeights(), att, world.shapes);

        String attShape = att >= 0 ? world.shapes.get(att).type.name() + " (" + world.shapes.get(att).color.name() + ")" : "—";
        cycleLabel .setText("CYCLE:  " + cycle);
        attendLabel.setText("ATTENDED:  " + attShape);
        moodLabel  .setText(String.format("MOOD:  %.3f", engine.getMood()));
        moodBar.setMood(engine.getMood());
        moodBar.repaint();
        logPanel.append(engine.getLog());
        saliencePanel.repaint();
    }

    private void reset() {
        running = false;
        world  = new GeometricWorld(12);
        engine = new ConsciousnessEngine();
        cycle  = 0;
        worldPanel.world  = world;
        worldPanel.engine = engine;
        worldPanel.setAttended(-1);
        cycleLabel .setText("CYCLE:  0");
        attendLabel.setText("ATTENDED:  —");
        moodLabel  .setText("MOOD:  0.000");
        moodBar.setMood(0);
        moodBar.repaint();
    }

    private void toggleRun(JButton btn) {
        running = !running;
        btn.setText(running ? "PAUSE" : "START");
        btn.setForeground(running ? new Color(255, 200, 60) : new Color(60, 200, 100));
    }

    // ---- helpers ----
    private JLabel styledLabel(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.BOLD, 11));
        l.setForeground(c);
        return l;
    }

    private JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.BOLD, 10));
        l.setForeground(new Color(60, 80, 130));
        return l;
    }

    private JButton darkButton(String text, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Monospaced", Font.BOLD, 11));
        b.setForeground(fg);
        b.setBackground(new Color(18, 22, 38));
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(fg.darker(), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(ConsciousnessExplorer::new);
    }
}
