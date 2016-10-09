/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fredboat.event;

import static fredboat.FredBoat.jdaBot;
import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.command.fun.TalkCommand;
import fredboat.commandmeta.CommandManager;
import fredboat.commandmeta.CommandRegistry;
import fredboat.commandmeta.abs.Command;
import fredboat.util.BotConstants;
import java.util.HashMap;
import java.util.regex.Matcher;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.InviteReceivedEvent;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.ReconnectedEvent;
import net.dv8tion.jda.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.events.message.MessageDeleteEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.events.voice.VoiceLeaveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListenerBoat extends AbstractScopedEventListener {

    private static final Logger log = LoggerFactory.getLogger(EventListenerBoat.class);

    public static HashMap<String, Message> messagesToDeleteIfIdDeleted = new HashMap<>();
    public User lastUserToReceiveHelp;

    public static int messagesReceived = 0;

    public EventListenerBoat(int scope, String defaultPrefix) {
        super(scope, defaultPrefix);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        /*log.info(event.getJDA().getSelfInfo().getUsername());
        log.info(event);
        log.info(event.getAuthor());
        log.info(event.getAuthor().getId());*/

        messagesReceived++;

        if (event.getPrivateChannel() != null) {
            log.info("PRIVATE" + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
            return;
        }

        if (event.getAuthor().getUsername().equals(event.getJDA().getSelfInfo().getUsername())) {
            log.info(event.getGuild().getName() + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
            return;
        }

        if (event.getMessage().getContent().length() < defaultPrefix.length()) {
            return;
        }

        if (event.getMessage().getContent().substring(0, defaultPrefix.length()).equals(defaultPrefix)) {
            Command invoked = null;
            try {
                log.info(event.getGuild().getName() + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
                Matcher matcher = commandNamePrefix.matcher(event.getMessage().getContent());
                matcher.find();

                invoked = CommandRegistry.getCommandFromScope(scope, matcher.group()).command;
            } catch (NullPointerException ex) {

            }

            if (invoked == null) {
                return;
            }

            CommandManager.prefixCalled(invoked, event.getGuild(), event.getTextChannel(), event.getAuthor(), event.getMessage());
        } else if (event.getMessage().getRawContent().startsWith("<@" + jdaBot.getSelfInfo().getId() + ">")) {
            log.info(event.getGuild().getName() + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
            CommandManager.commandsExecuted++;
            TalkCommand.talk(event.getAuthor(), event.getTextChannel(), event.getMessage().getRawContent().substring(jdaBot.getSelfInfo().getAsMention().length() + 1));
        }
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (messagesToDeleteIfIdDeleted.containsKey(event.getMessageId())) {
            Message msg = messagesToDeleteIfIdDeleted.remove(event.getMessageId());
            if (msg.getJDA() == jdaBot) {
                msg.deleteMessage();
            }
        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        //Ignore self
        if (event.getAuthor().getUsername().equals(event.getJDA().getSelfInfo().getUsername())) {
            return;
        }

        //Ignore invites (handled elsewhere)
        if (event.getMessage().getContent().contains("discord.gg")) {
            return;
        }

        if (event.getAuthor() == lastUserToReceiveHelp) {
            //Ignore, just got help!
            return;
        }

        event.getChannel().sendMessage(BotConstants.HELP_TEXT);
        lastUserToReceiveHelp = event.getAuthor();
    }

    @Override
    public void onInviteReceived(InviteReceivedEvent event) {
        if (event.getMessage().isPrivate()) {
            event.getAuthor().getPrivateChannel().sendMessage("Sorry! Since the release of the official API, registered bots must now be invited by someone with Manage **Server permissions**. If you have permissions, you can invite me at:\n"
                    + "https://discordapp.com/oauth2/authorize?&client_id=" + BotConstants.CLIENT_ID + "&scope=bot");
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        super.onReady(event);
        jdaBot.getAccountManager().setGame("Say ;;help");
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        jdaBot.getAccountManager().setGame("Say ;;help");
    }

    /* music related */
    @Override
    public void onVoiceLeave(VoiceLeaveEvent event) {
        GuildPlayer player = PlayerRegistry.getExisting(event.getGuild());

        if (player == null) {
            return;
        }

        if (player.getUsersInVC().isEmpty()
                && player.getUserCurrentVoiceChannel(jdaBot.getSelfInfo()) == event.getOldChannel()
                && player.isPaused() == false) {
            player.pause();
            player.getActiveTextChannel().sendMessage("All users have left the voice channel. The player has been paused.");
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        PlayerRegistry.destroyPlayer(event.getGuild());
    }

}
