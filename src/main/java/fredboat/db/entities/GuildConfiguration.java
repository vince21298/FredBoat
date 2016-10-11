package fredboat.db.entities;

import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;

@Entity
@Table(name = "guild_config")
public class GuildConfiguration {

    @Id
    private long guildId;

    @OneToMany
    @JoinColumn(name = "guildConfig")
    private Set<TextChannelConfiguration> textChannels;

    public GuildConfiguration() {
    }

    public GuildConfiguration(Guild guild) {
        this.guildId = Long.parseLong(guild.getId());

        for (TextChannel tc : guild.getTextChannels()) {
            TextChannelConfiguration tcc = new TextChannelConfiguration(this, tc);
            textChannels.add(tcc);
        }
    }

}
