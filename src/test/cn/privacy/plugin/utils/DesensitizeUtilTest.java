package cn.privacy.plugin.utils;

import org.junit.jupiter.api.Test;

class DesensitizeUtilTest {

    @Test
    void replaceSecretInfo() {
        System.out.println(DesensitizeUtil.replaceSecretInfo(
            "123123123123", "*"
        ));
    }
}