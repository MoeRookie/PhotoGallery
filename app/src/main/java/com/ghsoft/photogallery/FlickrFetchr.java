package com.ghsoft.photogallery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FlickrFetchr {
    /**
     * 从指定url获取原始数据并返回一个字节数组
     * @param urlSpec url参数
     * @return 字节数组
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        // 根据urlSpec创建url
        URL url = new URL(urlSpec);
        // 创建指向url地址的connection
        // 默认返回URLConnection对象
        // 但,要连接的是http URL对象;故要强转为HttpURLConnection对象
        // 另外,才能够调用其getInputStream()以及getResponseCode()方法
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // 获取输入流,此处才真正连接到了指定的url地址
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                ": with " +
                urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            // 循环读写字节数据
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            // 关闭网络连接
            connection.disconnect();
        }
    }

    /**
     * 从指定url获取最终字符串类型的网络数据
     * @param urlSpec url参数
     * @return 字符串类型的网络数据
     */
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }
}
