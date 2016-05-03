package dustine.kismet.server.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public abstract class CommandComponent extends CommandBase {
    private final String parent;

    CommandComponent(String parent) {
        this.parent = parent;
    }

    @Override public String getCommandUsage(ICommandSender sender) {
        return String.format("commands.%s.%s.usage", getParentName(), getCommandName());
    }

    String getParentName() {
        return this.parent;
    }
}
