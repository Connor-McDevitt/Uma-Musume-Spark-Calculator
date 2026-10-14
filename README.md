# Uma Musume Spark Probability Calculator

This project is the probability-engine foundation for estimating the chance of
producing a trained Uma with selected blue, pink, green, and white factors.
`Main` currently runs a small blue-factor example; the lineage-entry and
interactive calculator UI are not implemented yet. The engine currently
supports per-factor star intervals; aggregate minimum-count target evaluation
is not implemented yet.

## Current model

- **Blue:** Uses the final values of all five stats and the published
  conditional star rates: below 600 (90% 1★, 10% 2★), 600–1100 (50%, 45%,
  5%), and above 1100 (20%, 70%, 10%). The probability of selecting each stat
  is treated as uniform because the source describes the choice as random but
  does not publish its distribution.
- **Pink:** Requires user-supplied aptitude-selection and star probabilities.
  Numeric rates are not published in the cited guide.
- **Green:** Uses the documented rarity eligibility rule, but requires
  user-supplied star probabilities because universal numeric odds are not
  published.
- **White:** Uses published generation rates (normal skill, G1 race, and
  career factor: 20%; ◎ skill: 25%; gold skill: 40%). Star rates default to
  inputs supplied by the caller; the commonly cited 50% / 45% / 5% values
  apply to ordinary ranks, while SS+ adjustments are not specified there.
- **Multiple factors and repeat runs:** Combination probabilities assume
  independent factor rolls. The chance of at least one success in `n`
  independent runs is `1 - (1 - p)^n`; run-count estimates invert that formula
  for a selected confidence.

The calculator is conditional on the entered career outcome. A lineage does
not uniquely determine final stats, skills, race wins, or aptitude eligibility;
lineage effects and career results must not be conflated. The sources also do
not establish independence between factor rolls or repeated careers. Any
user-supplied odds should therefore be labeled as estimates in a future UI.

## References

- [GameTora: Legacy Guide](https://gametora.com/umamusume/legacies) — factor
  generation, blue stat thresholds, inheritance overview, and rank caveats.
- [Parenting / Gene Guide](https://docs.google.com/document/d/1Q3IJKbtkplmuY-PAJMNjYiLtasv0eU0aIBEqp8_C3tg/edit)
  — community-reported factor and compatibility details; not all mechanics
  are independently documented by the game.

## Running the current example

Compile with Java 17 or later:

```powershell
javac -d out src\SparkProbabilityCalculator.java src\Main.java
java -cp out Main
```

The lightweight tests can be run without adding a test framework:

```powershell
javac -d out src\SparkProbabilityCalculator.java test\SparkProbabilityCalculatorTest.java
java -cp out SparkProbabilityCalculatorTest
```
