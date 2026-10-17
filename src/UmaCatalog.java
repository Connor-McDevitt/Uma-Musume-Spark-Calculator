import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Global characters available through October 17, 2026; costume variants share one picker entry.
final class UmaCatalog {
    private static final String[] CHARACTERS = {
            "admire-vega|103301",
            "agnes-digital|101901",
            "agnes-tachyon|103201",
            "air-groove|101801",
            "air-shakur|103601",
            "aston-machan|108701",
            "bamboo-memory|105301",
            "biwa-hayahide|102301",
            "copano-rickey|109801",
            "curren-chan|103801",
            "daiwa-scarlet|100901",
            "eishin-flash|103701",
            "el-condor-pasa|101401",
            "fine-motion|102201",
            "fuji-kiseki|100501",
            "gold-city|104001",
            "gold-ship|100701",
            "grass-wonder|101101",
            "haru-urara|105201",
            "hishi-akebono|102801",
            "hishi-amazon|101201",
            "inari-one|103401",
            "ines-fujin|103101",
            "kawakami-princess|103901",
            "king-halo|106101",
            "kitasan-black|106801",
            "manhattan-cafe|102501",
            "maruzensky|100401",
            "matikanefukukitaru|105601",
            "matikanetannhauser|106201",
            "mayano-top-gun|102401",
            "meisho-doto|105801",
            "mejiro-ardan|107101",
            "mejiro-bright|107401",
            "mejiro-dober|105901",
            "mejiro-mcqueen|101301",
            "mejiro-palmer|106401",
            "mejiro-ryan|102701",
            "mihono-bourbon|102601",
            "nakayama-festa|104901",
            "narita-brian|101601",
            "narita-taishin|105001",
            "nice-nature|106001",
            "nishino-flower|105101",
            "oguri-cap|100601",
            "rice-shower|103001",
            "sakura-bakushin-o|104101",
            "sakura-chiyono-o|106901",
            "satono-diamond|106701",
            "seeking-the-pearl|104201",
            "seiun-sky|102001",
            "silence-suzuka|100201",
            "smart-falcon|104601",
            "special-week|100101",
            "super-creek|104501",
            "sweep-tosho|104401",
            "symboli-rudolf|101701",
            "taiki-shuttle|101001",
            "tamamo-cross|102101",
            "tm-opera-o|101501",
            "tokai-teio|100301",
            "tosen-jordan|104801",
            "vodka|100801",
            "winning-ticket|103501",
            "wonder-acute|110001",
            "yaeno-muteki|107201",
            "yamanin-zephyr|107801",
            "yukino-bijin|102901",
            "zenno-rob-roy|104701"
    };

    private UmaCatalog() {
    }

    static List<String> entries() {
        return Collections.unmodifiableList(Arrays.asList(CHARACTERS.clone()));
    }
}
