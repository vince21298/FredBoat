package fredboat.db.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_config")
public class UConfig {

    @Id
    private long userId;
    private String bearer;
    private String refresh;
    private String webtoken;

    public String getBearer() {
        return bearer;
    }

    public String getRefresh() {
        return refresh;
    }

    public long getUserId() {
        return userId;
    }

    public String getWebtoken() {
        return webtoken;
    }

    public void setBearer(String bearer) {
        this.bearer = bearer;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setWebtoken(String webtoken) {
        this.webtoken = webtoken;
    }

}
