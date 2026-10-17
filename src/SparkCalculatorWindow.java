import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SparkCalculatorWindow {
    private static final Color BACKGROUND = new Color(13, 19, 34);
    private static final Color SURFACE = new Color(23, 32, 52);
    private static final Color SURFACE_LIGHT = new Color(31, 43, 67);
    private static final Color TEXT = new Color(245, 241, 233);
    private static final Color MUTED = new Color(159, 172, 195);
    private static final Color PINK = new Color(244, 115, 163);
    private static final Color GOLD = new Color(246, 197, 113);
    private static final Color BORDER = new Color(51, 65, 91);
    private static final Color PORTRAIT_PLACEHOLDER = new Color(28, 35, 56);
    private static final Map<String, String> SPECIAL_NAMES = Map.of(
            "ksmiracle", "K.S. Miracle",
            "matikanefukukitaru", "Matikanefukukitaru",
            "matikanetannhauser", "Matikanetannhauser",
            "mr-cb", "Mr. C.B.",
            "tm-opera-o", "TM Opera O");
    private final JFrame frame = new JFrame("Sparkforge — Uma Musume Spark Calculator");
    private final Map<UmaRole, UmaSlot> slots = new EnumMap<>(UmaRole.class);
    private final Map<SparkProbabilityCalculator.Stat, JTextField> statFields =
            new EnumMap<>(SparkProbabilityCalculator.Stat.class);
    private final Set<SparkProbabilityCalculator.Stat> targetStats =
            EnumSet.of(SparkProbabilityCalculator.Stat.SPEED);
    private final Map<SparkProbabilityCalculator.Stat, JButton> statButtons =
            new EnumMap<>(SparkProbabilityCalculator.Stat.class);
    private final JComboBox<Integer> minimumStars = new JComboBox<>(new Integer[]{1, 2, 3});
    private final JComboBox<Integer> maximumStars = new JComboBox<>(new Integer[]{1, 2, 3});
    private final JComboBox<Integer> confidence = new JComboBox<>(new Integer[]{80, 85, 90, 95, 99});
    private final JLabel chanceValue = new JLabel("—");
    private final JLabel runsValue = new JLabel("—");
    private final JLabel resultMessage = new JLabel("Enter a stat target to see your odds.");
    private final JLabel rosterStatus = new JLabel();
    private final List<UmaCharacter> catalog = localCatalog();

    public SparkCalculatorWindow() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(960, 760));
        frame.setSize(1180, 920);
        frame.setLocationRelativeTo(null);
        frame.setBackground(BACKGROUND);
        frame.setContentPane(buildContent());
        setCharacter(findCharacter("special-week"), UmaRole.TRAINEE);
        updateCalculation();
    }

    public void show() {
        frame.setVisible(true);
    }

    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 32, 32));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(buildLineageCard());
        content.add(Box.createVerticalStrut(20));
        content.add(buildCalculatorCards());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        root.add(scrollPane, BorderLayout.CENTER);
        return root;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(19, 27, 45));
        header.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel logo = label("SPARKFORGE", 12, GOLD, Font.BOLD);
        JLabel title = label("Build your next great legacy", 23, TEXT, Font.BOLD);
        JLabel subtitle = label("A probability companion for your Uma Musume careers", 13, MUTED, Font.PLAIN);
        brand.add(logo);
        brand.add(Box.createVerticalStrut(5));
        brand.add(title);
        brand.add(Box.createVerticalStrut(3));
        brand.add(subtitle);
        JLabel chip = label("FACTOR LAB  /  01", 11, GOLD, Font.BOLD);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(112, 87, 54)),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        header.add(brand, BorderLayout.CENTER);
        header.add(chip, BorderLayout.EAST);
        return header;
    }

    private JComponent buildLineageCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 18));
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));
        JPanel intro = new JPanel(new BorderLayout());
        intro.setOpaque(false);
        JPanel headings = new JPanel();
        headings.setOpaque(false);
        headings.setLayout(new BoxLayout(headings, BoxLayout.Y_AXIS));
        headings.add(label("YOUR LINEAGE", 11, PINK, Font.BOLD));
        headings.add(Box.createVerticalStrut(6));
        headings.add(label("Set up your inheritance tree", 19, TEXT, Font.BOLD));
        headings.add(Box.createVerticalStrut(4));
        headings.add(label("Select the trainee, parents, and grandparents you’re planning around.", 12, MUTED, Font.PLAIN));
        intro.add(headings, BorderLayout.CENTER);

        JPanel traineeRow = new JPanel(new BorderLayout());
        traineeRow.setOpaque(false);
        traineeRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        UmaSlot trainee = createSlot(UmaRole.TRAINEE, "TRAINING");
        traineeRow.add(trainee, BorderLayout.CENTER);

        JPanel branches = new JPanel(new GridLayout(1, 2, 16, 0));
        branches.setOpaque(false);
        branches.add(buildParentBranch(UmaRole.PARENT_A, UmaRole.GRANDPARENT_A1, UmaRole.GRANDPARENT_A2, "PARENT 01"));
        branches.add(buildParentBranch(UmaRole.PARENT_B, UmaRole.GRANDPARENT_B1, UmaRole.GRANDPARENT_B2, "PARENT 02"));

        JPanel tree = new JPanel(new BorderLayout(0, 10));
        tree.setOpaque(false);
        tree.add(traineeRow, BorderLayout.NORTH);
        tree.add(branches, BorderLayout.CENTER);
        card.add(intro, BorderLayout.NORTH);
        card.add(tree, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildParentBranch(
            UmaRole parent,
            UmaRole grandparent1,
            UmaRole grandparent2,
            String heading) {
        JPanel branch = new JPanel(new BorderLayout(0, 11));
        branch.setBackground(SURFACE_LIGHT);
        branch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        branch.add(label(heading, 10, GOLD, Font.BOLD), BorderLayout.NORTH);
        branch.add(createSlot(parent, "PARENT"), BorderLayout.CENTER);
        JPanel grandparents = new JPanel(new GridLayout(1, 2, 10, 0));
        grandparents.setOpaque(false);
        grandparents.add(createSlot(grandparent1, "GRANDPARENT"));
        grandparents.add(createSlot(grandparent2, "GRANDPARENT"));
        branch.add(grandparents, BorderLayout.SOUTH);
        return branch;
    }

    private JPanel buildCalculatorCards() {
        JPanel row = new JPanel(new GridLayout(1, 2, 18, 0));
        row.setOpaque(false);
        row.add(buildInputsCard());
        row.add(buildResultCard());
        return row;
    }

    private JComponent buildInputsCard() {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        card.add(label("CAREER OUTCOME", 11, PINK, Font.BOLD));
        card.add(Box.createVerticalStrut(6));
        card.add(label("Factor probability", 19, TEXT, Font.BOLD));
        card.add(Box.createVerticalStrut(5));
        card.add(label("Enter final stats, then choose the blue spark you want.", 12, MUTED, Font.PLAIN));
        card.add(Box.createVerticalStrut(18));
        card.add(label("FINAL STATS", 10, MUTED, Font.BOLD));

        JPanel stats = new JPanel(new GridLayout(2, 3, 9, 9));
        stats.setOpaque(false);
        for (SparkProbabilityCalculator.Stat stat : SparkProbabilityCalculator.Stat.values()) {
            JPanel statCell = new JPanel(new BorderLayout(0, 5));
            statCell.setOpaque(false);
            statCell.add(label(statLabel(stat), 11, MUTED, Font.BOLD), BorderLayout.NORTH);
            JTextField input = textField(stat == SparkProbabilityCalculator.Stat.SPEED ? "1200" : "600");
            input.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateCalculation));
            statFields.put(stat, input);
            statCell.add(input, BorderLayout.CENTER);
            stats.add(statCell);
        }
        card.add(stats);
        card.add(Box.createVerticalStrut(17));
        card.add(label("TARGET BLUE SPARK", 10, MUTED, Font.BOLD));
        JPanel targets = new JPanel(new GridLayout(1, 5, 6, 0));
        targets.setOpaque(false);
        for (SparkProbabilityCalculator.Stat stat : SparkProbabilityCalculator.Stat.values()) {
            JButton button = new JButton(statLabel(stat));
            button.setFocusPainted(false);
            button.setForeground(TEXT);
            button.setBackground(targetStats.contains(stat) ? new Color(116, 48, 81) : SURFACE_LIGHT);
            button.setBorder(BorderFactory.createLineBorder(targetStats.contains(stat) ? PINK : BORDER));
            button.setFont(new Font("SansSerif", Font.BOLD, 10));
            button.addActionListener(event -> {
                if (targetStats.contains(stat)) {
                    targetStats.remove(stat);
                } else {
                    targetStats.add(stat);
                }
                refreshTargetButtons();
                updateCalculation();
            });
            statButtons.put(stat, button);
            targets.add(button);
        }
        card.add(targets);
        card.add(Box.createVerticalStrut(16));

        JPanel ranges = new JPanel(new GridLayout(1, 2, 10, 0));
        ranges.setOpaque(false);
        ranges.add(fieldWithLabel("STAR RANGE  ·  FROM", minimumStars));
        ranges.add(fieldWithLabel("TO", maximumStars));
        minimumStars.setSelectedItem(3);
        maximumStars.setSelectedItem(3);
        minimumStars.addActionListener(event -> updateCalculation());
        maximumStars.addActionListener(event -> updateCalculation());
        card.add(ranges);
        return card;
    }

    private JComponent buildResultCard() {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        card.add(label("YOUR ODDS", 11, GOLD, Font.BOLD));
        card.add(Box.createVerticalStrut(8));
        card.add(label("One career, one spark roll.", 16, TEXT, Font.BOLD));
        card.add(Box.createVerticalStrut(17));

        JPanel chance = new JPanel(new BorderLayout());
        chance.setBackground(SURFACE_LIGHT);
        chance.setBorder(BorderFactory.createEmptyBorder(16, 17, 16, 17));
        JPanel chanceCopy = new JPanel();
        chanceCopy.setOpaque(false);
        chanceCopy.setLayout(new BoxLayout(chanceCopy, BoxLayout.Y_AXIS));
        chanceCopy.add(label("CHANCE PER CAREER", 10, MUTED, Font.BOLD));
        chanceCopy.add(Box.createVerticalStrut(5));
        chanceValue.setFont(new Font("SansSerif", Font.BOLD, 36));
        chanceValue.setForeground(GOLD);
        chanceCopy.add(chanceValue);
        chance.add(chanceCopy, BorderLayout.CENTER);
        card.add(chance);
        card.add(Box.createVerticalStrut(14));

        JPanel runs = new JPanel(new BorderLayout(8, 0));
        runs.setOpaque(false);
        runs.add(label("Careers for confidence", 12, MUTED, Font.PLAIN), BorderLayout.WEST);
        runsValue.setFont(new Font("SansSerif", Font.BOLD, 20));
        runsValue.setForeground(TEXT);
        runs.add(runsValue, BorderLayout.EAST);
        card.add(runs);
        card.add(Box.createVerticalStrut(8));
        JPanel confidenceRow = new JPanel(new BorderLayout(8, 0));
        confidenceRow.setOpaque(false);
        confidenceRow.add(label("Desired confidence", 12, MUTED, Font.PLAIN), BorderLayout.WEST);
        confidence.setSelectedItem(90);
        confidence.addActionListener(event -> updateCalculation());
        confidenceRow.add(confidence, BorderLayout.EAST);
        card.add(confidenceRow);
        card.add(Box.createVerticalStrut(14));
        resultMessage.setFont(new Font("SansSerif", Font.PLAIN, 11));
        resultMessage.setForeground(MUTED);
        card.add(resultMessage);
        card.add(Box.createVerticalStrut(10));
        JLabel note = label("Lineage is shown for planning; it does not alter this conditional career estimate.", 11, MUTED, Font.PLAIN);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(note);
        return card;
    }

    private JPanel fieldWithLabel(String title, JComboBox<Integer> field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.add(label(title, 10, MUTED, Font.BOLD), BorderLayout.NORTH);
        field.setBackground(SURFACE_LIGHT);
        field.setForeground(TEXT);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private UmaSlot createSlot(UmaRole role, String caption) {
        UmaSlot slot = new UmaSlot(role, caption);
        slots.put(role, slot);
        return slot;
    }

    private void setCharacter(UmaCharacter character, UmaRole role) {
        UmaSlot slot = slots.get(role);
        if (slot != null) {
            slot.setCharacter(character);
        }
    }

    private void openPicker(UmaSlot slot) {
        JDialog dialog = new JDialog(frame, "Choose " + slot.caption.toLowerCase(Locale.ROOT), true);
        dialog.setMinimumSize(new Dimension(475, 580));
        dialog.setSize(520, 640);
        dialog.setLocationRelativeTo(frame);
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel title = label("Choose an Uma", 20, TEXT, Font.BOLD);
        JTextField search = textField("");
        search.putClientProperty("JTextField.placeholderText", "Search the local Uma roster…");
        JPanel top = new JPanel(new BorderLayout(0, 11));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(search, BorderLayout.CENTER);
        content.add(top, BorderLayout.NORTH);

        DefaultListModel<UmaCharacter> model = new DefaultListModel<>();
        JList<UmaCharacter> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(SURFACE_LIGHT);
        list.setForeground(TEXT);
        list.setFixedCellHeight(62);
        list.setCellRenderer(new CharacterCellRenderer());
        Runnable filter = () -> {
            String query = search.getText().trim().toLowerCase(Locale.ROOT);
            model.clear();
            for (UmaCharacter character : catalog) {
                if (character.name.toLowerCase(Locale.ROOT).contains(query)
                        || character.slug.toLowerCase(Locale.ROOT).contains(query)) {
                    model.addElement(character);
                }
            }
            if (!model.isEmpty()) {
                list.setSelectedIndex(0);
            }
        };
        filter.run();
        search.getDocument().addDocumentListener(new SimpleDocumentListener(filter));
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && list.getSelectedValue() != null) {
                    slot.setCharacter(list.getSelectedValue());
                    dialog.dispose();
                }
            }
        });
        content.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        rosterStatus.setText(catalog.size() + " Uma Musume stored locally · works offline");
        rosterStatus.setForeground(MUTED);
        rosterStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bottom.add(rosterStatus, BorderLayout.NORTH);
        JPanel custom = new JPanel(new BorderLayout(8, 0));
        custom.setOpaque(false);
        JTextField customName = textField("");
        customName.putClientProperty("JTextField.placeholderText", "Or add a custom Uma name");
        JButton addCustom = primaryButton("Use name");
        addCustom.addActionListener(event -> {
            String name = customName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Enter an Uma name first.", "Name required",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            slot.setCharacter(new UmaCharacter("custom-" + name.toLowerCase(Locale.ROOT), name, null));
            dialog.dispose();
        });
        customName.addActionListener(event -> addCustom.doClick());
        custom.add(customName, BorderLayout.CENTER);
        custom.add(addCustom, BorderLayout.EAST);
        bottom.add(custom, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private UmaCharacter findCharacter(String slug) {
        for (UmaCharacter character : catalog) {
            if (character.slug.equals(slug)) {
                return character;
            }
        }
        throw new IllegalStateException("The local Uma catalog is missing " + slug + ".");
    }

    private static List<UmaCharacter> localCatalog() {
        List<UmaCharacter> characters = new ArrayList<>();
        for (String entry : UmaCatalog.entries()) {
            String[] fields = entry.split("\\|", 2);
            if (fields.length != 2) {
                throw new IllegalStateException("Invalid entry in the bundled Uma catalog: " + entry);
            }
            characters.add(characterFromSlug(fields[0], fields[1]));
        }
        return characters;
    }

    private static UmaCharacter characterFromSlug(String slug, String id) {
        String specialName = SPECIAL_NAMES.get(slug);
        if (specialName != null) {
            return new UmaCharacter(slug, specialName, id);
        }
        String[] words = slug.split("-");
        StringBuilder name = new StringBuilder();
        for (String word : words) {
            if (name.length() > 0) {
                name.append(' ');
            }
            if (word.equals("tm")) {
                name.append("TM");
            } else if (word.equals("mcqueen")) {
                name.append("McQueen");
            } else {
                name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return new UmaCharacter(slug, name.toString(), id);
    }

    private void updateCalculation() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::updateCalculation);
            return;
        }
        try {
            Map<SparkProbabilityCalculator.Stat, Integer> stats =
                    new EnumMap<>(SparkProbabilityCalculator.Stat.class);
            for (SparkProbabilityCalculator.Stat stat : SparkProbabilityCalculator.Stat.values()) {
                String text = statFields.get(stat).getText().trim();
                if (text.isEmpty()) {
                    setInvalidCalculation("Complete all five final stat values.");
                    return;
                }
                int value = Integer.parseInt(text);
                if (value < 0) {
                    setInvalidCalculation("Final stats cannot be negative.");
                    return;
                }
                stats.put(stat, value);
            }
            if (targetStats.isEmpty()) {
                setInvalidCalculation("Choose at least one target stat.");
                return;
            }
            int from = (Integer) minimumStars.getSelectedItem();
            int to = (Integer) maximumStars.getSelectedItem();
            if (from > to) {
                setInvalidCalculation("The first star value must be at or below the last.");
                return;
            }
            double probability = SparkProbabilityCalculator.blueFactorProbability(stats, targetStats,
                    new SparkProbabilityCalculator.StarRange(from, to));
            int desiredConfidence = (Integer) confidence.getSelectedItem();
            chanceValue.setText(probability == 0.0
                    ? "0.00%"
                    : probability < 0.0001
                    ? "< 0.01%"
                    : String.format(Locale.ROOT, "%.2f%%", probability * 100));
            if (probability == 0.0) {
                runsValue.setText("∞");
                resultMessage.setText("This target has no chance with the current career outcome.");
                resultMessage.setForeground(MUTED);
                return;
            }
            int requiredRuns;
            try {
                requiredRuns = SparkProbabilityCalculator.runsForConfidence(
                        probability, desiredConfidence / 100.0);
                runsValue.setText(String.valueOf(requiredRuns));
            } catch (IllegalArgumentException exception) {
                if (!"Required run count exceeds the supported range.".equals(exception.getMessage())) {
                    throw exception;
                }
                requiredRuns = Integer.MAX_VALUE;
                runsValue.setText("> 2.1B");
            }
            resultMessage.setText(String.format(Locale.ROOT,
                    "%.1f%% chance of at least one target spark across the estimated careers.",
                    SparkProbabilityCalculator.chanceOfAtLeastOneSuccess(
                            probability,
                            requiredRuns)
                            * 100));
            resultMessage.setForeground(MUTED);
        } catch (NumberFormatException exception) {
            setInvalidCalculation("Use whole numbers for final stats.");
        } catch (IllegalArgumentException exception) {
            setInvalidCalculation(exception.getMessage());
        }
    }

    private void setInvalidCalculation(String message) {
        chanceValue.setText("—");
        runsValue.setText("—");
        resultMessage.setText(message);
        resultMessage.setForeground(new Color(255, 151, 158));
    }

    private void refreshTargetButtons() {
        for (Map.Entry<SparkProbabilityCalculator.Stat, JButton> entry : statButtons.entrySet()) {
            boolean selected = targetStats.contains(entry.getKey());
            entry.getValue().setBackground(selected ? new Color(116, 48, 81) : SURFACE_LIGHT);
            entry.getValue().setBorder(BorderFactory.createLineBorder(selected ? PINK : BORDER));
        }
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER));
        return panel;
    }

    private static JLabel label(String text, int size, Color color, int style) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", style, size));
        label.setForeground(color);
        return label;
    }

    private static JTextField textField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBackground(SURFACE_LIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 9, 8, 9)));
        return field;
    }

    private static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(BACKGROUND);
        button.setBackground(GOLD);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        return button;
    }

    private static String statLabel(SparkProbabilityCalculator.Stat stat) {
        switch (stat) {
            case SPEED:
                return "SPEED";
            case STAMINA:
                return "STAMINA";
            case POWER:
                return "POWER";
            case GUTS:
                return "GUTS";
            case WISDOM:
                return "WISDOM";
            default:
                throw new IllegalStateException("Unexpected stat: " + stat);
        }
    }

    private final class UmaSlot extends JPanel {
        private static final long serialVersionUID = 1L;
        private final UmaRole role;
        private final String caption;
        private final UmaPortrait portrait = new UmaPortrait(76);
        private final JLabel name = label("Choose Uma", 12, TEXT, Font.BOLD);
        private UmaCharacter character;

        private UmaSlot(UmaRole role, String caption) {
            this.role = role;
            this.caption = caption;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel roleLabel = label(caption, 9, MUTED, Font.BOLD);
            roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            portrait.setAlignmentX(Component.CENTER_ALIGNMENT);
            name.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(roleLabel);
            add(Box.createVerticalStrut(7));
            add(portrait);
            add(Box.createVerticalStrut(5));
            add(name);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            MouseAdapter pickerClick = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    openPicker(UmaSlot.this);
                }
            };
            addMouseListener(pickerClick);
            roleLabel.addMouseListener(pickerClick);
            portrait.addMouseListener(pickerClick);
            name.addMouseListener(pickerClick);
            roleLabel.setCursor(getCursor());
            portrait.setCursor(getCursor());
            name.setCursor(getCursor());
            setToolTipText("Click to choose from the Uma roster");
            roleLabel.setToolTipText(getToolTipText());
            portrait.setToolTipText(getToolTipText());
            name.setToolTipText(getToolTipText());
        }

        private void setCharacter(UmaCharacter selected) {
            character = selected;
            name.setText(selected.name);
            portrait.setCharacter(selected);
        }
    }

    private static final class UmaPortrait extends JComponent {
        private static final long serialVersionUID = 1L;
        private final int diameter;
        private UmaCharacter character;

        private UmaPortrait(int diameter) {
            this.diameter = diameter;
            setPreferredSize(new Dimension(diameter, diameter));
            setMinimumSize(new Dimension(diameter, diameter));
            setMaximumSize(new Dimension(diameter, diameter));
        }

        private void setCharacter(UmaCharacter selected) {
            character = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = diameter - 4;
            Color accent = character == null
                    ? PINK
                    : new Color(Color.HSBtoRGB(
                            (character.slug.hashCode() & 0x7fffffff) % 360 / 360f, 0.42f, 0.84f));
            g.setColor(PORTRAIT_PLACEHOLDER);
            g.fillOval(2, 2, size, size);
            BufferedImage portrait = character == null ? null : PortraitCache.get(character.slug);
            if (portrait != null) {
                Shape originalClip = g.getClip();
                g.setClip(2, 2, size, size);
                drawPortraitCrop(g, portrait, size);
                g.setClip(originalClip);
            } else {
                drawLocalMonogram(g, size, accent);
            }
            g.setColor(accent);
            g.setStroke(new java.awt.BasicStroke(2f));
            g.drawOval(2, 2, size, size);
            setToolTipText(character == null
                    ? "Choose an Uma to add it to this lineage slot."
                    : portrait == null
                    ? "No local JPG found. Add assets/portraits/" + character.slug + ".jpg."
                    : "Portrait loaded from local assets/portraits/" + character.slug + ".jpg.");
            g.dispose();
        }

        private void drawPortraitCrop(Graphics2D graphics, BufferedImage image, int size) {
            int sourceWidth = image.getWidth();
            int sourceHeight = image.getHeight();
            int cropX = 0;
            int cropY = 0;
            int cropSize = Math.min(sourceWidth, sourceHeight);
            if (sourceWidth > sourceHeight) {
                cropX = (sourceWidth - cropSize) / 2;
            } else {
                cropY = (sourceHeight - cropSize) / 2;
            }
            graphics.drawImage(image, 2, 2, size + 2, size + 2,
                    cropX, cropY, cropX + cropSize, cropY + cropSize, null);
        }

        private void drawLocalMonogram(Graphics2D graphics, int size, Color accent) {
            graphics.setComposite(AlphaComposite.SrcOver.derive(0.2f));
            graphics.setColor(accent);
            graphics.fillArc(2 + size / 9, 2 + size / 9, size * 7 / 9, size * 7 / 9, 24, 225);
            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 175));
            graphics.setStroke(new java.awt.BasicStroke(Math.max(2f, diameter / 18f),
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            graphics.drawArc(2 + size / 5, 2 + size / 5, size * 3 / 5, size * 3 / 5, 30, 120);
            graphics.drawArc(2 + size / 5, 2 + size / 5, size * 3 / 5, size * 3 / 5, 210, 120);
            graphics.setColor(new Color(28, 35, 56, 225));
            int badgeSize = size / 2;
            graphics.fillOval((diameter - badgeSize) / 2, (diameter - badgeSize) / 2,
                    badgeSize, badgeSize);
            drawInitials(graphics);
        }

        private void drawInitials(Graphics2D graphics) {
            String initials = character == null ? "+" : character.initials();
            graphics.setColor(PINK);
            graphics.setFont(new Font("SansSerif", Font.BOLD, Math.max(16, diameter / 4)));
            java.awt.FontMetrics metrics = graphics.getFontMetrics();
            graphics.drawString(initials, (diameter - metrics.stringWidth(initials)) / 2,
                    (diameter - metrics.getHeight()) / 2 + metrics.getAscent());
        }
    }

    private static final class CharacterCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        private final JPanel row = new JPanel(new BorderLayout(12, 0));
        private final UmaPortrait avatar = new UmaPortrait(46);
        private final JLabel name = label("", 13, TEXT, Font.BOLD);
        private final JLabel detail = label("Bundled Uma character", 10, MUTED, Font.PLAIN);

        private CharacterCellRenderer() {
            JPanel copy = new JPanel();
            copy.setOpaque(false);
            copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
            copy.add(name);
            copy.add(Box.createVerticalStrut(4));
            copy.add(detail);
            row.setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 9));
            row.add(avatar, BorderLayout.WEST);
            row.add(copy, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean selected, boolean focused) {
            super.getListCellRendererComponent(list, value, index, selected, focused);
            UmaCharacter character = (UmaCharacter) value;
            name.setText(character.name);
            String imageState = character.id == null
                    ? "Custom name · local monogram"
                    : PortraitCache.get(character.slug) == null
                    ? "Bundled Global Uma · local monogram"
                    : "Bundled Global Uma · local JPG";
            detail.setText(imageState);
            avatar.setCharacter(character);
            row.setBackground(selected ? new Color(65, 49, 77) : SURFACE_LIGHT);
            row.setOpaque(true);
            return row;
        }
    }

    private static final class PortraitCache {
        private static final Map<String, BufferedImage> IMAGES = new HashMap<>();
        private static final Set<String> CHECKED = new java.util.HashSet<>();

        private static BufferedImage get(String slug) {
            if (!CHECKED.add(slug)) {
                return IMAGES.get(slug);
            }

            String fileName = slug + ".jpg";
            try (InputStream resource = SparkCalculatorWindow.class.getResourceAsStream(
                    "/portraits/" + fileName)) {
                if (resource != null) {
                    return cache(slug, ImageIO.read(resource));
                }

                Path[] localPaths = {
                        Paths.get("assets", "portraits", fileName),
                        Paths.get("portraits", fileName)
                };
                for (Path path : localPaths) {
                    if (Files.isRegularFile(path)) {
                        return cache(slug, ImageIO.read(path.toFile()));
                    }
                }
            } catch (IOException exception) {
                System.err.println("Unable to load local portrait for " + slug + ": " + exception.getMessage());
            }
            return null;
        }

        private static BufferedImage cache(String slug, BufferedImage image) {
            if (image != null) {
                IMAGES.put(slug, image);
            }
            return image;
        }
    }

    private static final class UmaCharacter {
        private final String slug;
        private final String name;
        private final String id;

        private UmaCharacter(String slug, String name, String id) {
            this.slug = slug;
            this.name = name;
            this.id = id;
        }

        private String initials() {
            String[] words = name.trim().split("\\s+");
            if (words.length == 1) {
                return words[0].substring(0, 1).toUpperCase(Locale.ROOT);
            }
            return (words[0].substring(0, 1) + words[1].substring(0, 1)).toUpperCase(Locale.ROOT);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof UmaCharacter)) {
                return false;
            }
            return slug.equals(((UmaCharacter) other).slug);
        }

        @Override
        public int hashCode() {
            return slug.hashCode();
        }
    }

    private enum UmaRole {
        TRAINEE,
        PARENT_A,
        PARENT_B,
        GRANDPARENT_A1,
        GRANDPARENT_A2,
        GRANDPARENT_B1,
        GRANDPARENT_B2
    }

    private static final class SimpleDocumentListener implements DocumentListener {
        private final Runnable action;

        private SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void insertUpdate(DocumentEvent event) {
            action.run();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            action.run();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            action.run();
        }
    }
}
