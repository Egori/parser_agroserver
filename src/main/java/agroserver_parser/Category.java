/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agroserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author user
 */
public class Category {

    private ArrayList<HashMap<String, String>> categoryListMap = new ArrayList<>();
    private ArrayList<HashMap<String, String>> categoryListMapL2 = new ArrayList<>();
    private ArrayList<HashMap<String, String>> categoryListMapUnite = new ArrayList<>();
    private ArrayList<HashMap<String, String>> categoryListMapResult = new ArrayList<>();
    private Proxy proxy = new Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved("127.0.0.1", 9050));
    private String mainUrl = "https://agroserver.ru/spros/";
    DBAdapter dbAdapter = new DBAdapter("test");
    CSVWriter csvWriter = new CSVWriter("category");
    private static Category instance;

    private Category() {

    }

    public static Category getInstance() {
        if (instance == null) {		
            instance = new Category();	
        }
        return instance;
    }

    public ArrayList<HashMap<String, String>> getCategoryList() {

        Document mainDoc = getPageDoc(mainUrl);
        Elements first_cat = mainDoc.select("div .wrapper div a");
        int i = 0;
        for (Element catEl : first_cat) {
            if(!catEl.ownText().equals("Продукция с/х, сырье")&& !catEl.ownText().equals("Продукты переработки")){
                continue;
            }
            HashMap<String, String> categoryMap = new HashMap<>();
            categoryMap.put("title", catEl.ownText());
            categoryMap.put("url", catEl.absUrl("href"));
            categoryListMap.add(categoryMap);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
        }

        addCategories();

        categoryListMapUnite.addAll(categoryListMap);
        categoryListMapUnite.addAll(categoryListMapL2);

        return getJoinCategoryList();

    }

    private ArrayList<HashMap<String, String>> getJoinCategoryList() {
        ArrayList<HashMap<String, String>> joinCategoryList = new ArrayList<>();
        String concatCategoryStr = "";
        for (HashMap<String, String> catMap : categoryListMapUnite) {
            if (catMap.get("has_child").equals("0")) {
                concatCategoryStr = getParentsConcatCategory(catMap);
                catMap.put("category_path", concatCategoryStr);
                joinCategoryList.add(catMap);
            }
        }
        for (HashMap<String, String> catMap : joinCategoryList) {
            catMap.remove("has_child");
            catMap.remove("parent");
        }
        return joinCategoryList;
    }

    private String getParentsConcatCategory(HashMap<String, String> catMap) {
        String resultCategoryStr = "";
        if (resultCategoryStr.equals("")) {
            resultCategoryStr = catMap.get("title");
        }
        HashMap<String, String> parentCatMap = catMap;

        while (parentCatMap != null) {
            parentCatMap = getParentCatMap(catMap);
            if (parentCatMap != null) {
                resultCategoryStr = parentCatMap.get("title") + "|" + resultCategoryStr;
            }
            catMap = parentCatMap;

        }

        return resultCategoryStr;
    }

    private HashMap<String, String> getParentCatMap(HashMap<String, String> catMap) {
        HashMap<String, String> resultMap = null;
        for (HashMap<String, String> catMapSearch : categoryListMapUnite) {
            if (catMapSearch.get("title").equals(catMap.get("parent"))) {
                return catMapSearch;
            }
        }
        return resultMap;
    }

    private void addCategories() {
        for (HashMap<String, String> catMap : categoryListMap) {
            Document doc = getPageDoc(catMap.get("url"));
            if (doc == null) {
                continue;
            }
            if (hasChildCategory(doc)) {
                catMap.put("has_child", "1");
            } else {
                catMap.put("has_child", "0");
            }
            HashMap<String, String> newCatMap;
            addCategoryListL2(doc, catMap.get("title"));
        }

    }

    private void addCategoryListL2(Document doc, String parentTitle) {
        Elements first_cat = doc.select(".b_list_nav  a");
        for (Element catEl : first_cat) {
            HashMap<String, String> categoryMap = new HashMap<>();
            categoryMap.put("title", catEl.ownText());
            categoryMap.put("url", catEl.absUrl("href"));
            categoryMap.put("parent", parentTitle);
            String catPath = doc.selectFirst(".nav").wholeText().replaceAll("\n", "|");
            categoryMap.put("category_path", catPath);
//            Document childDoc = getPageDoc(categoryMap.get("url"));
//            if (hasChildCategory(childDoc)) {
//                categoryMap.put("has_child", "1");
//                addCategoryListL2(childDoc, categoryMap.get("title"));
//            } else {
//                categoryMap.put("has_child", "0");
//            }
            
            csvWriter.writeMapToCsv(categoryMap);

            Pages pages = Pages.getInstance();
            pages.parsePagesListUrls(categoryMap);

            categoryListMapL2.add(categoryMap);
            
            

            
            // !!!!!! debugging !!!!!!!
            //break;
            // !!!!!! debugging !!!!!!! \\
            try {
                Thread.sleep(2222);
            } catch (InterruptedException ex) {
                Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private boolean hasChildCategory(Document doc) {
        boolean result;
        Element childCatEls = doc.selectFirst(".b_list_nav  a");
        if (childCatEls == null) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    public Document getPageDoc(String url) {
        //debug
        if(url.equals("https://agroserver.ru/spros/oborudovanie-agro/")){
            System.out.println("agroserver.Category.getPageDoc()");
        }
        DocumentJsoupConnect docConnect = new DocumentJsoupConnect();
        Document doc = docConnect.getPageDocFromTor(url);
        System.out.println("agroserver.Category.getPageDoc() | " + url);     
        return doc;
    }
    
    private void checkIp(){
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = runtime.exec("curl --socks5 127.0.0.1:9050 http://checkip.amazonaws.com/");
        } catch (IOException ex) {
            Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedReader lineReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        lineReader.lines().forEach(System.out::println);
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        errorReader.lines().forEach(System.out::println);
    }

}
