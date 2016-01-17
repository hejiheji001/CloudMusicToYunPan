package com.fireawayh.main.cloudmusictoyunpan;

import com.fireawayh.cloudmusic.utils.ApiUtils;
import com.fireawayh.cloudmusic.utils.DownloadUtils;
import com.fireawayh.cloudmusic.utils.JsonUtils;
import com.fireawayh.cloudmusic.utils.MusicUtils;
import com.fireawayh.main.YunOffline;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Created by FireAwayH on 17/01/2016.
 */
public class CloudMusicToYunPan {
    private static DownloadUtils du = new DownloadUtils();
    private static JsonUtils ju = new JsonUtils();
    private static ApiUtils au = new ApiUtils();

    public static void main(String[] args){
        List argList = Arrays.asList(args);

        if(argList.isEmpty()){
            test();
        }

        String username = "";
        String password = "";
        String path = "/";

        if(argList.contains("-u") && argList.contains("-p")) {
            username = argList.get(argList.indexOf("-u") + 1).toString();
            password = argList.get(argList.indexOf("-p") + 1).toString();
        }

        if(argList.contains("-conf")){
            try {
                String filename = argList.get(argList.indexOf("-conf") + 1).toString();
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
            YunOffline yo = new YunOffline(username, password);
            if(yo.initYunPan()){
                yo.getYunPanToken();
                if(argList.contains("-path")) {
                    path = argList.get(argList.indexOf("-path") + 1).toString().toUpperCase();
                }

                if(argList.contains("-playlist")) {
                    String playListId = argList.get(argList.indexOf("-playlist") + 1).toString().toUpperCase();
                    System.out.println("Saved Playlist " + playListId + " To Yun Pan");
                    saveListToYunPan(yo, playListId, path);
                }
                if(argList.contains("-id")) {
                    String songId = argList.get(argList.indexOf("-id") + 1).toString().toUpperCase();
                    System.out.println("Saved Song " + songId + " To Yun Pan");
                    saveToYunPan(yo, songId, path);
                }
            }else{
                System.err.println("Init Failed");
            }
        }
    }

    public static void saveToYunPan(YunOffline yo, String id, String path){
        MusicUtils mu = new MusicUtils(id);
        Map music = mu.getBestMusic().object();
        String artistName = mu.getArtist();
        String songName = mu.getSongName();
        String bestMusicId = music.get("dfsId").toString();
        String fileName = artistName + " - " + songName  + "." + music.get("extension");
        String newFileName = stringConvert(fileName);
        String durl = au.getDownloadUrl(bestMusicId);
        System.out.println("Current task: " + newFileName);
        yo.setSource_url(durl);
        yo.setSavepath(path);
        yo.saveToYunPan("", "");
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
            String result = new String(str.getBytes("UTF-8"), fileEncode);
            return result;
        }catch (Exception e){
            return "?";
        }
    }

    public static void test(){
//        YunOffline yo = new YunOffline("@163.com", "");
////        yo.setPanToken("ee7f2c5c90a95e5c4ac6425f21a994d1");
//        if(yo.initYunPan()) {
//            yo.getYunPanToken();
//            saveListToYunPan(yo, "41370921");
//        }
        System.out.println("-u <baidu yun username> -p <baidu yun password> [options]");
        System.out.println("-c <userinfo.properties> [options]");
        System.out.println("-u <baidu yun username> -p <baidu yun password> [options]");
        System.out.println("-path <save path of baidu yun>");
        System.out.println("-id <netease music id>");
        System.out.println("or");
        System.out.println("-playlist <netease music play list id>");

    }
}
