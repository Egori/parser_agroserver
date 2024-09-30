package agroserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;


import org.json.JSONArray;
import org.json.JSONObject;

public class CSVWriter {

    String fileName = null;
    boolean isHeadersCsv = false;
    List<String> headersCsv = null;
    static List<Map<String, String>> listMapCategory;
    static List<Map<String, String>> listMapPage;
    private static int countLastUpload;

    public CSVWriter(String fileName) {
        this.listMapCategory = new ArrayList<>();
        this.listMapPage = new ArrayList<>();
        this.fileName = fileName;
    }

        public void addToListCategory(HashMap<String, String> map) {
        listMapCategory.add(map);
    }
    
    void addToListPage(HashMap<String, String> pageMap) {
        this.listMapPage.add(pageMap);
    }
    
    public void listMaptoCSV(List<Map<String, Object>> listMap) {
        final StringBuffer sb = new StringBuffer();
        if (!isHeadersCsv) {
            headersCsv = listMap.stream().flatMap(map -> map.keySet().stream()).distinct().collect(toList());
            for (int i = 0; i < headersCsv.size(); i++) {
                sb.append(headersCsv.get(i));
                sb.append(i == headersCsv.size() - 1 ? "\n" : "#");
            }
        }
        
        isHeadersCsv = true;
        for (Map<String, Object> map : listMap) {
            for (int i = 0; i < headersCsv.size(); i++) {
                sb.append(map.get(headersCsv.get(i)));
                sb.append(i == headersCsv.size() - 1 ? "\n" : "#");
            }
        }
        try {
            writeCsv(sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(DBAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void writeMapToCsv(Map<String, String> mapData) {
        
        final StringBuffer sb = new StringBuffer();
        if (!isHeadersCsv) {
            headersCsv = mapData.keySet().stream().distinct().collect(toList());
            for (int i = 0; i < headersCsv.size(); i++) {
                sb.append(headersCsv.get(i));
                sb.append(i == headersCsv.size() - 1 ? "\n" : "#");
            }
        }
        isHeadersCsv = true;
        for (int i = 0; i < headersCsv.size(); i++) {
            sb.append(mapData.get(headersCsv.get(i)));
            sb.append(i == headersCsv.size() - 1 ? "\n" : "#");
        }
        try {
            writeCsv(sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(DBAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void writeCsv(String csvStr) throws IOException {
        Date dateNow = new Date();
        SimpleDateFormat formatDateNow = new SimpleDateFormat("MM.dd.yyyy");
        File file = new File(System.getProperty("user.dir") + "/" + fileName + "_" + formatDateNow.format(dateNow) + ".csv");
        FileWriter fr = new FileWriter(file, true);
        BufferedWriter br = new BufferedWriter(fr);
        br.write(csvStr);
        
        br.close();
        fr.close();
    }
    
    public void writePagesToJson() throws IOException {
        
        JSONArray jsonArr = new JSONArray();
        
        for (Map<String, String> map : listMapPage) {
            JSONObject obj = new JSONObject(map);
            jsonArr.put(obj);
        }
        Date dateNow = new Date();
        SimpleDateFormat formatDateNow = new SimpleDateFormat("MM.dd.yyyy");
        File file = new File(System.getProperty("user.dir") + "/" + fileName + "_" + formatDateNow.format(dateNow) + ".json");
        FileWriter fr = new FileWriter(file, false);
        BufferedWriter br = new BufferedWriter(fr);
        
        br.write(jsonArr.toString());
        
        br.close();
        
        fr.close();
        
    }   
    
    public void uploadJsonToFtp(){
        if(countLastUpload==listMapPage.size()){
            return;
        }
        try {
            writePagesToJson();
        } catch (IOException ex) {
            Logger.getLogger(DBAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
        FtpUpload ftpUpload = new FtpUpload();
        Date dateNow = new Date();
      SimpleDateFormat formatDateNow = new SimpleDateFormat("MM.dd.yyyy");
        ftpUpload.uploadFile(System.getProperty("user.dir") + "/" + fileName + "_" + formatDateNow.format(dateNow) + ".json");
        countLastUpload = listMapPage.size();
    }
    
    
}
