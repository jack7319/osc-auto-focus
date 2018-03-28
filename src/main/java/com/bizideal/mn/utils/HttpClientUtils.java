package com.bizideal.mn.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * http请求工具类
 *
 * @author pc
 */
public class HttpClientUtils {

    /**
     * 发起https请求并获取结果
     *
     * @param requestUrl    请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr     提交的数据
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JsonObject httpRequest(String requestUrl,
                                         String requestMethod, String outputStr, String cookie) throws Exception {
        JsonObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        OutputStream outputStream = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        HttpsURLConnection httpUrlConn = null;
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = {new HttpsX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            httpUrlConn = (HttpsURLConnection) url.openConnection();
            // 模拟浏览器行为
            httpUrlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36)");

            if (StringUtils.isNotBlank(cookie)) {
                httpUrlConn.setRequestProperty("Cookie", cookie);
            }
//			httpUrlConn.setRequestProperty("Cookie", "_reg_key_=_reg_key_&_user_behavior_=b05c5203-3e65-49b1-943b-f0a6adce60c0&aliyungf_tc=AQAAALeEKSGvdwsAsAFAcBBaPZ11M76m");
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);

            // 设置请求方式（GET/POST）

            httpUrlConn.setRequestMethod(requestMethod);

            if ("GET".equalsIgnoreCase(requestMethod))
                httpUrlConn.connect();

            // 当有数据需要提交时
            if (null != outputStr) {
                outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串
            httpUrlConn.setInstanceFollowRedirects(false);
            inputStream = httpUrlConn.getInputStream();
            inputStreamReader = new InputStreamReader(
                    inputStream, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            String headerField = httpUrlConn.getHeaderField("Set-Cookie");
            httpUrlConn.disconnect();
            if (StringUtils.isBlank(buffer.toString())) {
                jsonObject = new JsonObject();
            } else {
                jsonObject = (JsonObject) new JsonParser().parse(buffer.toString());
            }
            jsonObject.addProperty("cookie", headerField);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (httpUrlConn != null) {
                httpUrlConn.disconnect();
            }
        }
        return jsonObject;
    }

}