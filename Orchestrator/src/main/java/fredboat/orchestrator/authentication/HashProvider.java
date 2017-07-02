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
import fredboat.shared.util.RestUtil;

/**
 * Created by napster on 02.07.17.
 * <p>
 * Provide hashes of the tokens in use
 */
public class HashProvider {

    /**
     * @param distribution The distribution which hashed token shall be returned
     * @return Hash of the token of the provided distribution
     */
    public static String getTokenHash(DistributionEnum distribution) {
        //todo fetch this from a database or something
        return RestUtil.hashUrlSafe("password");
    }

    /**
     * @param username The name of the user whose hashed password shall be returned
     * @return Hash of the password for the provided username
     */
    public static String getPasswordHash(String username) {
        //todo fetch this from a database or something
        return RestUtil.hashUrlSafe("password");
    }
}
