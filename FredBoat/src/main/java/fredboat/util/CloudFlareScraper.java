/*
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

package fredboat.util;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by napster on 31.03.17.
 * <p>
 * This class uses a headless browser to circumvent the CloudFlare basic javascript challenge protection
 * Attention: Yes, this runs wild javascript from the interwebs (the ones we request, that is) and executes it for a few
 * seconds. Shouldn't be a problem as the underlying Javascript engine seems sandboxed.
 */
public class CloudFlareScraper {

    private static final Logger log = LoggerFactory.getLogger(CloudFlareScraper.class);

    //the cookie manager will keep track of the precious CloudFlare cookies between requests
    //it claims to be thread safe
    private static final CookieManager cookieManager = new CookieManager();

    /**
     * Make a GET request to the url and return the body of the response
     * <p>
     * This is thread blocking, so best used with some async magic.
     */
    public static String get(String url) {

        String result = "";

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.setCookieManager(cookieManager);

            try {
                HtmlPage page = webClient.getPage(url);

                //CloudFlare T R I G G E R E D
                //wait it out to let it do its redirect & set cookies that we will use for faster requests in the future
                if (page.getWebResponse().getStatusCode() != 200) {
                    webClient.waitForBackgroundJavaScript(10000); //usually takes about 5 seconds
                    page = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
                }

                result = page.getBody().asXml();
            } catch (IOException e) {
                log.error("Error while requesting CloudFlare protected url " + url, e);
            }
        }
        return result;
    }
}
