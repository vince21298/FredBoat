package fredboat.command.fun;

import fredboat.commandmeta.abs.IFunCommand;
import fredboat.feature.I18n;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.text.MessageFormat;

/**
 * Created by napster on 30.04.17.
 * <p>
 * Hug someone. Thx to Rube Rose for collecting the hug gifs.
 */
public class HugCommand extends RandomImageCommand implements IFunCommand {

    public HugCommand(String[] urls) {
        super(urls);
    }

    public HugCommand(String imgurAlbumUrl) {
        super(imgurAlbumUrl);
    }

    @Override
    public void onInvoke(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {

        Message hugMessage = null;
        if (message.getMentionedUsers().size() > 0) {
            if (message.getMentionedUsers().get(0) == guild.getJDA().getSelfUser()) {
                hugMessage = new MessageBuilder().append(I18n.get(guild).getString("hugBot")).build();
            } else {
                hugMessage = new MessageBuilder()
                        .append("_")
                        .append(MessageFormat.format(I18n.get(guild).getString("hugSuccess"), message.getMentionedUsers().get(0).getAsMention()))
                        .append("_")
                        .build();
            }
        }
        super.sendRandomFileWithMessage(channel, hugMessage);
    }

    @Override
    public String help(Guild guild) {
        return "{0}{1} @<username>\n#Hug someone.";
    }
}
