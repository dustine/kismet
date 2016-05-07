package dustine.kismet.server.command;

import dustine.kismet.config.ConfigKismet;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class CCUnforce extends CCSuperStackEntry {
    CCUnforce(String parent) {
        super(parent, EnumCommandType.UNFORCE);
    }

    @Override protected void processEntry(ICommandSender sender, String entry) {
        staticProcessEntry(sender, entry);
    }

    public static void staticProcessEntry(ICommandSender sender, String entry) {
        if (!ConfigKismet.removeFromForceAdd(entry)) {
            CommandKismet.sendError(sender,
                    new TextComponentString("Entry not present in forced target list")
            );
            return;
        }
        TargetLibrary.build(WSDTargetDatabase.get(sender.getEntityWorld()));
        CommandKismet.send(sender,
                new TextComponentString(String.format("Removed %s to forced target list", entry))
        );
    }

    @Override public String getCommandName() {
        return "force";
    }
}
