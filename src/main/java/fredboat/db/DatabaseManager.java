package fredboat.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fredboat.FredBoat;
import fredboat.db.entities.GuildConfig;
import fredboat.db.entities.TCConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.json.JSONObject;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

public class DatabaseManager {

    private static final Map<Thread, EntityManager> EM_MAP = new ConcurrentHashMap<>();
    private static EntityManagerFactory emf;
    private static final Map<String, GuildConfig> GUILD_CONFIGS = new HashMap<>();

    public static void startup(JSONObject credsJson) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(credsJson.getString("jdbcUrl"));
        DataSource dataSource = new HikariDataSource(config);

        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.connection.driver_class", "org.postgresql.Driver");

        LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
        emfb.setDataSource(dataSource);
        emfb.setPackagesToScan("fredboat.db");
        emfb.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emfb.setJpaProperties(properties);
        emfb.setPersistenceUnitName("fredboat.test");
        emfb.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        emfb.afterPropertiesSet();

        emf = emfb.getObject();
        EntityManager em = getEntityManager();

        em.getTransaction().begin();

        System.out.println(FredBoat.jdaBot.getGuilds());

        for (Guild guild : FredBoat.jdaBot.getGuilds()) {
            GuildConfig gc = em.find(GuildConfig.class, Long.parseLong(guild.getId()));
            if (gc == null) {
                System.err.println("NEW");
                gc = new GuildConfig(guild);
                em.persist(gc);
            }
        }

        em.getTransaction().commit();
    }

    public static EntityManager getEntityManager() {
        EntityManager em = EM_MAP.get(Thread.currentThread());

        if (em == null) {
            EM_MAP.put(Thread.currentThread(), em);
        }

        return em;
    }

    public static GuildConfig getGuildConfig(Guild guild) {
        return GUILD_CONFIGS.get(guild.getId());
    }

    public static TCConfig getTextChannelConfig(TextChannel tc) {
        GuildConfig gc = getGuildConfig(tc.getGuild());

        if (gc != null) {
            long tcId = Long.getLong(tc.getId());
            for (TCConfig tcc : gc.getTextChannels()) {
                if (tcc.getTextChannelId() == tcId) {
                    return tcc;
                }
            }
        }

        return null;
    }

    public static void persistGuildConfig(GuildConfig gc, boolean commit) {
        EntityManager em = getEntityManager();
        if (commit) {
            em.getTransaction().begin();
        }
        
        em.persist(gc);
        GUILD_CONFIGS.put(Long.toString(gc.getGuildId()), gc);
        
        if (commit) {
            em.getTransaction().commit();
        }
    }
    
    public static void persistTextChannelConfig(TCConfig tcc, boolean commit) {
        EntityManager em = getEntityManager();
        if (commit) {
            em.getTransaction().begin();
        }
        
        em.persist(tcc);
        tcc.getGuildConfiguration().addTextChannel(tcc);
        
        if (commit) {
            em.getTransaction().commit();
        }
    }
    
    public static void remove(GuildConfig gc, boolean commit){
        EntityManager em = getEntityManager();
        if (commit) {
            em.getTransaction().begin();
        }
        
        em.remove(gc);
        GUILD_CONFIGS.remove(Long.toString(gc.getGuildId()));
        for(TCConfig tcc : gc.getTextChannels()){
            gc.removeTextChannel(tcc);
            em.remove(tcc);
        }
        
        if (commit) {
            em.getTransaction().commit();
        }
    }
    
    public static void remove(TCConfig tcc, boolean commit){
        EntityManager em = getEntityManager();
        if (commit) {
            em.getTransaction().begin();
        }
        
        em.remove(tcc);
        tcc.getGuildConfiguration().removeTextChannel(tcc);
        
        if (commit) {
            em.getTransaction().commit();
        }
    }

}
