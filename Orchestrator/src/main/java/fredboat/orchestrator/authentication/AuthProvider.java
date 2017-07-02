/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fredboat.orchestrator.authentication;

import fredboat.shared.constant.DistributionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Created by napster on 02.07.17.
 * <p>
 * Authenticates FredBoats and Admins
 */
@Component
public class AuthProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(AuthProvider.class);

    /**
     * @param authentication The authentication which shall be authenticated
     * @return An authenticated token, or null if the authentication fails
     * @throws AuthenticationException if the authentication could not take place
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            DistributionEnum distribution = DistributionEnum.valueOf((String) authentication.getPrincipal());
            log.info("Authentication request received for distribution {}", distribution.name());
            String tokenHash = (String) authentication.getCredentials();

            checkCredentials(tokenHash, distribution.name());

            if (tokenHash.equals(HashProvider.getTokenHash(distribution))) {
                log.info("Distribution {} has been authenticated", distribution.name());
                return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), Authorities.FREDBOAT);
            } else {
                log.info("Distribution {} has failed authentication", distribution.name());
                throw new BadCredentialsException("Provided credentials did not match stored ones");
            }
        } catch (IllegalArgumentException e) {
            //might be a user trying to log in
            String userName = (String) authentication.getPrincipal();
            log.info("Authentication request received for user {}", userName);
            String password = (String) authentication.getCredentials();

            checkCredentials(password, userName);

            if (password.equals(HashProvider.getPasswordHash(userName))) {
                log.info("User {} has been authenticated", userName);
                return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), Authorities.ADMIN);
            } else {
                log.info("User {} has failed authentication", userName);
                throw new BadCredentialsException("Provided password did not match stored one");
            }
        }
    }

    private void checkCredentials(String credentials, String userName) throws AuthenticationException {
        if (credentials == null) {
            log.info("{} has failed authentication", userName);
            throw new BadCredentialsException("Provided credentials may not be null");
        }

        if (credentials.isEmpty()) {
            log.info("{} has failed authentication", userName);
            throw new BadCredentialsException("Provided credentials may not be empty");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
