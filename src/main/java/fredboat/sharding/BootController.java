package fredboat.sharding;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.User;
import org.json.JSONArray;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Configuration
@EnableAutoConfiguration(exclude={HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
@Controller
@ComponentScan
public class BootController {

    private final String token;
    private final JDA jda;

    public BootController() {
        this.token = FredBoatAPIServer.token;
        this.jda = FredBoatAPIServer.jda;
    }

    @RequestMapping("/guildCount")
    @ResponseBody
    private String guildCount(HttpServletRequest request, HttpServletResponse response) {
        if (isAuthenticated(request)) {
            return String.valueOf(jda.getGuilds().size());
        } else {
            response.setStatus(403);
            return null;
        }
    }
    
    @RequestMapping("/globalGuildCount")
    @ResponseBody
    private String globalGuildCount(HttpServletRequest request, HttpServletResponse response) {
        if (isAuthenticated(request)) {
            return String.valueOf(ShardTracker.getGlobalGuildCount());
        } else {
            response.setStatus(403);
            return null;
        }
    }

    @RequestMapping("/users")
    @ResponseBody
    private String users(HttpServletRequest request, HttpServletResponse response) {
        if (isAuthenticated(request)) {
            JSONArray a = new JSONArray();

            for (User user : FredBoatAPIServer.jda.getUsers()) {
                a.put(user.getId());
            }

            return a.toString();
        } else {
            response.setStatus(403);
            return null;
        }
    }
    
    @RequestMapping("/globalUserCount")
    @ResponseBody
    private String globalUserCount(HttpServletRequest request, HttpServletResponse response) {
        if (isAuthenticated(request)) {
            return String.valueOf(ShardTracker.getGlobalUserCount());
        } else {
            response.setStatus(403);
            return null;
        }
    }

    public boolean isAuthenticated(HttpServletRequest request) {
        return request.getHeader("authorization").equals(token);
    }

}
