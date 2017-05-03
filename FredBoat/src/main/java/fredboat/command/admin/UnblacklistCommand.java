package fredboat.command.admin;

import fredboat.Config;
import fredboat.command.util.HelpCommand;
import fredboat.commandmeta.abs.Command;
import fredboat.commandmeta.abs.ICommandAdminRestricted;
import fredboat.feature.I18n;
import fredboat.util.TextUtils;
import fredboat.util.ratelimit.Ratelimiter;
import net.dv8tion.jda.core.entities.*;

import java.text.MessageFormat;

/**
 * Created by napster on 17.04.17.
 * <p>
 * Lift ratelimit and remove a user from the blacklist
 */
public class UnblacklistCommand extends Command implements ICommandAdminRestricted {
    @Override
    public void onInvoke(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {

        String command = args[0].substring(Config.CONFIG.getPrefix().length());
        if (message.getMentionedUsers().isEmpty()) {
            HelpCommand.sendFormattedCommandHelp(guild, channel, invoker, command);
            return;
        }

        User user = message.getMentionedUsers().get(0);
        String userId = user.getId();

        if (userId == null || "".equals(userId)) {
            HelpCommand.sendFormattedCommandHelp(guild, channel, invoker, command);
            return;
        }

        Ratelimiter.getRatelimiter().liftLimitAndBlacklist(user.getIdLong());
        channel.sendMessage(TextUtils.replyWithName(channel, invoker,
                MessageFormat.format(I18n.get(guild).getString("unblacklisted"), user.getAsMention())));
    }

    @Override
    public String help(Guild guild) {
        return "{0}{1} @<user>\n#Remove a user from the blacklist.";
    }
}
