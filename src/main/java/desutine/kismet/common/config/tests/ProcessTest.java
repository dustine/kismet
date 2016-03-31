package desutine.kismet.common.config.tests;

import desutine.kismet.common.config.InformedItemStack;
import net.minecraft.item.ItemStack;

import java.util.Set;

public abstract class ProcessTest implements InformedItemStack.ITest {
    private final String name;
    protected boolean determined = false;
    protected boolean passed = false;
    protected Set<InformedItemStack> dependencies;

    protected ProcessTest(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasPassed() {
        return determined && passed;
    }

    @Override
    public abstract void test(ItemStack item);
}
