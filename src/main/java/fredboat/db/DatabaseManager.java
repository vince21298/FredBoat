package fredboat.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fredboat.db.entities.GuildConfiguration;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.json.JSONObject;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

public class DatabaseManager {

    public static EntityManager startup(JSONObject credsJson) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(credsJson.getString("jdbcUrl"));
        DataSource dataSource = new HikariDataSource(config);

        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");

        LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
        emfb.setDataSource(dataSource);
        emfb.setPackagesToScan("fredboat.db");
        emfb.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emfb.setJpaProperties(properties);
        emfb.setPersistenceUnitName("fredboat.test");
        emfb.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        emfb.afterPropertiesSet();

        EntityManagerFactory emf = emfb.getObject();
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        GuildConfiguration cfg = em.find(GuildConfiguration.class, 174820236481134592L);

        em.getTransaction().commit();
        em.getTransaction().begin();
        
        if (cfg == null) {
            cfg = new GuildConfiguration(174820236481134592L);
            cfg.setTest("new");
            em.persist(cfg);
        } else {
            cfg.setTest(cfg.getTest() + " merged");
            em.merge(cfg);
        }
        System.out.println(cfg.getTest());
        em.getTransaction().commit();
        return em;
    }

}
