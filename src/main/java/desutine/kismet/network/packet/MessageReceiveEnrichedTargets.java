package desutine.kismet.network.packet;

import desutine.kismet.server.TargetsWorldSavedData;

import java.util.List;

public class MessageReceiveEnrichedTargets extends MessageTargets {
    public MessageReceiveEnrichedTargets() {
        super();
    }

    public MessageReceiveEnrichedTargets(List<TargetsWorldSavedData.WrapperTarget> items) {
        super(items);
    }
}
