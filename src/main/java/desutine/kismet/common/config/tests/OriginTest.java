package desutine.kismet.common.config.tests;

import desutine.kismet.common.config.InformedResourceLocation;

public abstract class OriginTest implements InformedResourceLocation.ITest {
    protected boolean passed = false;
    protected final String name;



    protected OriginTest(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean hasPassed() {
        return passed;
    }

    public abstract void test();
}
