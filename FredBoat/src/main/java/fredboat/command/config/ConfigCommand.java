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

package fredboat.command.config;

import fredboat.Config;
import fredboat.command.util.HelpCommand;
import fredboat.commandmeta.MessagingException;
import fredboat.commandmeta.abs.Command;
import fredboat.commandmeta.abs.IModerationCommand;
import fredboat.db.EntityReader;
import fredboat.db.EntityWriter;
import fredboat.db.entity.GuildConfig;
import fredboat.event.ReactionListener;
import fredboat.feature.I18n;
import fredboat.util.BotConstants;
import fredboat.util.DiscordUtil;
import fredboat.util.Emojis;
import fredboat.util.TextUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigCommand extends Command implements IModerationCommand {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {
        if(args.length == 1) {
            // we are about to send an embed, but can we do that?
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
                throw new MessagingException(I18n.get(channel.getGuild()).getString("permissionMissingBot") + " "
                        + I18n.get(channel.getGuild()).getString("permissionEmbedLinks"));
            }
            printConfig(guild, channel, invoker, message, args);
        } else {
            setConfig(guild, channel, invoker, message, args);
        }
    }

    private void printConfig(Guild guild, TextChannel channel, Member invoker, Message commandMessage, String[] args) {
        GuildConfig gc = EntityReader.getGuildConfig(guild.getId());

        Consumer<Void> persistGC = aVoid -> EntityWriter.mergeGuildConfig(gc);
        //the strings have to be unicode representations of emojis so that we can use them as reactions
        Map<String, OptionBoolean> options = new LinkedHashMap<>();//using linked to keep order of elements
        options.put(Emojis.NUMBER_0, new OptionBoolean(gc::isTrackAnnounce, gc::setTrackAnnounce, "Announce tracks", persistGC));
        options.put(Emojis.NUMBER_1, new OptionBoolean(gc::isAutoResume, gc::setAutoResume, "Auto resume", persistGC));

        channel.sendMessage(prepareGuildConfigEmbed(guild, options).build()).queue(m -> {
            options.keySet().forEach(emoji -> m.addReaction(emoji).queue());
            guild.getJDA().addEventListener(new ReactionListener(m,
                    this::hasGuildConfigPerms,
                    //on reaction
                    reaction -> {
                        OptionBoolean op = options.get(reaction.getEmote().getName());
                        if (op == null) return;
                        op.toggle();
                        m.editMessage(prepareGuildConfigEmbed(guild, options).build()).queue();
                    },
                    1000 * 60 * 2, //2 minutes of inactivity
                    //on destruct
                    aVoid -> {
                        m.delete().queue();
                        try {
                            commandMessage.delete().queue();
                        } catch (Exception ignored) {
                        }
                    }));
        });
    }

    private boolean hasGuildConfigPerms(Member member) {
        if (member.getUser().isBot()) return false; //no bots allowed
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
        if (DiscordUtil.isUserBotOwner(member.getUser())) return true;

        return false;
    }

    private EmbedBuilder prepareGuildConfigEmbed(Guild guild, Map<String, OptionBoolean> options) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Server settings for " + guild.getName());
        eb.setColor(BotConstants.FREDBOAT_COLOR);

        final StringBuilder optionField = new StringBuilder();
        options.forEach((emoji, optionBoolean) ->
                optionField.append(emoji).append(" ").append(optionBoolean.text).append(": ")
                        .append(optionBoolean.getter.get() ? Emojis.CHECK : Emojis.CROSS).append("\n")
        );
        eb.addField("", optionField.toString(), true);
        eb.addField("", "Click the reactions below to toggle the options.", false);
        eb.addField("", "Reactions by non-admins of this server will be ignored.", false);
        return eb;
    }

    /**
     * Describes a boolean config option
     */
    private class OptionBoolean {
        private Supplier<Boolean> getter;
        private Consumer<Boolean> setter;
        private String text;
        private Consumer<Void> save;

        public OptionBoolean(Supplier<Boolean> getter, Consumer<Boolean> setter, String text, Consumer<Void> save) {
            this.getter = getter;
            this.setter = setter;
            this.text = text;
            this.save = save;
        }

        public void toggle() {
            setter.accept(!getter.get());
            save.accept(null);
        }
    }

    private void setConfig(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {
        if (!hasGuildConfigPerms(invoker)) {
            channel.sendMessage(MessageFormat.format(I18n.get(guild).getString("configNotAdmin"), invoker.getEffectiveName())).queue();
            return;
        }

        if(args.length != 3) {
            String command = args[0].substring(Config.CONFIG.getPrefix().length());
            HelpCommand.sendFormattedCommandHelp(guild, channel, invoker, command);
            return;
        }

        GuildConfig gc = EntityReader.getGuildConfig(guild.getId());
        String key = args[1];
        String val = args[2];

        switch (key) {
            case "track_announce":
                if (val.equalsIgnoreCase("true") | val.equalsIgnoreCase("false")) {
                    gc.setTrackAnnounce(Boolean.valueOf(val));
                    EntityWriter.mergeGuildConfig(gc);
                    TextUtils.replyWithName(channel, invoker, "`track_announce` " + MessageFormat.format(I18n.get(guild).getString("configSetTo"), val));
                } else {
                    channel.sendMessage(MessageFormat.format(I18n.get(guild).getString("configMustBeBoolean"), invoker.getEffectiveName())).queue();
                }
                break;
            case "auto_resume":
                if (val.equalsIgnoreCase("true") | val.equalsIgnoreCase("false")) {
                    gc.setAutoResume(Boolean.valueOf(val));
                    EntityWriter.mergeGuildConfig(gc);
                    TextUtils.replyWithName(channel, invoker, "`auto_resume` " + MessageFormat.format(I18n.get(guild).getString("configSetTo"), val));
                } else {
                    channel.sendMessage(MessageFormat.format(I18n.get(guild).getString("configMustBeBoolean"), invoker.getEffectiveName())).queue();
                }
                break;
            default:
                channel.sendMessage(MessageFormat.format(I18n.get(guild).getString("configUnknownKey"), invoker.getEffectiveName())).queue();
                break;
        }
    }

    @Override
    public String help(Guild guild) {
        String usage = "{0}{1} OR {0}{1} <key> <value>\n#";
        return usage + I18n.get(guild).getString("helpConfigCommand");
    }
}
