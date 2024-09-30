/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agroserver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author user
 */
public class ParseEmailFromWeb {

    public String getEmails(String site) {

        if (site.equals("") || site == null) {
            return "";
        }
        if(site.contains(".agroserver.")){
            return "";
        }

        String contactPageStr = "";
        Document doc = null;
        Document contactPageDoc = null;
        String emails = "";

        DocumentJsoupConnect docConnect = new DocumentJsoupConnect();
        doc = docConnect.getPageDocDirectly(site);
        if(doc == null){
            return "";
        }
        contactPageStr = getContactPage(doc);

        if (!contactPageStr.equals("")) {

            try {
                docConnect = new DocumentJsoupConnect();
                contactPageDoc = docConnect.getPageDocDirectly(contactPageStr);
            } catch (Exception e) {
                System.out.println("contactPage " + contactPageStr + " not found");
            }

        }

        if (contactPageDoc != null) {
            emails = getEmail(contactPageDoc);

        } else if (doc != null) {
            emails = getEmail(doc);

        }

        return emails;

    }

    private String getEmail(Document doc) {
        String emails = getMailFromPage(doc).toString().replaceAll("^.", "").replaceAll(".$", "");
        if (emails == null) {
            emails = "";
        }
        return emails;
    }

    private static Document getJsoupDoc(String url) {

        Document doc = null;
        Connection.Response res;

        try {
            res = Jsoup.connect(url)
                    .timeout(10000)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64; rv:58.0) Gecko/20100101 Firefox/58.0")
                    .execute();
            doc = res.parse();
        } catch (IOException ex) {
            System.out.println("Не удалось получить страницу " + url);
        }

        return doc;

    }

    // Получение емейла
    private static Set<String> getMailFromPage(Document doc) {

        Set<String> emailSet = new HashSet<>();

        Pattern p = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        Matcher matcher = p.matcher(doc.body().html());
        while (matcher.find()) {
            if (matcher.group().contains("Rating@Mail.ru")
                    || matcher.group().contains(".png")
                    || matcher.group().contains(".gif")
                    || matcher.group().contains(".jpg")) {
                continue;
            }
            emailSet.add(matcher.group());
        }

        return emailSet;

    }

    private static String getContactPage(Document doc) {

        String contUrl = "";
        String baseURL = doc.baseUri();
        // регулярное, для поиска ссылки на контактную страницу
        String contMatches = "[kKcC]onta|[KC]ONTA|[Кк]онтакт|[yY]hteys";

        // представление ссылки содержит подстроку
        contUrl = doc.select("a:matches(" + contMatches + ")").attr("href");

        // ссылка содержит подстроку
        if (contUrl.equals("")) {
            contUrl = doc.select("a[href~=" + contMatches + "]").attr("href");
        }

        // Если ссылка не полная, подставляется url главной страницы
        if (!contUrl.startsWith("http")) {
            if (contUrl.startsWith("/")) {
                contUrl = contUrl.replaceFirst("/", "");
            }
            if (baseURL.endsWith("/")) {
                baseURL = baseURL.replaceFirst("/$", "");
            }
            contUrl = baseURL + "/" + contUrl;
        }

        return contUrl;

    }

}
