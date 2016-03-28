package desutine.kismet.common.config;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import desutine.kismet.common.config.tests.OriginTest;
import desutine.kismet.common.config.tests.ProcessTest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class InformedResourceLocation {
    public ResourceLocation location;
    public ItemStack item;
    // fixme swapped for debug reasons
    public boolean crafted = true;
    public boolean generated = false;
    public boolean looted = false;
    public boolean silkTouched = false;
    public boolean harvested = false;
    public boolean cooked = false;
    public boolean brewed = false;
    public static Set<OriginTest> originTests = new HashSet<>();
    public static Set<ProcessTest> processTests = new HashSet<>();

    public interface ITest {
        String getName();
        boolean hasPassed();
        void test();
    }

//        Blocks.kismetDisplayBlock.getHarvestLevel();
//        Blocks.kismetDisplayBlock.getBlockState().getValidStates()

    public boolean isObtainable() {
        return Iterables.any(originTests, new Predicate<OriginTest>() {
            @Override
            public boolean apply(@Nullable OriginTest input) {
                return input != null && input.hasPassed();
            }
        }) || Iterables.any(processTests, new Predicate<ProcessTest>() {
            @Override
            public boolean apply(@Nullable ProcessTest input) {
                return input != null && input.hasPassed();
            }
        });

//            BiomeGenBase.biomeRegistry.getObject()
    }

    public InformedResourceLocation(ResourceLocation location) {
        this.location = location;
    }
    public InformedResourceLocation(String location) {
        this.location = new ResourceLocation(location);
    }
}
