package dustine.kismet.server.command;

import dustine.kismet.config.ConfigKismet;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class CCPardon extends CCSuperStackEntry {
    CCPardon(String parent) {
        super(parent, EnumCommandType.PARDON);
    }

    @Override public String getCommandName() {
        return "pardon";
    }

    @Override protected void processEntry(ICommandSender sender, String entry) {
        staticProcessEntry(sender, entry);
    }

    public static void staticProcessEntry(ICommandSender sender, String entry) {
        if (!ConfigKismet.removeFromGenFilter(entry)) {
            CommandKismet.sendError(sender,
                    new TextComponentString("Entry not present in gen target filter")
            );
            return;
        }
        TargetLibrary.build(WSDTargetDatabase.get(sender.getEntityWorld()));
        CommandKismet.send(sender,
                new TextComponentString(String.format("Removed %s to gen target filter", entry))
        );
    }
}
