package com.minqiliang;

import com.minqiliang.utils.AudioUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReggieTakeOutApplicationTests {

    @Test
    void contextLoads() {
        // 播放语音提示
        AudioUtils.play("D:\\图片\\reggie\\audio\\reggie.mp3");
    }

}
