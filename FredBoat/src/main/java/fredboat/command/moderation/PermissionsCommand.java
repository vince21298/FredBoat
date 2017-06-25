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
 *
 */

package fredboat.command.moderation;

import fredboat.commandmeta.abs.Command;
import fredboat.commandmeta.abs.IModerationCommand;
import fredboat.db.EntityReader;
import fredboat.db.EntityWriter;
import fredboat.db.entity.GuildPermissions;
import fredboat.perms.PermissionLevel;
import fredboat.perms.PermsUtil;
import fredboat.util.ArgumentUtil;
import fredboat.util.constant.BotConstants;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

public class PermissionsCommand extends Command implements IModerationCommand {

    public final PermissionLevel permissionLevel;

    public PermissionsCommand(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    @Override
    public void onInvoke(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {
        if (args.length < 2) {
            channel.sendMessage(help(guild)).queue();
            return;
        }

        switch (args[1]) {
            case "del":
            case "remove":
            case "delete":
                if (!PermsUtil.checkPermsWithFeedback(PermissionLevel.ADMIN, invoker, channel)) return;

                if (args.length < 3) {
                    channel.sendMessage(help(guild)).queue();
                    return;
                }

                remove(guild, channel, invoker, message, args);
                break;
            case "add":
                if (!PermsUtil.checkPermsWithFeedback(PermissionLevel.ADMIN, invoker, channel)) return;

                if (args.length < 3) {
                    channel.sendMessage(help(guild)).queue();
                    return;
                }

                add(guild, channel, invoker, message, args);
                break;
            case "list":
            case "ls":
                list(guild, channel, invoker, message, args);
                break;
            default:
                channel.sendMessage(help(guild)).queue();
                break;
        }
    }

    public void remove(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {
        String term = ArgumentUtil.getSearchTerm(message, args, 2);

        List<IMentionable> list = new ArrayList<>();
        GuildPermissions gp = EntityReader.getGuildPermissions(guild);
        list.addAll(idsToMentionables(guild, gp.getFromEnum(permissionLevel)));

        IMentionable selected = ArgumentUtil.checkSingleFuzzySearchResult(list, channel, term);
        if (selected == null) return;

        List<String> newList = new ArrayList<>(gp.getFromEnum(permissionLevel));
        newList.remove(mentionableToId(selected));
        gp.setFromEnum(permissionLevel, newList);
        EntityWriter.mergeGuildPermissions(gp);
    }

    public void add(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {
        String term = ArgumentUtil.getSearchTerm(message, args, 2);

        List<IMentionable> list = new ArrayList<>();
        list.addAll(ArgumentUtil.fuzzyRoleSearch(guild, term));
        list.addAll(ArgumentUtil.fuzzyMemberSearch(guild, term));
        GuildPermissions gp = EntityReader.getGuildPermissions(guild);
        list.removeAll(idsToMentionables(guild, gp.getFromEnum(permissionLevel)));

        IMentionable selected = ArgumentUtil.checkSingleFuzzySearchResult(list, channel, term);
        if (selected == null) return;

        List<String> newList = new ArrayList<>(gp.getFromEnum(permissionLevel));
        newList.add(mentionableToId(selected));
        gp.setFromEnum(permissionLevel, newList);
        EntityWriter.mergeGuildPermissions(gp);
    }

    public void list(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {
        EmbedBuilder builder = new EmbedBuilder();
        GuildPermissions gp = EntityReader.getGuildPermissions(guild);

        List<IMentionable> mentionables = idsToMentionables(guild, gp.getFromEnum(permissionLevel));

        String roleMentions = "";
        String memberMentions = "";

        for (IMentionable mentionable : mentionables) {
            if (mentionable instanceof Role) {
                roleMentions = roleMentions + mentionable.getAsMention() + "\n";
            } else {
                memberMentions = memberMentions + mentionable.getAsMention() + "\n";
            }
        }

        boolean invokerHas = PermsUtil.checkPerms(permissionLevel, invoker);

        builder.setColor(BotConstants.FREDBOAT_COLOR)
                .setTitle("Users and roles with the " + permissionLevel + " permissions")
                .setAuthor(channel.getJDA().getSelfUser().getName(), null, channel.getJDA().getSelfUser().getAvatarUrl())
                .addField("Roles", roleMentions, true)
                .addField("Members", memberMentions, true)
                .addField(invoker.getEffectiveName(), invokerHas ? ":white_check_mark:" : ":x:", false);

        channel.sendMessage(builder.build()).queue();
    }

    private static String mentionableToId(IMentionable mentionable) {
        if (mentionable instanceof ISnowflake) {
            return ((ISnowflake) mentionable).getId();
        } else if (mentionable instanceof Member) {
            return ((Member) mentionable).getUser().getId();
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static List<IMentionable> idsToMentionables(Guild guild, List<String> list) {
        List<IMentionable> out = new ArrayList<>();

        for (String id : list) {
            if (id.equals("")) continue;

            if (guild.getRoleById(id) != null) {
                out.add(guild.getRoleById(id));
                continue;
            }

            if (guild.getMemberById(id) != null) {
                out.add(guild.getMemberById(id));
            }
        }

        return out;
    }

    @Override
    public String help(Guild guild) {
        return null;
    }

}
