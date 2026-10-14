import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<SparkProbabilityCalculator.Stat, Integer> finalStats =
                new EnumMap<>(SparkProbabilityCalculator.Stat.class);
        finalStats.put(SparkProbabilityCalculator.Stat.SPEED, 1200);
        finalStats.put(SparkProbabilityCalculator.Stat.STAMINA, 600);
        finalStats.put(SparkProbabilityCalculator.Stat.POWER, 600);
        finalStats.put(SparkProbabilityCalculator.Stat.GUTS, 600);
        finalStats.put(SparkProbabilityCalculator.Stat.WISDOM, 600);

        double probability = SparkProbabilityCalculator.blueFactorProbability(
                finalStats,
                EnumSet.of(SparkProbabilityCalculator.Stat.SPEED),
                new SparkProbabilityCalculator.StarRange(3, 3));

        System.out.printf("Chance of a 3★ Speed blue spark: %.2f%%%n", probability * 100);
        System.out.printf("Runs for 90%% success confidence: %d%n",
                SparkProbabilityCalculator.runsForConfidence(probability, 0.90));
    }
}
