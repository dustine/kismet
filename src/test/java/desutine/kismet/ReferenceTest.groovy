package desutine.kismet

import desutine.kismet.client.gui.ModGuiFactory
import desutine.kismet.proxy.ClientProxy
import desutine.kismet.proxy.ServerProxy
import groovy.test.GroovyAssert
import org.junit.Test

class ReferenceTest extends GroovyAssert {
    @Test
    public void clientProxyReference() throws Exception {
        assertEquals(ClientProxy.canonicalName, Reference.CLIENT_PROXY_CLASS);
    }

    @Test
    public void serverProxyReference() throws Exception {
        assertEquals(ServerProxy.canonicalName, Reference.SERVER_PROXY_CLASS);
    }

    @Test
    public void guiFactoryReference() throws Exception {
        assertEquals(ModGuiFactory.canonicalName, Reference.GUI_FACTORY_CLASS);
    }
}
