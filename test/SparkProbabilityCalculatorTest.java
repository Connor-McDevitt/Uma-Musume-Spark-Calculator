import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SparkProbabilityCalculatorTest {
    public static void main(String[] args) {
        testBlueProbability();
        testBlueThresholds();
        testPinkProbability();
        testGreenProbability();
        testWhiteProbability();
        testRepeatedRuns();
        testBundledUmaCatalog();
        testInvalidInputs();
        System.out.println("All probability calculator tests passed.");
    }

    private static void testBlueProbability() {
        Map<SparkProbabilityCalculator.Stat, Integer> stats =
                new EnumMap<>(SparkProbabilityCalculator.Stat.class);
        for (SparkProbabilityCalculator.Stat stat : SparkProbabilityCalculator.Stat.values()) {
            stats.put(stat, 1200);
        }

        double probability = SparkProbabilityCalculator.blueFactorProbability(
                stats,
                EnumSet.of(SparkProbabilityCalculator.Stat.SPEED),
                new SparkProbabilityCalculator.StarRange(3, 3));
        assertNear(0.02, probability);

        assertNear(1.0, SparkProbabilityCalculator.blueFactorProbability(
                stats,
                EnumSet.allOf(SparkProbabilityCalculator.Stat.class),
                new SparkProbabilityCalculator.StarRange(1, 3)));
    }

    private static void testBlueThresholds() {
        assertNear(0.18, blueSpeedStarChance(599, 1));
        assertNear(0.10, blueSpeedStarChance(600, 1));
        assertNear(0.10, blueSpeedStarChance(1100, 1));
        assertNear(0.04, blueSpeedStarChance(1101, 1));
        assertNear(0.02, blueSpeedStarChance(1101, 3));
    }

    private static void testPinkProbability() {
        double probability = SparkProbabilityCalculator.pinkFactorProbability(
                Map.of("Turf", 0.5, "Dirt", 0.5),
                Set.of("Turf"),
                new SparkProbabilityCalculator.StarRange(2, 3),
                new double[]{0.3, 0.5, 0.2});
        assertNear(0.35, probability);
    }

    private static void testGreenProbability() {
        assertNear(0.0, SparkProbabilityCalculator.greenFactorProbability(
                false,
                new SparkProbabilityCalculator.StarRange(1, 3),
                new double[]{0.5, 0.4, 0.1}));
        assertNear(0.4, SparkProbabilityCalculator.greenFactorProbability(
                true,
                new SparkProbabilityCalculator.StarRange(2, 2),
                new double[]{0.5, 0.4, 0.1}));
    }

    private static void testWhiteProbability() {
        double oneFactor = SparkProbabilityCalculator.whiteFactorProbability(
                SparkProbabilityCalculator.WhiteFactorCategory.NORMAL_SKILL,
                new SparkProbabilityCalculator.StarRange(3, 3),
                new double[]{0.5, 0.45, 0.05});
        assertNear(0.01, oneFactor);
        assertNear(0.0001, SparkProbabilityCalculator.requiredWhiteCombinationProbability(
                List.of(oneFactor, oneFactor)));
        assertNear(0.40, SparkProbabilityCalculator.WhiteFactorCategory.GOLD_SKILL
                .generationProbability());
    }

    private static void testRepeatedRuns() {
        int runs = SparkProbabilityCalculator.runsForConfidence(0.5, 0.9);
        if (runs != 4) {
            throw new AssertionError("Expected four runs for 90% confidence; got " + runs);
        }
        if (SparkProbabilityCalculator.chanceOfAtLeastOneSuccess(0.5, runs) < 0.9
                || SparkProbabilityCalculator.chanceOfAtLeastOneSuccess(0.5, runs - 1) >= 0.9) {
            throw new AssertionError("Run count does not meet the minimum confidence threshold.");
        }
        assertNear(0.0, SparkProbabilityCalculator.chanceOfAtLeastOneSuccess(0.5, 0));
        if (SparkProbabilityCalculator.runsForConfidence(1.0, 0.99) != 1) {
            throw new AssertionError("A guaranteed success must require one run.");
        }
    }

    private static void testBundledUmaCatalog() {
        List<String> catalog = UmaCatalog.entries();
        if (catalog.size() != 69) {
            throw new AssertionError("Expected 69 Global-released Uma records; got " + catalog.size());
        }
        Set<String> slugs = new HashSet<>();
        Set<String> characterIds = new HashSet<>();
        for (String entry : catalog) {
            String[] fields = entry.split("\\|", -1);
            if (fields.length != 2 || !fields[0].matches("[a-z0-9-]+")
                    || !fields[1].matches("\\d{6}")) {
                throw new AssertionError("Invalid bundled Uma record: " + entry);
            }
            if (!slugs.add(fields[0]) || !characterIds.add(fields[1])) {
                throw new AssertionError("Duplicate bundled Uma slug or character ID: " + entry);
            }
        }
        if (!slugs.contains("special-week") || !slugs.contains("zenno-rob-roy")
                || !slugs.contains("narita-brian") || !slugs.contains("kitasan-black")
                || slugs.contains("admire-groove") || slugs.contains("orfevre")) {
            throw new AssertionError("The Global roster is missing released characters or includes unreleased entries.");
        }
    }

    private static void testInvalidInputs() {
        expectIllegalArgument(() -> SparkProbabilityCalculator.pinkFactorProbability(
                Map.of("Turf", 0.7),
                Set.of("Turf"),
                new SparkProbabilityCalculator.StarRange(1, 3),
                new double[]{0.5, 0.4, 0.1}));
        expectIllegalArgument(() -> SparkProbabilityCalculator.runsForConfidence(0.0, 0.9));
        expectIllegalArgument(() -> SparkProbabilityCalculator.whiteFactorProbability(
                SparkProbabilityCalculator.WhiteFactorCategory.GOLD_SKILL,
                new SparkProbabilityCalculator.StarRange(1, 3),
                new double[]{0.5, 0.4, 0.2}));
    }

    private static void assertNear(double expected, double actual) {
        if (Math.abs(expected - actual) > 1.0e-9) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private static double blueSpeedStarChance(int speed, int stars) {
        Map<SparkProbabilityCalculator.Stat, Integer> stats =
                new EnumMap<>(SparkProbabilityCalculator.Stat.class);
        for (SparkProbabilityCalculator.Stat stat : SparkProbabilityCalculator.Stat.values()) {
            stats.put(stat, 600);
        }
        stats.put(SparkProbabilityCalculator.Stat.SPEED, speed);
        return SparkProbabilityCalculator.blueFactorProbability(
                stats,
                EnumSet.of(SparkProbabilityCalculator.Stat.SPEED),
                new SparkProbabilityCalculator.StarRange(stars, stars));
    }

    private static void expectIllegalArgument(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException.");
    }
}
