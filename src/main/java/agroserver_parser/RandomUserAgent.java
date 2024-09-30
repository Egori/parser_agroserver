/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agroserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class RandomUserAgent {

    private static RandomUserAgent instance;
    ArrayList<String> UAList = new ArrayList<String>();
    Random randomGenerator = new Random();
    
    

    public static synchronized RandomUserAgent getInstance() {
        if (instance == null) {
            instance = new RandomUserAgent();
        }
        return instance;
    }

    public RandomUserAgent() {
        try {
            UAList = getUserAgentList();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RandomUserAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public String getRandomUserAgent() {


        int randIndex = randomGenerator.nextInt(UAList.size());
        String UAStr = UAList.get(randIndex);

        return UAStr;

    }

    private ArrayList getUserAgentList() throws FileNotFoundException {
        Scanner s = new Scanner(new File(System.getProperty("user.dir") + "/" + "user_agents.txt"));
        ArrayList<String> list = new ArrayList<String>();
        while (s.hasNextLine()) {
            list.add(s.nextLine());
        }
        s.close();
        return list;
    }

}
