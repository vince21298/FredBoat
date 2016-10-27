package fredboat.commandmeta;

import fredboat.commandmeta.abs.Command;
import fredboat.commandmeta.abs.ICommand;
import fredboat.commandmeta.abs.ICommandOwnerRestricted;
import fredboat.commandmeta.abs.IMusicBackupCommand;
import fredboat.commandmeta.abs.IMusicCommand;
import fredboat.util.BotConstants;
import fredboat.util.DiscordUtil;
import fredboat.util.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.PermissionUtil;
import org.slf4j.LoggerFactory;

public class CommandManager {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CommandManager.class);
    
    public static HashMap<String, ICommand> commands = new HashMap<>();
    public static ICommand defaultCmd = new UnknownCommand();
    public static int commandsExecuted = 0;

    public static void prefixCalled(Command invoked, Guild guild, TextChannel channel, User invoker, Message message) {
        //String[] args = message.getRawContent().replace("\n", " ").split(" ");
        String[] args = commandToArguments(message.getRawContent());
        commandsExecuted++;
        if (invoked instanceof ICommandOwnerRestricted) {
            //This command is restricted to only Frederikam
            //Check if invoker is actually Frederikam
            if (!invoker.getId().equals(BotConstants.OWNER_ID)) {
                channel.sendMessage(TextUtils.prefaceWithMention(invoker, " you are not allowed to use that command!"));
                return;
            }
        }

        if (invoked instanceof IMusicBackupCommand && DiscordUtil.isMusicBot() && DiscordUtil.isMainBotPresent(guild)) {
            log.info("Ignored command because main bot is present");
            return;
        }

        if (invoked instanceof IMusicCommand
                && PermissionUtil.checkPermission(channel, guild.getJDA().getSelfInfo(), Permission.MESSAGE_WRITE) == false) {
            log.info("Ignored command because it was a music command, and this bot cannot write in that channel");
            return;
        }
        
        //Hardcode music commands in FredBoatHangout. Blacklist any channel that isn't #general or #staff, but whitelist Frederikam
        if(invoked instanceof IMusicCommand && guild.getId().equals("174820236481134592")){
            if(!channel.getId().equals("174821093633294338")
                    && !channel.getId().equals("217526705298866177")
                    && !invoker.getId().equals("81011298891993088")){
                message.deleteMessage();
                channel.sendMessage("Please don't spam music commands outside of <#174821093633294338>.");
                return;
            }
        }

        try {
            invoked.onInvoke(guild, channel, invoker, message, args);
        } catch (Exception e) {
            TextUtils.handleException(e, channel, invoker);
        }

    }

    public static String[] commandToArguments(String cmd) {
        ArrayList<String> a = new ArrayList<>();
        int argi = 0;
        boolean isInQuote = false;

        for (Character ch : cmd.toCharArray()) {
            if (Character.isWhitespace(ch) && isInQuote == false) {
                String arg = null;
                try {
                    arg = a.get(argi);
                } catch (IndexOutOfBoundsException e) {
                }
                if (arg != null) {
                    argi++;//On to the next arg
                }//else ignore

            } else if (ch.equals('"')) {
                isInQuote = !isInQuote;
            } else {
                a = writeToArg(a, argi, ch);
            }
        }

        String[] newA = new String[a.size()];
        int i = 0;
        for (String str : a) {
            newA[i] = str;
            i++;
        }

        return newA;
    }

    private static ArrayList<String> writeToArg(ArrayList<String> a, int argi, char ch) {
        String arg = null;
        try {
            arg = a.get(argi);
        } catch (IndexOutOfBoundsException e) {
        }
        if (arg == null) {
            a.add(argi, String.valueOf(ch));
        } else {
            a.set(argi, arg + ch);
        }

        return a;
    }
}
