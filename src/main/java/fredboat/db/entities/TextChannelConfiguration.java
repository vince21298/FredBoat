package fredboat.db.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import net.dv8tion.jda.entities.TextChannel;

@Entity
@Table(name = "tc_configuration")
public class TextChannelConfiguration {

    @Id
    private long textChannelId;

    @ManyToOne
    private GuildConfiguration guildConfig;

    public TextChannelConfiguration() {
    }

    public TextChannelConfiguration(GuildConfiguration gc, TextChannel chn) {
        this.textChannelId = Long.parseLong(chn.getId());
        this.guildConfig = gc;
    }

    public long getTextChannelId() {
        return textChannelId;
    }

    public GuildConfiguration getGuildConfiguration() {
        return guildConfig;
    }

}
