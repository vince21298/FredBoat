package fredboat.command.util;

import fredboat.commandmeta.abs.Command;
import fredboat.util.TextUtils;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class AvatarCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        if(message.getMentionedUsers().isEmpty()){
            TextUtils.replyWithMention(channel, invoker, " proper usage is: ```;;avatar @<username>```");
        } else {
            TextUtils.replyWithMention(channel, invoker, " found it\n"+message.getMentionedUsers().get(0).getAvatarUrl());
        }
    }

}
