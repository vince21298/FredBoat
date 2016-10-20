package fredboat.db;

import fredboat.FredBoat;
import fredboat.db.entities.UConfig;
import java.net.URI;
import java.util.List;
import javax.persistence.EntityManager;
import net.dv8tion.jda.utils.ApplicationUtil;
import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.oauth2.client.BasicOAuth2AuthorizationProvider;
import org.dmfs.oauth2.client.BasicOAuth2Client;
import org.dmfs.oauth2.client.BasicOAuth2ClientCredentials;
import org.dmfs.oauth2.client.OAuth2AuthorizationProvider;
import org.dmfs.oauth2.client.OAuth2Client;
import org.dmfs.oauth2.client.OAuth2ClientCredentials;
import org.dmfs.rfc5545.Duration;

public class AccountManager {

    private static OAuth2Client oauth = null;
    private static HttpRequestExecutor executor = null;
    
    public static void init(String secret) {
        executor = new HttpUrlConnectionExecutor();

        // Create OAuth2 provider
        OAuth2AuthorizationProvider provider = new BasicOAuth2AuthorizationProvider(
                URI.create("https://discordapp.com/api/oauth2/authorize"),
                URI.create("https://discordapp.com/api/oauth2/token"),
                new Duration(1, 0, 3600) /* default expiration time in case the server doesn't return any */);

        String clientId = ApplicationUtil.getApplicationId(FredBoat.jdaBot);
        
        OAuth2ClientCredentials credentials = new BasicOAuth2ClientCredentials(
                clientId, secret);

        oauth = new BasicOAuth2Client(
                provider,
                credentials,
                URI.create("http://localhost") /* Redirect URL, unused */);
    }

    public static UConfig getUserForToken(String token) {
        EntityManager em = DatabaseManager.getEntityManager();
        List list = em.createQuery("SELECT uc FROM user_config uc WHERE uc.token = :token").setParameter("token", token).getResultList();

        if (list.isEmpty()) {
            return null;
        }

        return (UConfig) list.get(0);
    }

    public static UConfig handleCallback(String code) {
        return null;
    }

}
