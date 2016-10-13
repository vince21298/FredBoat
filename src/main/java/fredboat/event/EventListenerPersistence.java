package fredboat.event;

import fredboat.db.DatabaseManager;
import fredboat.db.entities.GuildConfig;
import fredboat.db.entities.TCConfig;
import net.dv8tion.jda.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

public class EventListenerPersistence extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        GuildConfig gc = new GuildConfig(event.getGuild());
        DatabaseManager.persistGuildConfig(gc, true);
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        GuildConfig gc = new GuildConfig(event.getGuild());
        DatabaseManager.remove(gc, true);
    }

    @Override
    public void onTextChannelCreate(TextChannelCreateEvent event) {
        TCConfig tcc = new TCConfig(DatabaseManager.getGuildConfig(event.getGuild()), event.getChannel());
        DatabaseManager.persistTextChannelConfig(tcc, true);
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        TCConfig tcc = new TCConfig(DatabaseManager.getGuildConfig(event.getGuild()), event.getChannel());
        DatabaseManager.remove(tcc, true);
    }
    
    
    
}
