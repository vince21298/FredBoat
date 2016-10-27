package fredboat.agent;

import com.mashape.unirest.http.Unirest;
import fredboat.sharding.ShardTracker;
import net.dv8tion.jda.JDA;
import org.slf4j.LoggerFactory;

public class CarbonitexAgent extends Thread {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CarbonitexAgent.class);
    
    private final String key;
    public final JDA jda;

    public CarbonitexAgent(JDA jda, String key) {
        this.jda = jda;
        this.key = key;
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (this) {
                    sendStats();
                    sleep(30 * 60 * 1000);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendStats() {
        try {
            final String response = Unirest.post("https://www.carbonitex.net/discord/data/botdata.php").field("key", key)
                    .field("servercount", ShardTracker.getGlobalGuildCount()).asString().getBody();
            log.info("Successfully posted the botdata to carbonitex.com: " + response);
        } catch (Exception e) {
            log.error("An error occured while posting the botdata to carbonitex.com", e);
        }
    }

}
