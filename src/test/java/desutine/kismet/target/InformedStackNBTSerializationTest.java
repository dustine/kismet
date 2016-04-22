package desutine.kismet.target;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class InformedStackNBTSerializationTest {
    private final Set<InformedStack.ObtainableTypes> obtainable;

    public InformedStackNBTSerializationTest(Set<InformedStack.ObtainableTypes> obtainable) {
        this.obtainable = obtainable;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Set<InformedStack.ObtainableTypes>> data() {
        // generate case combinations
        final ArrayList<Set<InformedStack.ObtainableTypes>> possibilities = new ArrayList<>();
        int n = InformedStack.ObtainableTypes.values().length;
        for (long l = 0; l < Math.pow(2, n); l++) {
            final Set<InformedStack.ObtainableTypes> itCase = new HashSet<>();
            for (int o = 0; o < n; o++) {
                if (((l >> o) & 0b1) == 0b1) {
                    itCase.add(InformedStack.ObtainableTypes.values()[o]);
                }
            }
            possibilities.add(itCase);
        }
        return possibilities;
    }

    @Test
    public void test() {
        final InformedStack testCase = new InformedStack(new ItemStack(new Item()));
        testCase.setObtainable(obtainable);

        final InformedStack serialized = new InformedStack(testCase.serializeNBT());

        final Set<InformedStack.ObtainableTypes> serializedObtainable = serialized.getObtainable();
        assertThat("same length", serializedObtainable.size(), is(this.obtainable.size()));
        assertTrue("serialized c testcase", serializedObtainable.containsAll(this.obtainable));
        assertTrue("testcase c serialized", this.obtainable.containsAll(serializedObtainable));
    }
}
