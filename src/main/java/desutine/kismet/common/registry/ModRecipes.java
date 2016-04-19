package desutine.kismet.common.registry;

import desutine.kismet.common.ConfigKismet;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ModRecipes {
    public static void init() {
        if (ConfigKismet.isChillEnabled()) {
//            .          | :gemEmerald | .
//            .          | stone slab  | .
//            item frame | :blockLapis | comparator
//
//            = Chill Kismet Display
            // todo create the chill display
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.DISPLAY), " e ", " s ", "fLc",
                    's', new ItemStack(Blocks.stone_slab),
                    'e', "gemEmerald",
                    'f', new ItemStack(Items.item_frame),
                    'L', "blockLapis",
                    'c', new ItemStack(Items.comparator)
            ));
        }

        if (ConfigKismet.isTimedEnabled()) {
//            .          | :gemEmerald | .
//            .          | stone slab  | .
//            item frame | :blockLapis | clock
//
//            = Timed Kismet Display
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.DISPLAY), " e ", " s ", "fLc",
                    's', new ItemStack(Blocks.stone_slab),
                    'e', "gemEmerald",
                    'f', new ItemStack(Items.item_frame),
                    'L', "blockLapis",
                    'c', new ItemStack(Items.clock)
            ));
        }
    }


}
