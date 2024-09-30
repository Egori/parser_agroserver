/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agroserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mumu
 */
public class PrepareQuery {

    private static Set<String> uniqueItemsSet;

    public PrepareQuery() {
        if (uniqueItemsSet == null) {
            uniqueItemsSet = new HashSet<>();
        }
    }

    public void query(String category) {
        if (uniqueItemsSet == null) {
            uniqueItemsSet = new HashSet<>();
        }
        if (!uniqueItemsSet.add(category) || category.equals("")) {
            return;
        }
//        Thread thread = new Thread(new QueryRunnable(category));
//        thread.start();
        QueryRunnable qr = new QueryRunnable(category);
        qr.run();

    }

    class QueryRunnable implements Runnable {

        Thread thread;
        private String categoryStr;

        QueryRunnable(String category) {
            categoryStr = category;
        }

        @Override
        public void run() {
            try {
                URL url = null;
                URI uri = null;
//                String location = URLEncoder.encode("Россия", "UTF-8");
//                categoryStr = URLEncoder.encode(categoryStr, "UTF-8");
//                System.out.println("start query: " + categoryStr);
                try {
                    uri = new URI("http", null,"95.214.63.130" , 8080, "/search_org_1.1/search", "q=" + categoryStr + "&location=Россия&cache=emails&lang=ru&wholesale", null);
                } catch (URISyntaxException ex) {
                    Logger.getLogger(PrepareQuery.class.getName()).log(Level.SEVERE, null, ex);
                }
                String urlStr = uri.toASCIIString();
                url = new URL(urlStr);
                URLConnection urlConn = url.openConnection();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                urlConn.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = reader.readLine()) != null) {
                        System.out.println(inputLine);
                    }
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(PrepareQuery.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrepareQuery.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
