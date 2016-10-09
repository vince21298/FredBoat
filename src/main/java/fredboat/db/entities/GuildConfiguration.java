package fredboat.db.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "guild_configuration_2")
public class GuildConfiguration {

    @Id
    private long guildId;
    private String test;

    public GuildConfiguration() {
    }

    public GuildConfiguration(long guildId) {
        this.guildId = guildId;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getTest() {
        return test;
    }

    
    
}
