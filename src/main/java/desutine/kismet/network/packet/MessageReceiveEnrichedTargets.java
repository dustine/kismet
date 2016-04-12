package desutine.kismet.network.packet;

import desutine.kismet.server.StackWrapper;

import java.util.List;

public class MessageReceiveEnrichedTargets extends MessageTargets {
    public MessageReceiveEnrichedTargets() {
        super();
    }

    public MessageReceiveEnrichedTargets(List<StackWrapper> stacks) {
        super(stacks);
    }
}
