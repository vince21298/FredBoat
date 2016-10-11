package fredboat.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fredboat.FredBoat;
import fredboat.db.entities.GuildConfiguration;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import net.dv8tion.jda.entities.Guild;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.json.JSONObject;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

public class DatabaseManager {

    private static EntityManager em;

    public static EntityManager startup(JSONObject credsJson) {
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

        EntityManagerFactory emf = emfb.getObject();
        em = emf.createEntityManager();

        em.getTransaction().begin();

        FredBoat.jdaBot.getGuilds().forEach((Object o) -> {
            Guild guild = (Guild) o;
            GuildConfiguration gc = em.find(GuildConfiguration.class, Long.parseLong(guild.getId()));
            if (gc == null) {
                gc = new GuildConfiguration(guild);
                em.persist(gc);
            }
        });

        em.getTransaction().commit();

        return em;
    }

    public static EntityManager getEntityManager() {
        return em;
    }

}
