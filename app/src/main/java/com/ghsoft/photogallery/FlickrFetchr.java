package com.ghsoft.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";
    public static final String API_KEY = "adccfea42ef1829e72843c4d3e17d6a7";
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

    /**
     * 构建请求URL并获取内容
     */
    public List<GalleryItem> fetchItems(){
        ArrayList<GalleryItem> items = new ArrayList<>();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
            Log.i(TAG, "Received JSON: " + jsonString);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }

    /**
     * 解析json,生成galleryItem集合
     * @param items galleryItem集合
     * @param jsonBody json
     */
    public void parseItems(List<GalleryItem> items,JSONObject jsonBody)
            throws JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            if (!photoJsonObject.has("url_s")) {
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }
}
