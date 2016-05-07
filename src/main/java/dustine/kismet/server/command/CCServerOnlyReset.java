package dustine.kismet.server.command;

import dustine.kismet.Kismet;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.Collections;
import java.util.List;

public class CCServerOnlyReset extends CommandComponent {
    public CCServerOnlyReset(final String parent) {
        super(parent);
    }

    @Override public String getCommandName() {
        return "serverOnlyReset";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender,
                        final String[] args) throws CommandException {
        if (Kismet.databaseBuilder != null) {
            CommandKismet.send(sender, new TextComponentString("Starting §cserver only§r database reset..."));
            Kismet.databaseBuilder.buildServerSide((EntityPlayerMP) sender);
            CommandKismet.send(sender, new TextComponentString("Finished! Refreshing target library now..."));
            TargetLibrary.build(WSDTargetDatabase.get(server.getEntityWorld()));
            CommandKismet.send(sender, new TextComponentString("Done! Database reset finished."));
        } else {
            CommandKismet.error("Target database factory not found");
        }
    }

    @Override public List<String> getTabCompletionOptions(final MinecraftServer server, final ICommandSender sender,
                                                          final String[] args,
                                                          final BlockPos pos) {
        return Collections.emptyList();
    }
}
