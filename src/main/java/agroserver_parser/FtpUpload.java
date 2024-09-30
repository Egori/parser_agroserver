/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agroserver;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;

/**
 *
 * @author mumu
 */
public class FtpUpload {

//    String server = "fb7967y9.beget.tech";
//    int port = 21;
//    String user = "fb7967y9_traderb2b";
//    String pass = "traderb2b.com";
    String server = "95.214.63.130";
    int port = 21;
    String user = "traderb2b";
    String pass = "127488@daowW111";
    String fileName;
   
    public void uploadFile(String fileName) {
        String SFTPHOST = server;
        int SFTPPORT = 22;
        String SFTPUSER = user;
        String SFTPPASS = pass;
        String SFTPWORKINGDIR = "public_html/traderb2b.com/TRADER_B2B/cache/agroserver/";

        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        System.out.println("preparing the host information for sftp.");

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
            session.setPassword(SFTPPASS);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            System.out.println("Host connected.");
            channel = session.openChannel("sftp");
            channel.connect();
            System.out.println("sftp channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
            channelSftp.cd(SFTPWORKINGDIR);
            File f = new File(fileName);
            channelSftp.put(new FileInputStream(f), f.getName());
            System.out.println("File transfered successfully to host.");
        } catch (Exception ex) {
            System.out.println("Exception found while tranfer the response.");
        } finally {
            channelSftp.exit();
            System.out.println("sftp Channel exited.");
            channel.disconnect();
            System.out.println("Channel disconnected.");
            session.disconnect();
            System.out.println("Host Session disconnected.");
        }
    }

    public void _uploadFile(String filepath, String filename) {
        
       
        System.setProperty("jdk.tls.useExtendedMasterSecret", "false");
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

        FTPSClient ftpClient = new FTPSClient();

        try {

            ftpClient.connect(server);
            //ftpClient.login(user, pass);
            ftpClient.execPBSZ(0);
            ftpClient.execPROT("P");
            ftpClient.enterLocalPassiveMode();

            if (ftpClient.login(user, pass)) {
//               
//                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                System.out.println("Logged into FTP server successfully");
            } else {
                System.out.println("Failed log into FTP server");
                ftpClient.logout();
                ftpClient.disconnect();
                return;
            }

            //ftpClient.enterLocalPassiveMode();
            //ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // APPROACH #2: uploads second file using an OutputStream
            File localFile = new File(filepath);
            FileInputStream inputStream = new FileInputStream(localFile);
//
//            System.out.println("Start uploading file");
//            boolean done = ftpClient.storeFile("agroserver.json", inputStream);
//            inputStream.close();
//            if (done) {
//                System.out.println("The first file is uploaded successfully.");
//            }

            OutputStream outputStream = ftpClient.storeFileStream(filename);
            byte[] bytesIn = new byte[4096];
            int read = 0;

            while ((read = inputStream.read(bytesIn)) != -1) {
                outputStream.write(bytesIn, 0, read);
            }
            inputStream.close();
            outputStream.close();

            boolean completed = ftpClient.completePendingCommand();
            if (completed) {
                System.out.println("File is uploaded successfully.");
            }

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            System.out.println(ftpClient.getReplyCode());
            System.out.println(ftpClient.getReplyString());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();

                System.out.println(ftpClient.getReplyCode());
                System.out.println(ftpClient.getReplyString());
            }
        }
    }
}
