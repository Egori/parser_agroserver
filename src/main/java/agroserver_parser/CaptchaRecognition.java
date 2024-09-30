/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agroserver_parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;

/**
 *
 * @author user
 */
public class CaptchaRecognition {

    private static final Proxy TOR_PROXY = new Proxy(Proxy.Type.SOCKS,
            InetSocketAddress.createUnresolved("127.0.0.1", 9050));

    public static void parse(Response jsoupResponse, FormElement captchaForm) throws IOException, Exception {

        // Fetch the captcha image
        Connection.Response response = null;
        try {
            response = Jsoup //
                    .connect(captchaForm.selectFirst("img").absUrl("src")) // Extract image absolute URL
                    .cookies(jsoupResponse.cookies()) // Grab cookies
                    .ignoreContentType(true) // Needed for fetching image
                    .timeout(15000)
                    .proxy(TOR_PROXY)
                    .execute();
        } catch (IOException ex) {
            Logger.getLogger(CaptchaRecognition.class.getName()).log(Level.SEVERE, null, ex);
            parse(jsoupResponse, captchaForm);
        }

        InputStream fileStream = response.bodyStream();

        String jsonPath = System.getProperty("user.dir") + "/API_KEYS/Google/ocr-d5a3bd73f684.json";

        // Vision.authExplicit(jsonPath);
        String captchaText = Vision.detectText(fileStream, System.out, jsonPath);

        Element captchaInputField = captchaForm.select(".captcha_div input").first();
        checkElement("Login Field", captchaInputField);
        captchaInputField.val(captchaText);

        // # Now send the form for login
        Connection.Response captchaActionResponse = captchaForm.submit()
                .cookies(jsoupResponse.cookies())
                .proxy(TOR_PROXY)
                .execute();
    }

    public static void checkElement(String name, Element elem) {
        if (elem == null) {
            throw new RuntimeException("Unable to find " + name);
        }
    }

}
