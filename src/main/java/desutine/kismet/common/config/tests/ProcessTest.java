package desutine.kismet.common.config.tests;

import desutine.kismet.common.config.InformedResourceLocation;

import java.util.Set;

public abstract class ProcessTest implements InformedResourceLocation.ITest {
    protected boolean determined = false;
    protected boolean passed = false;
    protected Set<InformedResourceLocation> dependencies;
    private final String name;

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
    public abstract void test();
}
