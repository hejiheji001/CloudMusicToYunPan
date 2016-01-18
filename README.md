# CloudMusicToYunPan

# Useage:

###java -jar xxx.jar -u [your baidu username] -p [your baidu password] [options]
###java -jar xxx.jar -conf [userinfo.properties] [options]

# Options
### -r rename.txt 
#### auto rename downloaded task (based on path and file name)
>PS1: rename.txt will auto generate when you add some tasks into baidu yun

>PS2: you can change any file you like, just edit the txt file, specify the right path and name

### -id [netease music id]
#### add an offline task of this song to your baidu yun, 320K prefered

### -playlist [netease playlist id]
#### add some offline tasks of all the songs in this playlist to your baidu yun, 320K prefered

### -path [save path of your baidu yun]
#### default value is /
>PS: new folder will be created if does not exists
