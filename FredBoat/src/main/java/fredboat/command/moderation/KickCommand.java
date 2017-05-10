/*
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fredboat.command.moderation;

import fredboat.Config;
import fredboat.command.util.HelpCommand;
import fredboat.commandmeta.abs.Command;
import fredboat.commandmeta.abs.IModerationCommand;
import fredboat.feature.I18n;
import fredboat.util.ArgumentUtil;
import fredboat.util.DiscordUtil;
import fredboat.util.TextUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Created by napster on 19.04.17.
 * <p>
 * Kick a user.
 */
//Basically copy pasta of the softban command. If you change something here make sure to check the related
// moderation commands too.
public class KickCommand extends Command implements IModerationCommand {

    private static final Logger log = LoggerFactory.getLogger(KickCommand.class);

    @Override
    public void onInvoke(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {
        //Ensure we have a search term
        if (args.length == 1) {
            String command = args[0].substring(Config.CONFIG.getPrefix().length());
            HelpCommand.sendFormattedCommandHelp(guild, channel, invoker, command);
            return;
        }

        Member target = ArgumentUtil.checkSingleFuzzySearchResult(channel, args[1]);

        if (target == null) return;

        if (!checkKickAuthorization(channel, invoker, target)) return;

        target.getGuild().getController().kick(target).queue(
                aVoid -> {
                    TextUtils.replyWithName(channel, invoker, MessageFormat.format(I18n.get(guild).getString("kickSuccess"), target.getUser().getName(), target.getUser().getDiscriminator(), target.getUser().getId()));
                },
                throwable -> log.error(MessageFormat.format(I18n.get(guild).getString("kickFail"), target.getUser()))
        );
    }

    private boolean checkKickAuthorization(TextChannel channel, Member mod, Member target) {
        if (mod == target) {
            TextUtils.replyWithName(channel, mod, I18n.get(channel.getGuild()).getString("kickFailSelf"));
            return false;
        }

        if (target.isOwner()) {
            TextUtils.replyWithName(channel, mod, I18n.get(channel.getGuild()).getString("kickFailOwner"));
            return false;
        }

        if (target == target.getGuild().getSelfMember()) {
            TextUtils.replyWithName(channel, mod, I18n.get(channel.getGuild()).getString("kickFailMyself"));
            return false;
        }

        if (!mod.hasPermission(Permission.KICK_MEMBERS) && !mod.isOwner()) {
            TextUtils.replyWithName(channel, mod, I18n.get(channel.getGuild()).getString("kickFailUserPerms"));
            return false;
        }

        if (DiscordUtil.getHighestRolePosition(mod) <= DiscordUtil.getHighestRolePosition(target) && !mod.isOwner()) {
            TextUtils.replyWithName(channel, mod, MessageFormat.format(I18n.get(channel.getGuild()).getString("modFailUserHierarchy"), target.getEffectiveName()));
            return false;
        }

        if (!mod.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
            TextUtils.replyWithName(channel, mod, I18n.get(channel.getGuild()).getString("modKickBotPerms"));
            return false;
        }

        if (DiscordUtil.getHighestRolePosition(mod.getGuild().getSelfMember()) <= DiscordUtil.getHighestRolePosition(target)) {
            TextUtils.replyWithName(channel, mod, MessageFormat.format(I18n.get(channel.getGuild()).getString("modFailBotHierarchy"), target.getEffectiveName()));
            return false;
        }

        return true;
    }

    @Override
    public String help(Guild guild) {
        String usage = "{0}{1} <user>\n#";
        return usage + I18n.get(guild).getString("helpKickCommand");
    }
}
