/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agroserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author user
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {


        Category category = Category.getInstance();
        ArrayList<HashMap<String, String>> categoryListMap = category.getCategoryList();

       Pages pages = new Pages();
       ArrayList<HashMap<String, String>> pagesListMap = pages.getPagesList();
    
    }
    
}
