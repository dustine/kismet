package dustine.kismet.server.command;

import dustine.kismet.config.ConfigKismet;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class CCBlock extends CCSuperStackEntry {
    public CCBlock(String parent) {
        super(parent, EnumCommandType.BLOCK);
    }

    @Override public String getCommandName() {
        return "block";
    }

    @Override protected void processEntry(ICommandSender sender, String entry) {
        staticProcessEntry(sender, entry);
    }

    public static void staticProcessEntry(ICommandSender sender, String entry) {
        if (!ConfigKismet.addToGenFilter(entry)) {
            CommandKismet.sendError(sender,
                    new TextComponentString("Entry already present in gen target filter")
            );
            return;
        }
        TargetLibrary.build(WSDTargetDatabase.get(sender.getEntityWorld()));
        CommandKismet.send(sender,
                new TextComponentString(String.format("Added %s to gen target filter", entry))
        );
    }
}
