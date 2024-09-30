/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agroserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class RandomProxy {
    
    private static RandomProxy instance;
    ArrayList<String> proxyList = new ArrayList<String>();
    Random randomGenerator = new Random();
    
    public static synchronized RandomProxy getInstance(){
        if(instance == null){
            instance = new RandomProxy();
        }
        return instance;
    }

    private RandomProxy() {
         try {
            proxyList = getProxyList();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RandomProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
    
    
    public Proxy getRandomProxy(){
        
        Proxy proxy;
       
        int randIndex = randomGenerator.nextInt(proxyList.size());
        String proxyArr[] = proxyList.get(randIndex).split(":");
        
        SocketAddress socketAddr;
        socketAddr =  InetSocketAddress.createUnresolved(proxyArr[0], Integer.parseInt(proxyArr[1]));
        proxy = new Proxy(Proxy.Type.HTTP, socketAddr);
        
        return proxy;
        
    }

    private ArrayList getProxyList() throws FileNotFoundException {
        Scanner s = new Scanner(new File(System.getProperty("user.dir") + "/" + "proxy.txt"));
        ArrayList<String> list = new ArrayList<String>();
        while (s.hasNext()) {
            list.add(s.next());
        }
        s.close();
        return list;
    }

}
