package desutine.kismet.common.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ModRecipes {
    public static void init() {
//            .           | golden pressure plate    | .
//            !gemDiamond | smooth double stone slab | !gemEmerald
//            item frame  | !blockLapis              | clock
//
//            = Timed Kismet Display
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.DISPLAY), " i ", "dse", "flc",
                'i', new ItemStack(Blocks.heavy_weighted_pressure_plate),
                'd', "gemDiamond",
                's', new ItemStack(Blocks.stone_slab),
                'e', "gemEmerald",
                'f', new ItemStack(Items.item_frame),
                'l', "blockLapis",
                'c', new ItemStack(Items.clock)
        ));


//            same as above but diamond and emerald are flipped
//            so you don't need to remember if diamond and emerald are on the left or right
//            same for the item frame and clock because minecraft does mirrored recipes automatically~

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.DISPLAY), " i ", "esd", "flc",
                'i', new ItemStack(Blocks.heavy_weighted_pressure_plate),
                'e', "gemEmerald",
                's', new ItemStack(Blocks.stone_slab),
                'd', "gemDiamond",
                'f', new ItemStack(Items.item_frame),
                'l', "blockLapis",
                'c', new ItemStack(Items.clock)
        ));
    }



}
