package desutine.kismet

import desutine.kismet.client.gui.ModGuiFactory
import desutine.kismet.proxy.ClientProxy
import desutine.kismet.proxy.ServerProxy
import spock.lang.Specification

class ReferenceSpec extends Specification {
    def "class references are correct"() {
        expect:
        ClientProxy.canonicalName == Reference.CLIENT_PROXY_CLASS
        ServerProxy.canonicalName == Reference.SERVER_PROXY_CLASS
        ModGuiFactory.canonicalName == Reference.GUI_FACTORY_CLASS
    }
}
