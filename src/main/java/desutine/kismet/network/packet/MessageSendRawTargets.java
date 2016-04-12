package desutine.kismet.network.packet;

import desutine.kismet.server.StackWrapper;

import java.util.List;

public class MessageSendRawTargets extends MessageTargets {
    public MessageSendRawTargets() {
        super();
    }

    public MessageSendRawTargets(List<StackWrapper> stacks) {
        super(stacks);
    }
}
