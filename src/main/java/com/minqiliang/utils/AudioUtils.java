package com.minqiliang.utils;

import jmp123.PlayBack;

public class AudioUtils {

    public static void play(String url) throws Exception{
        PlayBack player = new PlayBack(new jmp123.output.Audio());
        player.open(url,"");
        player.start(true);
    }
}