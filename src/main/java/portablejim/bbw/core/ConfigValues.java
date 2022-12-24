package portablejim.bbw.core;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import net.minecraftforge.common.config.Configuration;

/**
 * Config file
 */
public class ConfigValues {
    private Configuration configFile;

    public int DIAMOND_WAND_DURABILITY;
    public static final int DIAMOND_WAND_DURABILITY_DEFAULT = 10000;
    public static final String DIAMOND_WAND_DURABILITY_NAME = "diamond_wand_durability";
    public static final String DIAMOND_WAND_DURABILITY_DESCRIPTION = "Durability of the Diamond Building Wand";
    public static final String CONFIG_WHYNOT = "why_not";
    public static final String CONFIG_GENERAL = "general";

    public boolean NO_EXTRA_UTILS_RECIPES;
    public static final boolean NO_EXTRA_UTILS_RECIPES_DEFAULT = false;
    public static final String NO_EXTRA_UTILS_RECIPES_NAME = "straymav_ultimate_wand";
    public static final String NO_EXTRA_UTILS_RECIPES_DESCRIPTION =
            "For those that don't like Extra Utils progression.";

    public boolean ENABLE_STONE_WAND;
    public static final boolean ENABLE_STONE_WAND_DEFAULT = true;
    public static final String ENABLE_STONE_WAND_NAME = "enable_stone_wand";
    public static final String ENABLE_STONE_WAND_DESCRIPTION = "Enable recipe for stone builder's wand";

    public boolean ENABLE_IRON_WAND;
    public static final boolean ENABLE_IRON_WAND_DEFAULT = true;
    public static final String ENABLE_IRON_WAND_NAME = "enable_iron_wand";
    public static final String ENABLE_IRON_WAND_DESCRIPTION = "Enable recipe for iron builder's wand";

    public boolean ENABLE_DIAMOND_WAND;
    public static final boolean ENABLE_DIAMOND_WAND_DEFAULT = true;
    public static final String ENABLE_DIAMOND_WAND_NAME = "enable_diamond_wand";
    public static final String ENABLE_DIAMOND_WAND_DESCRIPTION = "Enable recipe for diamond builder's wand";

    public String OVERRIDES_RECIPES;
    public static final String OVERRIDES_RECIPES_DEFAULT =
            "minecraft:lapis_ore/0=>1*minecraft:lapis_ore/4=>minecraft:lapis_ore/0,minecraft:lit_redstone_ore/0=>1*minecraft:redstone_ore/0=>minecraft:lit_redstone_ore/0";
    public static final String OVERRIDES_RECIPES_NAME = "forced_blocks";
    public static final String OVERRIDES_RECIPES_DESCRIPTION =
            "Specify forced mappings for what to build from certain blocks.\n(what you are looking at)=>(number required)*(item required)=>(block to build)";

    public String[] HARD_BLACKLIST;
    public Set<String> HARD_BLACKLIST_SET;
    public static final String[] HARD_BLACKLIST_DEFAULT = new String[] {};
    public static final String HARD_BLACKLIST_NAME = "blacklisted_blocks";
    public static final String HARD_BLACKLIST_DESCRIPTION = "Blocks that won't work at all with the wands";

    public String[] SOFT_BLACKLIST;
    public Set<String> SOFT_BLACKLIST_SET;
    public static final String[] SOFT_BLACKLIST_DEFAULT = new String[] {};
    public static final String SOFT_BLACKLIST_NAME = "no_assumption_blocks";
    public static final String SOFT_BLACKLIST_DESCRIPTION =
            "Blocks that break assumptions. When the placed block is not what you expect.";

    public ConfigValues(File file) {
        configFile = new Configuration(file);
    }

    public void loadConfigFile() {
        NO_EXTRA_UTILS_RECIPES = configFile.getBoolean(
                NO_EXTRA_UTILS_RECIPES_NAME,
                CONFIG_WHYNOT,
                NO_EXTRA_UTILS_RECIPES_DEFAULT,
                NO_EXTRA_UTILS_RECIPES_DESCRIPTION);
        OVERRIDES_RECIPES = configFile
                .get(CONFIG_GENERAL, OVERRIDES_RECIPES_NAME, OVERRIDES_RECIPES_DEFAULT, OVERRIDES_RECIPES_DESCRIPTION)
                .getString();
        ENABLE_STONE_WAND = configFile
                .get(CONFIG_GENERAL, ENABLE_STONE_WAND_NAME, ENABLE_STONE_WAND_DEFAULT, ENABLE_STONE_WAND_DESCRIPTION)
                .getBoolean();
        ENABLE_IRON_WAND = configFile
                .get(CONFIG_GENERAL, ENABLE_IRON_WAND_NAME, ENABLE_IRON_WAND_DEFAULT, ENABLE_IRON_WAND_DESCRIPTION)
                .getBoolean();
        ENABLE_DIAMOND_WAND = configFile
                .get(
                        CONFIG_GENERAL,
                        ENABLE_DIAMOND_WAND_NAME,
                        ENABLE_DIAMOND_WAND_DEFAULT,
                        ENABLE_DIAMOND_WAND_DESCRIPTION)
                .getBoolean();
        HARD_BLACKLIST = configFile
                .get(CONFIG_GENERAL, HARD_BLACKLIST_NAME, HARD_BLACKLIST_DEFAULT, HARD_BLACKLIST_DESCRIPTION)
                .getStringList();
        SOFT_BLACKLIST = configFile
                .get(CONFIG_GENERAL, SOFT_BLACKLIST_NAME, SOFT_BLACKLIST_DEFAULT, SOFT_BLACKLIST_DESCRIPTION)
                .getStringList();

        //noinspection unchecked
        HARD_BLACKLIST_SET = new TreeSet<String>(Arrays.asList(HARD_BLACKLIST));
        //noinspection unchecked
        SOFT_BLACKLIST_SET = new TreeSet<String>(Arrays.asList(SOFT_BLACKLIST));

        DIAMOND_WAND_DURABILITY = configFile
                .get(
                        DIAMOND_WAND_DURABILITY_NAME,
                        CONFIG_GENERAL,
                        DIAMOND_WAND_DURABILITY_DEFAULT,
                        DIAMOND_WAND_DURABILITY_DESCRIPTION)
                .getInt();

        configFile.save();
    }
}
