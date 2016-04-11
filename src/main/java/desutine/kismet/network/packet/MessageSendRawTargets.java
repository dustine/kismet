package desutine.kismet.network.packet;

import desutine.kismet.server.TargetsWorldSavedData;

import java.util.List;

public class MessageSendRawTargets extends MessageTargets {
    public MessageSendRawTargets() {
        super();
    }

    public MessageSendRawTargets(List<TargetsWorldSavedData.WrapperTarget> stacks) {
        super(stacks);
    }
}
