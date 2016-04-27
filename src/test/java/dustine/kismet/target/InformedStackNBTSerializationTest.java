package dustine.kismet.target;

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
    private final Set<InformedStack.EnumOrigin> obtainable;

    public InformedStackNBTSerializationTest(Set<InformedStack.EnumOrigin> obtainable) {
        this.obtainable = obtainable;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Set<InformedStack.EnumOrigin>> data() {
        // generate case combinations
        final ArrayList<Set<InformedStack.EnumOrigin>> possibilities = new ArrayList<>();
        int n = InformedStack.EnumOrigin.values().length;
        for (long l = 0; l < Math.pow(2, n); l++) {
            final Set<InformedStack.EnumOrigin> itCase = new HashSet<>();
            for (int o = 0; o < n; o++) {
                if (((l >> o) & 0b1) == 0b1) {
                    itCase.add(InformedStack.EnumOrigin.values()[o]);
                }
            }
            possibilities.add(itCase);
        }
        return possibilities;
    }

    @Test
    public void test() {
        final InformedStack testCase = new InformedStack(new ItemStack(new Item()));
        testCase.setOrigins(obtainable);

        final InformedStack serialized = new InformedStack(testCase.serializeNBT());

        final Set<InformedStack.EnumOrigin> serializedObtainable = serialized.getOrigins();
        assertThat("same length", serializedObtainable.size(), is(this.obtainable.size()));
        assertTrue("serialized c testcase", serializedObtainable.containsAll(this.obtainable));
        assertTrue("testcase c serialized", this.obtainable.containsAll(serializedObtainable));
    }
}
