package dustine.kismet.registry;

import dustine.kismet.config.ConfigKismet;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ModRecipes {
    public static void init() {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.KEY, 1), "g  ", "egg", "g n",
                'g', "ingotGold",
                'e', "gemEmerald",
                'n', "nuggetGold"));

        if (ConfigKismet.isChillEnabled()) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.CHILL_DISPLAY), "eqd", "rir", "sLs",
                    'e', "gemEmerald",
                    'q', "gemQuartz",
                    'd', "gemDiamond",
                    'r', "dustRedstone",
                    'i', new ItemStack(Items.ITEM_FRAME),
                    's', new ItemStack(Blocks.STONE_SLAB),
                    'L', "blockLapis"
            ));
        }

        if (ConfigKismet.isTimedEnabled()) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.TIMED_DISPLAY), "eqd", "lil", "sRs",
                    'e', "gemEmerald",
                    'q', "gemQuartz",
                    'd', "gemDiamond",
                    'l', "gemLapis",
                    'i', new ItemStack(Items.ITEM_FRAME),
                    's', new ItemStack(Blocks.STONE_SLAB),
                    'R', "blockRedstone"
            ));
        }
    }


}
