package fredboat.db.entities;

import fredboat.db.DatabaseManager;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;

@Entity
@Table(name = "guild_config")
public class GuildConfig {

    @Id
    private long guildId;

    @OneToMany
    @JoinColumn(name = "guildconfig")
    private Set<TCConfig> textChannels;

    public GuildConfig() {
        System.out.println("Instantiated!");
    }

    public GuildConfig(Guild guild) {
        System.out.println("Instantiated!" + guild.getId());
        this.guildId = Long.parseLong(guild.getId());

        textChannels = new CopyOnWriteArraySet<>();

        for (TextChannel tc : guild.getTextChannels()) {
            TCConfig tcc = new TCConfig(this, tc);
            textChannels.add(tcc);
        }

        for (TCConfig tcc : textChannels) {
            DatabaseManager.getEntityManager().persist(tcc);
        }
    }

    public Set<TCConfig> getTextChannels() {
        return textChannels;
    }
    
    public void addTextChannel(TCConfig tcc){
        textChannels.add(tcc);
    }
    
    public void removeTextChannel(TCConfig tcc){
        textChannels.remove(tcc);
    }

    public long getGuildId() {
        return guildId;
    }

}
