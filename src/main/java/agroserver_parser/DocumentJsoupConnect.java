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
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;

/**
 *
 * @author user
 */
public class DocumentJsoupConnect {

    private Proxy proxy = null;
    private final Proxy TOR_PROXY = new Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved("127.0.0.1", 9050));
    private int countTryingCurrent = 0;
    private final int COUNT_TRYING = 8;
    private RandomUserAgent userAgent;

    public DocumentJsoupConnect() {

        userAgent = RandomUserAgent.getInstance();

    }

  
    public Document getPageDocFromTor(String url) {

        Response res = null;
        Document doc = null;

        if (countTryingCurrent > COUNT_TRYING) {
            return doc;
        }
        
        try {
            res = getJsoupResponse(url);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
        }       
        
        try {
            doc = res.parse();
        } catch (IOException ex) {
            try {
                modTorNode();
            } catch (IOException | InterruptedException e) {
                Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, e);
            }
            doc = getPageDocFromTor(url);
            Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        
            FormElement captchaForm = (FormElement) doc.select("div.forma.froma_corr form").first();
            
            if(res.statusCode() == 503 && captchaForm == null){
                try {
                    modTorNode();
                    doc = getPageDocFromTor(url);
                } catch (IOException | InterruptedException e) {
                    Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            
            if (captchaForm != null) {
                try {
                    CaptchaRecognition.parse(res, captchaForm);
                    doc = getPageDocFromTor(url);
                } catch (Exception ex) {
                    Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
                    try {
                        modTorNode();
                    } catch (IOException | InterruptedException e) {
                        Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, e);
                    }
                    doc = getPageDocFromTor(url);
                }
            }       
        

//        if (res.statusCode() != 200 || res.statusCode() == 503) {
//            System.out.println(res.statusCode() + ": " + res.statusMessage());
//            try {
//                modTorNode();
//            } catch (IOException ex) {
//                Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            countTryingCurrent += 1;          
//            doc = getPageDocFromTor(url);
//        }else{
//            try {
//                doc = res.parse();
//            } catch (IOException ex) {
//                doc = getPageDocFromTor(url);
//                Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

//        try {
//            doc = res.parse();
//        } catch (IOException ex) {
//            try {
//                modTorNode();
//            } catch (IOException ex1) {
//                Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex1);
//            } catch (InterruptedException ex1) {
//                Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex1);
//            }
//            doc = getPageDocFromTor(url);
//            Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
//        }


        return doc;

    }

    private Response getJsoupResponse(String url) throws IOException, InterruptedException {
        Response resp = null;
        try {
            resp = Jsoup
                    .connect(url)
                    .proxy(TOR_PROXY)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .userAgent(userAgent.getRandomUserAgent()).execute();
        } catch (IOException ex) {

            modTorNode();

            Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);

            countTryingCurrent += 1;
            
            if(countTryingCurrent > 10){
                countTryingCurrent = 0;
                return resp;
            }
            
            resp = getJsoupResponse(url);
            
        }
        
        countTryingCurrent = 0;

        return resp;
    }

    public Document getPageDocFromProxy(String url) {

        RandomProxy randProxy = RandomProxy.getInstance();
        proxy = randProxy.getRandomProxy();

        Connection.Response res = null;
        Document doc = null;

        if (countTryingCurrent > COUNT_TRYING) {
            return doc;
        }

        try {
            res = Jsoup
                    .connect(url)
                    .proxy(proxy)
                    .timeout(10000)
                    .userAgent(userAgent.getRandomUserAgent()).execute();
            doc = res.parse();
        } catch (IOException ex) {
            Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (doc == null || res.statusCode() != 200 || doc.selectFirst("div.a .captcha_div") != null) {
            try {
                modTorNode();
            } catch (IOException ex) {
                Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
            countTryingCurrent += 1;
            doc = getPageDocFromProxy(url);
        }

        return doc;

    }

    public Document getPageDocDirectly(String url) {
        Connection.Response res = null;
        Document doc = null;

        try {
            res = Jsoup
                    .connect(ChangeToPunycode(url))
                    .userAgent(userAgent.getRandomUserAgent())
                    .execute();
            doc = res.parse();
        } catch (IOException ex) {
            Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
        }

        return doc;

    }
    
    private static String ChangeToPunycode(String s) {
        URL url;
        try {
            url = new URL(s);
            String oldHost = url.getHost();
            String newHost = java.net.IDN.toASCII(oldHost);
            s = s.replaceFirst(oldHost, newHost);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return s;
    }
    
       private String checkIp() {
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = runtime.exec("curl --socks5 127.0.0.1:9050 http://checkip.amazonaws.com/");
        } catch (IOException ex) {
            Logger.getLogger(Category.class.getName()).log(Level.SEVERE, null, ex);
        }
        String message = "";
        try {
            message = IOUtils.toString(new InputStreamReader(process.getInputStream()));
            System.out.println(message);
        } catch (IOException ex) {
            Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        errorReader.lines().forEach(System.out::println);
        return message;
    }
       
    private void modTorNode() throws IOException, InterruptedException {
        System.out.println("modTorNode() start...");
        String ip_message_before = checkIp();
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        process = runtime.exec("sh tor_new_node.sh");
//        String[] cmd = { "/bin/sh", "-c", "cd ~; sh tor_new_node.sh" };
//        process = runtime.exec(cmd);
        
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            //timeout - kill the process. 
            process.destroy(); // consider using destroyForcibly instead
            torRestart();
        }   

        System.out.println("modTorNode() wait...");
        Thread.sleep(22000);
        String ip_message_after = checkIp();
        if(ip_message_before.equals(ip_message_after)){
            System.out.println("modTorNode() ip not changed, trying again...");
            modTorNode();
        }else{
            System.out.println("modTorNode() ip changed successfull");
        }
    }
    
    private void torRestart() {

        try {
            System.out.println("torRestart() start...");
//        Runtime runtime = Runtime.getRuntime();
//        Process process = null;
// 
//        try {
//            process = runtime.exec("sudo service tor restart");
//            if (!process.waitFor(5, TimeUnit.SECONDS)) {
//                //timeout - kill the process.
//                process.destroy();
//            }
//        } catch (InterruptedException | IOException ex) {
//            Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
//        }

            String[] cmd = {"/bin/bash", "-c", "sudo service tor restart"};
            Process pb = Runtime.getRuntime().exec(cmd);

            String line;
            BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
        } catch (IOException ex) {
            Logger.getLogger(DocumentJsoupConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
