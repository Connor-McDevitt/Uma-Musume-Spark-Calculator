import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SparkProbabilityCalculator {
    private static final double PROBABILITY_TOLERANCE = 1.0e-9;

    private SparkProbabilityCalculator() {
    }

    public enum Stat {
        SPEED,
        STAMINA,
        POWER,
        GUTS,
        WISDOM
    }

    public enum WhiteFactorCategory {
        NORMAL_SKILL(0.20),
        TWO_CIRCLE_SKILL(0.25),
        GOLD_SKILL(0.40),
        G1_RACE(0.20),
        CAREER(0.20);

        private final double generationProbability;

        WhiteFactorCategory(double generationProbability) {
            this.generationProbability = generationProbability;
        }

        public double generationProbability() {
            return generationProbability;
        }
    }

    public record StarRange(int minimum, int maximum) {
        public StarRange {
            if (minimum < 1 || maximum > 3 || minimum > maximum) {
                throw new IllegalArgumentException("Star range must be within 1★–3★.");
            }
        }

        public boolean includes(int stars) {
            return stars >= minimum && stars <= maximum;
        }
    }

    /**
     * Returns the probability of the requested blue-spark stat and star range.
     * The game source describes stat selection as random but does not specify
     * its distribution, so equal odds across the five stats are an assumption.
     */
    public static double blueFactorProbability(
            Map<Stat, Integer> finalStats,
            Set<Stat> targetStats,
            StarRange targetStars) {
        if (finalStats == null || finalStats.size() != Stat.values().length
                || !finalStats.keySet().containsAll(List.of(Stat.values()))) {
            throw new IllegalArgumentException("Final values for all five stats are required.");
        }
        if (targetStats == null || targetStats.isEmpty()) {
            throw new IllegalArgumentException("At least one target blue-spark stat is required.");
        }
        if (targetStars == null) {
            throw new IllegalArgumentException("A target blue-spark star range is required.");
        }
        double probability = 0.0;
        for (Stat stat : Stat.values()) {
            Integer finalStat = finalStats.get(stat);
            if (finalStat == null) {
                throw new IllegalArgumentException("Final values for all five stats are required.");
            }
            int value = finalStat;
            if (value < 0) {
                throw new IllegalArgumentException("Final stat values cannot be negative.");
            }
            if (targetStats.contains(stat)) {
                probability += blueStarProbability(value, targetStars) / Stat.values().length;
            }
        }
        return probability;
    }

    /**
     * Returns a pink-factor probability from user-supplied type-selection and
     * star probabilities. Published sources do not provide numeric rates for
     * these rolls, so callers must label these inputs as estimates.
     */
    public static double pinkFactorProbability(
            Map<String, Double> eligibleAptitudeProbabilities,
            Set<String> targetAptitudes,
            StarRange targetStars,
            double[] estimatedStarProbabilities) {
        validateDistribution(eligibleAptitudeProbabilities, "Aptitude selection");
        if (targetAptitudes == null || targetAptitudes.isEmpty()) {
            throw new IllegalArgumentException("At least one target aptitude is required.");
        }
        if (targetStars == null) {
            throw new IllegalArgumentException("A target pink-spark star range is required.");
        }
        double starProbability = probabilityInRange(estimatedStarProbabilities, targetStars);
        double aptitudeProbability = 0.0;
        for (String aptitude : targetAptitudes) {
            aptitudeProbability += eligibleAptitudeProbabilities.getOrDefault(aptitude, 0.0);
        }
        return aptitudeProbability * starProbability;
    }

    /**
     * Returns the probability of a green factor with the requested stars.
     * Star odds must be supplied as an estimate; only the 3★-or-higher
     * eligibility rule is represented here.
     */
    public static double greenFactorProbability(
            boolean eligibleAtCareerStart,
            StarRange targetStars,
            double[] estimatedStarProbabilities) {
        if (!eligibleAtCareerStart) {
            return 0.0;
        }
        if (targetStars == null) {
            throw new IllegalArgumentException("A target green-spark star range is required.");
        }
        return probabilityInRange(estimatedStarProbabilities, targetStars);
    }

    /**
     * Returns the probability that one specified eligible white factor is
     * generated with stars in the requested range.
     */
    public static double whiteFactorProbability(
            WhiteFactorCategory category,
            StarRange targetStars,
            double[] starProbabilities) {
        if (category == null || targetStars == null) {
            throw new IllegalArgumentException("A white-factor category and star range are required.");
        }
        return category.generationProbability()
                * probabilityInRange(starProbabilities, targetStars);
    }

    /**
     * Multiplies the probabilities of all required white factors, assuming
     * their generation and star rolls are independent.
     */
    public static double requiredWhiteCombinationProbability(List<Double> factorProbabilities) {
        return multiplyIndependentProbabilities(factorProbabilities, false);
    }

    /**
     * Combines distinct factor-type probabilities under an independence
     * assumption. The game sources do not establish independence.
     */
    public static double independentCombinationProbability(List<Double> factorProbabilities) {
        return multiplyIndependentProbabilities(factorProbabilities, true);
    }

    private static double multiplyIndependentProbabilities(
            List<Double> factorProbabilities,
            boolean requireAtLeastOne) {
        if (factorProbabilities == null) {
            throw new IllegalArgumentException("Factor probabilities are required.");
        }
        if (requireAtLeastOne && factorProbabilities.isEmpty()) {
            throw new IllegalArgumentException("At least one factor probability is required.");
        }
        double probability = 1.0;
        for (double factorProbability : factorProbabilities) {
            validateProbability(factorProbability, "Factor probability");
            probability *= factorProbability;
        }
        return probability;
    }

    public static double chanceOfAtLeastOneSuccess(double probabilityPerRun, int runs) {
        validateProbability(probabilityPerRun, "Probability per run");
        if (runs < 0) {
            throw new IllegalArgumentException("Run count cannot be negative.");
        }
        if (runs == 0 || probabilityPerRun == 0.0) {
            return 0.0;
        }
        if (probabilityPerRun == 1.0) {
            return 1.0;
        }
        return -Math.expm1(runs * Math.log1p(-probabilityPerRun));
    }

    public static int runsForConfidence(double probabilityPerRun, double desiredConfidence) {
        validateProbability(probabilityPerRun, "Probability per run");
        validateProbability(desiredConfidence, "Desired confidence");
        if (desiredConfidence == 0.0) {
            return 0;
        }
        if (probabilityPerRun == 0.0) {
            throw new IllegalArgumentException("A zero per-run chance cannot reach positive confidence.");
        }
        if (probabilityPerRun == 1.0) {
            return 1;
        }
        double runs = Math.log1p(-desiredConfidence) / Math.log1p(-probabilityPerRun);
        if (runs > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Required run count exceeds the supported range.");
        }
        return (int) Math.ceil(runs);
    }

    private static double blueStarProbability(int finalStat, StarRange targetStars) {
        double[] probabilities;
        if (finalStat < 600) {
            probabilities = new double[]{0.90, 0.10, 0.00};
        } else if (finalStat <= 1100) {
            probabilities = new double[]{0.50, 0.45, 0.05};
        } else {
            probabilities = new double[]{0.20, 0.70, 0.10};
        }
        return sumInRange(probabilities, targetStars);
    }

    private static double probabilityInRange(double[] probabilities, StarRange targetStars) {
        validateDistribution(probabilities, "Star probability");
        return sumInRange(probabilities, targetStars);
    }

    private static double sumInRange(double[] probabilities, StarRange targetStars) {
        double sum = 0.0;
        for (int stars = targetStars.minimum(); stars <= targetStars.maximum(); stars++) {
            sum += probabilities[stars - 1];
        }
        return sum;
    }

    private static void validateDistribution(Map<String, Double> probabilities, String label) {
        if (probabilities == null || probabilities.isEmpty()) {
            throw new IllegalArgumentException(label + " probabilities are required.");
        }
        double sum = 0.0;
        for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                throw new IllegalArgumentException(label + " names cannot be blank.");
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException(label + " probabilities cannot be null.");
            }
            validateProbability(entry.getValue(), label + " probability");
            sum += entry.getValue();
        }
        validateDistributionSum(sum, label);
    }

    private static void validateDistribution(double[] probabilities, String label) {
        if (probabilities == null || probabilities.length != 3) {
            throw new IllegalArgumentException(label + " must contain probabilities for 1★, 2★, and 3★.");
        }
        double sum = 0.0;
        for (double probability : probabilities) {
            validateProbability(probability, label + " probability");
            sum += probability;
        }
        validateDistributionSum(sum, label);
    }

    private static void validateDistributionSum(double sum, String label) {
        if (Math.abs(sum - 1.0) > PROBABILITY_TOLERANCE) {
            throw new IllegalArgumentException(label + " probabilities must sum to 1.");
        }
    }

    private static void validateProbability(double probability, String label) {
        if (!Double.isFinite(probability) || probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException(label + " must be between 0 and 1.");
        }
    }
}
