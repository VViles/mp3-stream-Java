package com.kanmenzhu.ipaudio.ipaudioaclient;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kanmenzhu.ipaudio.ipaudioaclient.network.TcpClient;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 简单测试类
 */
public class Sample {
    private static final Logger logger = LoggerFactory.getLogger(Sample.class);

    /**
     * 发一个mp3测试
     */
    public static void sendMp3(int num) {
        String seedCode = "seed_code";
        JSONObject connectCmd = new JSONObject();
        connectCmd.put("cmd", "PLAYLIST");
        connectCmd.put("ulevel", 99);
        connectCmd.put("plevel", 2);
        connectCmd.put("Umask", "test");
        connectCmd.put("Umagic", 3);
        JSONArray snList = new JSONArray();
        snList.add(seedCode);
        connectCmd.put("snlist", snList);

        String data = connectCmd.toJSONString();


        //发送mp3
        String mp3 = "C:\\Users\\Administrator\\Desktop\\1\\tmp\\20180622001.mp3";

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();

             BufferedInputStream bis = new BufferedInputStream(new FileInputStream(mp3))) {

            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = bis.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            byte[] allData = baos.toByteArray();
            Map<String, Integer> header = parseHeader(Arrays.copyOfRange(allData, 0, 24));
            int[] packageLenArr = parseAllPackageLen(Arrays.copyOfRange(allData, header.get("LEN") + 24,
                    allData.length), header.get("PACKAGE_COUNT"));
            if(packageLenArr.length<num){
            	return;
            }
            //ByteBuf byteBuf= allData;
            TcpClient.sendByteArray((data.length() + "\n" + data).getBytes(StandardCharsets.UTF_8));
            logger.info("send {} bytes mp3 audio to server", allData.length);
            sendAudio(allData, packageLenArr, header.get("DELAY"),num);


        } catch (Exception e) {
            logger.error("send failed", e);
        }

    }

    private static Map<String, Integer> parseHeader(byte[] header) {

        short[] lenArr = removeSign(Arrays.copyOfRange(header, 8, 12));

        short[] pkgCountArr = removeSign(Arrays.copyOfRange(header, 12, 16));

        short[] delayArr = removeSign(Arrays.copyOfRange(header, 16, 20));

        int len = (lenArr[0] & 0xff) << 24 | (lenArr[1] & 0xff) << 16 | (lenArr[2] & 0xff) << 8 | lenArr[3];
        int packageCount = (pkgCountArr[0] & 0xff) << 24 | (pkgCountArr[1] & 0xff) << 16 | (pkgCountArr[2] & 0xff) << 8 | pkgCountArr[3];
        int dalayMs = (delayArr[0] & 0xff) << 24 | (delayArr[1] & 0xff) << 16 | (delayArr[2] & 0xff) << 8 | delayArr[3];
        Map<String, Integer> headerMap = new HashMap<>(3, 1.0f);
        headerMap.put("LEN", len);
        headerMap.put("PACKAGE_COUNT", packageCount);
        headerMap.put("DELAY", dalayMs);
        return headerMap;
    }

    private static int[] parseAllPackageLen(byte[] allPackageHeader, int packageCount) {
        short[] noSignData = removeSign(allPackageHeader);
        int[] packageLenArr = new int[packageCount];
        for (int i = 0; i < packageCount * 2; i += 2) {
            packageLenArr[i / 2] = (noSignData[i] & 0xff) << 8 | noSignData[i + 1] & 0xff;
        }
        return packageLenArr;
    }

    private static void sendAudio(byte[] allData, int[] packageLenArr, int delayMs,int num) {
        int pos = 24;
        for(int i=0;i<packageLenArr.length;i++){
        	int pkgLen = packageLenArr[i];
        	if((i+1)>=num||num==-1){
        		logger.info("send {} bytes to server", pkgLen);
                //TcpClient.sendByteArray(Arrays.copyOfRange(allData, pos, pos + pkgLen));
                TcpClient.sendByteArray(Arrays.copyOfRange(allData, pos, pos + pkgLen));
                try {
                    TimeUnit.MILLISECONDS.sleep(150);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        	}
        	pos += pkgLen;
        }
    }

    private static short[] removeSign(byte[] origin) {

        short[] noSignByteArr = new short[origin.length];
        for (int i = 0; i < origin.length; i++) {
            if (origin[i] < 0) {
                noSignByteArr[i] = (short) (origin[i] + 256);
            } else {
                noSignByteArr[i] = origin[i];
            }
        }
        return noSignByteArr;
    }
}
