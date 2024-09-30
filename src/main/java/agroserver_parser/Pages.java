/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agroserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author user
 */
public class Pages {

    private ArrayList<HashMap<String, String>> pagesListMap = new ArrayList<>();
    private ArrayList<HashMap<String, String>> categoryListMap = new ArrayList<>();
    List<Thread> parseEmailThreads = new ArrayList<Thread>();
    private DBAdapter dbAdapter = new DBAdapter("agroserver_pages_supply");;
    private static Pages instance;
    private static int count_all = 0;
    private static int count_with_emails = 0;
    Set<String> uniqueSet = new HashSet<String>();
    

    Pages() {
        
    }
    
    public static Pages getInstance(){ 
        if(instance == null){		
            instance = new Pages();	
        }
        return instance;
    }
    
    public void setCategoryListMap(ArrayList<HashMap<String, String>> categoryListMap ){
        this.categoryListMap = categoryListMap;
    }

    public ArrayList<HashMap<String, String>> getPagesList() {

        for (HashMap<String, String> categoryMap : categoryListMap) {

            parsePagesListUrls(categoryMap);
            try {
                Thread.sleep(5555);
            } catch (InterruptedException ex) {
                Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        for (Thread parseEmailThread : parseEmailThreads) {
            try {
                parseEmailThread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Pages.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return pagesListMap;
    }

    public void parsePagesListUrls(HashMap<String, String> categoryMap) {
        Document pageDoc = getPageDoc(categoryMap.get("url"));
        Elements pageEls = pageDoc.select("table.b_list_table tr");
        for (Element pageEl : pageEls) {
            /// only for current date //
            String dateOfPost = pageEl.select("td:eq(0)").text().split("\\D", 0)[0];
            SimpleDateFormat dateform = new SimpleDateFormat("d");
            String currDate = dateform.format(new Date());
            if(!dateOfPost.equals(currDate)){
                //dbAdapter.uploadJsonToFtp();
                break;
            }            
            ///\ only for current date
            HashMap<String, String> pageMap = new HashMap<String, String>();
            pageMap.put("category_path", categoryMap.get("category_path"));
            pageMap.put("category_title", categoryMap.get("title"));
            pageMap.put("category_parent", categoryMap.get("parent"));
            pageMap.put("category_url", categoryMap.get("url"));
            pageMap.put("title", pageEl.select("td:eq(1)").text());
            pageMap.put("url", pageEl.select("td:eq(1) a").attr("abs:href"));
            pageMap.put("user", pageEl.select("td:eq(3)").text());
            pageMap.put("user_page", pageEl.select("td:eq(3) a").attr("abs:href"));
            pageMap.put("region", pageEl.select("td:eq(2)").text());
            
            // filter for unique //
            if(!uniqueSet.contains(pageMap.get("category_parent") + "|" + pageMap.get("user_page"))){
                uniqueSet.add(pageMap.get("category_parent") + "|" + pageMap.get("user_page"));
            }else{
                continue;
            }            
            //\filter for unique
            
            pageMap = filterPageMap(pageMap);
            if(pageMap == null || pageMap.get("category")==null){
                continue;
            }
            
            try {
                pageMap.putAll(parsePage(pageMap.get("url")));
                pageMap.putAll(parseUserPage(pageMap.get("user_page")));
            } catch (Exception e) {
                continue;
            }
            
            pageMap.remove("category_parent");
            pageMap.remove("category_path");
            pageMap.remove("category_title");
            pageMap.remove("category_url");
            pageMap.remove("url");
            pageMap.remove("user_page");
            //pageMap.put("date", currDate);

            //// filter for auction pages ////  
                    
            count_all++;
            System.out.println(count_all + " : all pages");
            if(pageMap.get("emails").equals("")){
                continue;
            }
            count_with_emails++;
            System.out.println(count_with_emails + " : pages with emails");
            
           /// \filter for auction pages ////
                    
            pagesListMap.add(pageMap);            
            dbAdapter.writeMapToCsv(pageMap);
            dbAdapter.addToListPage(pageMap);
            System.out.println("try to get query: " + pageMap.get("category") + " ...");
            PrepareQuery prepareQuery = new PrepareQuery();
            
            prepareQuery.query(pageMap.get("category"));
//            try {
//                Thread.sleep(5555);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
        
        //// next string ///
//        if (pageDoc.select("ul.pgt li:eq(2) a").size() > 0) {
//            categoryMap.put("url", pageDoc.select("ul.pgt li:eq(2) a").attr("abs:href"));
//            parsePagesListUrls(categoryMap);
//        } 


//        try {
//            dbAdapter.writePagesToJson();
//        } catch (IOException ex) {
//            Logger.getLogger(Pages.class.getName()).log(Level.SEVERE, null, ex);
//        }
        dbAdapter.uploadJsonToFtp();
        
    }
    
    // filter page info for auction
    private HashMap<String, String> filterPageMap(HashMap<String, String> pageMap){
          SimpleDateFormat  dateform = new SimpleDateFormat("dd.MM.Y");
          String  currDate = dateform.format(new Date());
        //HashMap<String, String> filteredPageMap;
        String categorySelectedString = "";
        
        switch (pageMap.get("category_parent")) {
            case ("Сельскохозяйственная техника"):
                categorySelectedString = pageMap.get("category_parent");
                break;
            case ("Продукция с/х, сырье"):
                categorySelectedString = pageMap.get("category_title");
                if(pageMap.get("category_title").equals("Грибы")){
                    categorySelectedString = "";
                }
                if(pageMap.get("category_title").equals("Техническое сырье")){
                    categorySelectedString = "";
                }
                if(pageMap.get("category_title").equals("Технические культуры")){
                    categorySelectedString = "";
                }
                if(pageMap.get("category_title").equals("С/х животные и птица (живок)")){
                    categorySelectedString = "Животноводство";
                }
                if (pageMap.get("category_title").equals("Зерно, зернобобовые")
                       || pageMap.get("category_title").equals("Масличные культуры")) {
                    categorySelectedString = "Зерно";
                }
                if (pageMap.get("category_title").equals("Мёд, продукция пчеловодства")){
                    categorySelectedString = "Продукты пчеловодства";
                }
                break;
            case ("Оборудование"):
                categorySelectedString = "Оборудование для сельского хозяйства";
                if(pageMap.get("category_title").equals("Хлебопекарное и кондитерское оборудование")){
                    categorySelectedString = pageMap.get("category_title");
                }else if(pageMap.get("category_title").equals("Холодильное оборудование")){
                    categorySelectedString = pageMap.get("category_title");
                }
                break;
            case("Продукты переработки"):
                categorySelectedString = pageMap.get("category_title");
                if(pageMap.get("category_title").equals("Масложировая продукция")){
                    categorySelectedString = "Маслопродукты";
                }
                if(pageMap.get("category_title").equals("Сушеные овощи и фрукты")){
                    categorySelectedString = "Сухофрукты";
                }
                if(pageMap.get("category_title").equals("Мясо и мясные продукты")){
                    categorySelectedString = "Мясо";
                }
                break;
            case("Земли и объекты с/х недвижимости"):
                categorySelectedString = "";
                break;
            case("Услуги"):
                categorySelectedString = "";
                break;
            case("Корма для с.х. животных и птиц"):
                categorySelectedString = "Сельскохозяйственные корма";
                break;
            case("Прочее"):
                categorySelectedString = "";
                break;
            default:
                categorySelectedString = pageMap.get("category_parent");
                break;
        }
        
        pageMap.put("category", categorySelectedString);
            
            if(pageMap.get("category")==null || pageMap.get("category").equals("")){
                pageMap = null;
            }
            return pageMap;
    }

    private HashMap<String, String> parsePage(String url) {
        Document pageDoc = getPageDoc(url);
        HashMap<String, String> pageMap = new HashMap<String, String>();
        if(pageDoc == null){
            return pageMap;
        }
        String textPage = pageDoc.selectFirst("div.of div.text").text();
        pageMap.put("text", textPageRemoveContacts(textPage));
        pageMap.put("date", pageDoc.selectFirst("div.date").ownText());
        pageMap.put("category_own", pageDoc.select("div .nav  a").last().text());

        return pageMap;
    }
    
    private String textPageRemoveContacts(String text){
        text = text.replaceAll("([^\\.]|Тел.)*\\d{3}[\\s-].*|([^\\.]*\\b[\\w\\.-]+@[\\w\\.-]+\\.\\w{2,4}\\b.*)", "");
        return text;
    }

    private HashMap<String, String> parseUserPage(String url) {
        Document pageDoc = getPageDoc(url);
        HashMap<String, String> pageMap = new HashMap<String, String>();
         if(pageDoc == null){
            return pageMap;
        }
        pageMap.put("cantact_name", pageDoc.select("div.rblock li:contains(Контактное лицо:) span").text());
        pageMap.put("cantact_address", pageDoc.select("div.rblock li:contains(Адрес:) span").text());
        pageMap.put("cantact_site", pageDoc.select("div.rblock li:contains(Сайт:) span a").attr("abs:href"));
        pageMap.put("cantact_locality", pageDoc.select("div.rblock li:contains(Населенный пункт:) span").text());
        List<String> phoneList = pageDoc.select("div.rblock li:contains(Телефон:) span").eachText();
        // add separator between phones
        StringBuilder sbString = new StringBuilder("");
        for (String phoneStr : phoneList) {
            sbString.append(phoneStr).append("|");
        }
        String phonesStr = sbString.toString();

        //remove last separator from String
        if (phonesStr.length() > 0) {
            phonesStr = phonesStr.substring(0, phonesStr.length() - 1);
        }
        pageMap.put("cantact_phones", phonesStr);
        
        
        try {
            parseEmail(pageMap, pageMap.get("cantact_site"));
        } catch (Exception e) {
        }

        return pageMap;
    }

    public Document getPageDoc(String url) {
         DocumentJsoupConnect docConnect = new DocumentJsoupConnect();  
      Document doc = docConnect.getPageDocFromTor(url);
        System.out.println("agroserver.Pages.getPageDoc() | " + url);
        try {
            Thread.sleep(5222);
        } catch (InterruptedException ex) {
            Logger.getLogger(Pages.class.getName()).log(Level.SEVERE, null, ex);
        }
        return doc;
    }

    private void checkIp() {
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

    private void parseEmail(HashMap<String, String> pageMap, String url) {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ParseEmailFromWeb parseEmail = new ParseEmailFromWeb();
//                pageMap.put("emails", parseEmail.getEmails(url));
//            }
//        });
//        thread.start();
//        parseEmailThreads.add(thread);
        if(url.equals("")){
            pageMap.put("emails", "");
            return;
        }
        String cacheEmailStr = dbAdapter.getCacheUserFromDB(url);
        if (cacheEmailStr != null) {
            pageMap.put("emails", cacheEmailStr);
        } else {
            ParseEmailFromWeb parseEmail = new ParseEmailFromWeb();
            pageMap.put("emails", parseEmail.getEmails(url));
            dbAdapter.setEmailToDB(url, pageMap.get("emails"));
        }
    }
    
}
