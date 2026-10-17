# Uma Musume Spark Probability Calculator

This project estimates the chance of producing a trained Uma with selected
spark factors. `Main` opens a desktop calculator with an interactive Uma
lineage picker and a blue-factor probability panel. The lineage tree is for
planning and display; it does not infer career stats or factor odds from
parents.

The picker includes 69 English Uma available in the Global game through
October 17, 2026; costume variants use a single character entry. The catalog
and any available portraits are loaded locally, and the application makes no
network requests. Character artwork is not bundled; the picker uses its
monogram fallback until you add portrait JPGs you have permission to use.

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

The current UI calculates blue-factor odds from the entered final stats,
selected target stats, and star range. It reports the estimated number of
independent careers needed to reach the selected confidence. Pink, green, and
white factor probability controls are not yet exposed in the UI.

## References

- [GameTora: Legacy Guide](https://gametora.com/umamusume/legacies) — factor
  generation, blue stat thresholds, inheritance overview, and rank caveats.
- [Uma Guide: October 2026 Release Schedule](https://uma.guide/guides/release-schedule-october26)
  — Global banner schedule used to set the roster cutoff.
- [Parenting / Gene Guide](https://docs.google.com/document/d/1Q3IJKbtkplmuY-PAJMNjYiLtasv0eU0aIBEqp8_C3tg/edit)
  — community-reported factor and compatibility details; not all mechanics
  are independently documented by the game.

## Running the calculator

Compile with Java 17 or later and launch the desktop UI:

```powershell
javac -d out src\SparkProbabilityCalculator.java src\UmaCatalog.java src\SparkCalculatorWindow.java src\Main.java
java -cp out Main
```

The character catalog and monogram fallback are bundled with the application.
Optional local portrait files should be named `assets\portraits\<slug>.jpg`
(for example, `assets\portraits\special-week.jpg`). For packaged builds, put
the same files in the classpath's `portraits` folder. Portraits are never
downloaded by the app; add only images you have permission to use.

The lightweight tests can be run without adding a test framework:

```powershell
javac -d out src\SparkProbabilityCalculator.java src\UmaCatalog.java test\SparkProbabilityCalculatorTest.java
java -cp out SparkProbabilityCalculatorTest
```
