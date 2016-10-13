package fredboat.db.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import net.dv8tion.jda.entities.TextChannel;

@Entity
@Table(name = "tc_config")
public class TCConfig {

    @Id
    private long channelId;

    @ManyToOne
    private GuildConfig guildConfig;

    public TCConfig() {
    }

    public TCConfig(GuildConfig gc, TextChannel chn) {
        this.channelId = Long.parseLong(chn.getId());
        this.guildConfig = gc;
    }

    public long getTextChannelId() {
        return channelId;
    }

    public GuildConfig getGuildConfiguration() {
        return guildConfig;
    }

}
