package com.fireawayh.main.cloudmusictoyunpan;

import com.fireawayh.cloudmusic.utils.ApiUtils;
import com.fireawayh.cloudmusic.utils.DownloadUtils;
import com.fireawayh.cloudmusic.utils.JsonUtils;
import com.fireawayh.cloudmusic.utils.MusicUtils;
import com.fireawayh.main.YunOffline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

        if(argList.contains("-u") && argList.contains("-p")) {
            String username = argList.get(argList.indexOf("-u") + 1).toString();
            String password = argList.get(argList.indexOf("-p") + 1).toString();
            YunOffline yo = new YunOffline(username, password);
            if(yo.initYunPan()){
                yo.getYunPanToken();
                switch (args[0]){
                    case "-playlist":
                        if(argList.contains("-playlist")) {
                            String playListId = argList.get(argList.indexOf("-playlist") + 1).toString().toUpperCase();
                            System.out.println("Saved Playlist " + playListId + " To Yun Pan");
                            saveListToYunPan(yo, playListId);
                        }
                        break;
                    case "-id":
                        if(argList.contains("-id")) {
                            String songId = argList.get(argList.indexOf("-id") + 1).toString().toUpperCase();
                            System.out.println("Saved Song " + songId + " To Yun Pan");
                            saveToYunPan(yo, songId);
                        }
                        break;
                }
            }else{
                System.err.println("Init Failed");
            }
        }
    }

    public static void saveToYunPan(YunOffline yo, String id){
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
        yo.saveToYunPan("", "");
//        if(!taskid.isEmpty()){
//            System.out.println("Saved TO Yun Pan, ID IS " + taskid);
//        }else{
//            System.out.println("Failed To Save To Yun Pan");
//        }
    }

    public static void saveListToYunPan(YunOffline yo, String listId){
        ArrayList<String> songIds = ju.getPlayListSongs(listId);
        for (int i = 0; i < songIds.size() - 1; i++){
            saveToYunPan(yo, songIds.get(i));
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
        System.out.println("-id <netease music id>");
        System.out.println("or");
        System.out.println("-playlist <netease music play list id>");

    }
}
