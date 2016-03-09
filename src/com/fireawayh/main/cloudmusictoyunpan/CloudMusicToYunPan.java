package com.fireawayh.main.cloudmusictoyunpan;

import com.fireawayh.cloudmusic.utils.ApiUtils;
import com.fireawayh.cloudmusic.utils.DownloadUtils;
import com.fireawayh.cloudmusic.utils.JsonUtils;
import com.fireawayh.cloudmusic.utils.MusicUtils;
import com.fireawayh.main.YunOffline;
import com.fireawayh.util.IOUtils;
import com.fireawayh.util.ShowGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * By FireAwayH on 17/01/2016.
 */
public class CloudMusicToYunPan {
    private static DownloadUtils du = new DownloadUtils();
    private static JsonUtils ju = new JsonUtils();
    private static ApiUtils au = new ApiUtils();
    private static IOUtils iou = new IOUtils();


    public static void main(String[] args){
        List<String> argList = Arrays.asList(args);

        if(argList.isEmpty()){
//            test();
            showGUI();
        }

        String username = "";
        String password = "";
        String path = "/";

        if(argList.contains("-u") && argList.contains("-p")) {
            username = argList.get(argList.indexOf("-u") + 1);
            password = argList.get(argList.indexOf("-p") + 1);
        }

        if(argList.contains("-conf")){
            try {
                String filename = argList.get(argList.indexOf("-conf") + 1);
//                filename = "userinfo.properties";
                Properties prop = new Properties();
                File file = new File(filename);
                if (file.exists()) {
                    prop.load(new FileInputStream(file));
                    username = prop.getProperty("Username");
                    password = prop.getProperty("Password");
                    path = prop.getProperty("Savepath");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(!username.isEmpty() && !password.isEmpty()){
            init(username, password, argList, path);
        }
    }

    private static void showGUI() {
        TextField idText = new TextField();
        Frame frame = new Frame("Cloud Music Downloader");
        Dialog dialog = new Dialog(frame, "Hint", true);
        idText.setColumns(15);
        DefaultComboBoxModel<String> typeSelector = new DefaultComboBoxModel<>();
        typeSelector.addElement("Music ID");
        typeSelector.addElement("Playlist ID");
        JComboBox<String> idType = new JComboBox<>(typeSelector);
        Button start = new Button("Start!");
        frame.add(idType);
        frame.add(idText);
        frame.add(start);
        frame.setLayout(new FlowLayout());
        frame.setSize(250, 100);
        frame.setResizable(false);
        frame.setLocation(getCenter(frame));
        frame.setVisible(true);
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
        start.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Label l = new Label();
                        l.setAlignment(Label.CENTER);
                        dialog.setSize(150, 50);
                        dialog.setLocation(getCenter(dialog));
                        dialog.addWindowListener(
                                new WindowAdapter() {
                                    @Override
                                    public void windowClosing(WindowEvent e) {
                                        dialog.removeAll();
                                        dialog.dispose();
                                    }
                                }
                        );
                        String songId = idText.getText();
                        if (songId.isEmpty()) {
                            l.setText("Music id or Playlist id is required");
                            dialog.add(l);
                            dialog.setVisible(true);
                            return;
                        }

                        start.setLabel("Working");
                        start.setEnabled(false);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ShowGUI s = new ShowGUI();

                                if (idType.getSelectedItem().equals("Music ID")) {
                                    String durl = getDURLById(songId);
                                    s.showGui(durl);
                                }
                                if (idType.getSelectedItem().equals("Playlist ID")) {
                                    ArrayList<String> songIds = ju.getPlayListSongs(songId);
                                    ArrayList<String> durls = new ArrayList<>();
                                    for (int i = 0; i < songIds.size() - 1; i++) {
                                        String durl = getDURLById(songIds.get(i));
                                        durls.add(durl);
                                    }
                                    s.showGui(durls);
                                }

                                start.setLabel("Start");
                                start.setEnabled(true);
                            }
                        }).start();
                    }
                }
        );
    }

    private static Point getCenter(Window f) {
        int windowWidth = f.getWidth();                    //获得窗口宽
        int windowHeight = f.getHeight();
        Toolkit kit = Toolkit.getDefaultToolkit();             //定义工具包
        Dimension screenSize = kit.getScreenSize();            //获取屏幕的尺寸
        int screenWidth = screenSize.width;                    //获取屏幕的宽
        int screenHeight = screenSize.height;
        return new Point(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);
    }

    public static void init(String username, String password, List<String> argList, String path) {
        YunOffline yo = new YunOffline(username, password);
        if (yo.initYunPan()) {
            yo.getYunPanToken();
            if (argList.contains("-r")) {
                String source = argList.get(argList.indexOf("-r") + 1).toUpperCase();
                rename(yo, source);
            }

            if (argList.contains("-path")) {
                path = argList.get(argList.indexOf("-path") + 1).toUpperCase();
            }

            if (argList.contains("-playlist")) {
                String playListId = argList.get(argList.indexOf("-playlist") + 1).toUpperCase();
                System.out.println("Saved Playlist " + playListId + " To Yun Pan");
                saveListToYunPan(yo, playListId, path);
            }
            if (argList.contains("-id")) {
                String songId = argList.get(argList.indexOf("-id") + 1).toUpperCase();
                System.out.println("Saved Song " + songId + " To Yun Pan");
                saveToYunPan(yo, songId, path);
            }
        } else {
            System.err.println("Init Failed");
        }
    }

    public static void rename(YunOffline yo, String filename){
        String[] content = iou.getStringFromFile(filename).split("Item:");
        for(String c : content){
            if(!c.isEmpty()) {
                String[] info = c.split(",");
                String path = info[0].split("=")[1];
                String oldName = info[1].split("=")[1];
                String newName = info[2].split("=")[1];
                yo.renameFile(path, oldName, newName);
            }
        }
    }

    public static void saveToYunPan(YunOffline yo, String id, String path){
        MusicUtils mu = new MusicUtils(id);
        Map<String, Object> music = mu.getBestMusic().object();
        String artistName = mu.getArtist();
        String songName = mu.getSongName();
        String bestMusicId = music.get("dfsId").toString();
        String fileName = artistName + " - " + songName  + "." + music.get("extension");
//        String newFileName = stringConvert(fileName);
        String durl = au.getDownloadUrl(bestMusicId);
        Pattern p = Pattern.compile("/([0-9]*.mp3)");
        Matcher matcher = p.matcher(durl);
        String oldFileName = "";
        if (matcher.find()) {
            oldFileName = matcher.group(1);
        }

        System.out.println("Current task: " + fileName);
        yo.setSource_url(durl);
        yo.setSavepath(path);
        yo.saveToYunPan("", "");
        iou.appendStringToFile("Item:path=" + path + ",old=" + oldFileName + ",new=" + fileName + "\r\n", "rename.txt");
//        if(!taskid.isEmpty()){
//            System.out.println("Saved TO Yun Pan, ID IS " + taskid);
//        }else{
//            System.out.println("Failed To Save To Yun Pan");
//        }
    }

    public static void saveListToYunPan(YunOffline yo, String listId, String path){
        ArrayList<String> songIds = ju.getPlayListSongs(listId);
        for (int i = 0; i < songIds.size() - 1; i++){
            saveToYunPan(yo, songIds.get(i), path);
        }
    }

//    TODO
    public static String stringConvert(String str) {
        try {
            String fileEncode = System.getProperty("file.encoding");
            return new String(str.getBytes("UTF-8"), fileEncode);
        }catch (Exception e){
            return "?";
        }
    }

    public static String getDURLById(String id) {
        MusicUtils mu = new MusicUtils(id);
        Map<String, Object> music = mu.getBestMusic().object();
        String bestMusicId = music.get("dfsId").toString();
        return au.getDownloadUrl(bestMusicId);
    }

    public static void test(){
//        YunOffline yo = new YunOffline("hejiheji001@163.com", "MyLifeForFire0");
//////        yo.setPanToken("ee7f2c5c90a95e5c4ac6425f21a994d1");
//        if(yo.initYunPan()) {
//            yo.getYunPanToken();
////            saveListToYunPan(yo, "41370921");
//            rename(yo, "rename.txt");
//        }
//
        System.out.println("-u <baidu yun username> -p <baidu yun password> [options]");
        System.out.println("-c <userinfo.properties> [options]");
        System.out.println("-u <baidu yun username> -p <baidu yun password> [options]");
        System.out.println("-path <save path of baidu yun>");
        System.out.println("-id <netease music id>");
        System.out.println("or");
        System.out.println("-playlist <netease music play list id>");

    }
}
