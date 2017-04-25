package fredboat.util.ratelimit;

/**
 * Created by napster on 24.04.17.
 * <p>
 * Wraps a result from the ratelimiter
 */
public class RateResult {
    //yeah or nah
    public boolean allowed;

    //y tho
    public String reason;

    public RateResult(boolean allowed, String reason) {
        this.allowed = allowed;
        this.reason = reason;
    }
}
