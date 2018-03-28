package com.bizideal.mn.core;

import com.bizideal.mn.utils.HttpClientUtils;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: liulq
 * @Date: 2018/3/23 12:33
 * @Description:
 * @version: 1.0
 */
public class OscCrawler {

    private static Logger logger = LoggerFactory.getLogger(OscCrawler.class);

    static String cookie = "";
    static long time = 0l;
    static Map<String, String> map = new HashMap();

    // 登录时密码加密方式为sha1s
    // 登录获取cookie，cookie有效时间设置为10分钟
    public static void login() throws Exception {
        if (System.currentTimeMillis() - time < 10 * 60 * 1000) {
            return; // 10分钟内不做登录
        }
        String userName = "15172492711";
        String pwd = "8a923cbfdb5c9df22ed05e73be790b18b0b6bfcc";
        String loginurl = "https://www.oschina.net/action/user/hash_login";
        String outputStr = "email=" + userName + "&pwd=" + pwd + "&verifyCode=&save_login=1";
        JsonObject post = HttpClientUtils.httpRequest(loginurl, "POST", outputStr, cookie);
        cookie = post.get("cookie").getAsString();
        map.clear();
        if (StringUtils.isNotBlank(cookie)) {
            logger.info("获取cookie成功，cookie:{}", cookie);
            for (String cookie : StringUtils.split(cookie, ";")) {
                cookie = cookie.trim();
                if (cookie.contains("=")) {
                    String[] split = StringUtils.split(cookie, "=");
                    map.put(split[0], split[1]);
                }
            }
        }
        time = System.currentTimeMillis();
    }

    public static void main(String[] args) throws Exception {
        login();
        HttpClientUtils.httpRequest("https://www.oschina.net", "GET", null, cookie);
    }

    // 点赞接口
    // user_code登录后获得，这里是直接写死了
    public static void userMarkLove(String logid, String currentLoveCount, String ownerOfLog, String username) throws Exception {
        String url = "https://www.oschina.net/action/tweet/makeAsLove";
        // user_code=yIVgLn2ceyHt7wMbeSIcbBkYoeljorp5ToMhkEtA&logid=16787208&current_love_count=0&ownerOfLog=988298&clickCount=0
        String user_code = "U55EtlMgp5085Fv16lRLOnoywyIkk0dfnYjHWFJr";
        String outputStr = "user_code=" + user_code + "&logid=" + logid + "&current_love_count=" + currentLoveCount + "&ownerOfLog=" + ownerOfLog + "&clickCount=0";
        JsonObject response = HttpClientUtils.httpRequest(url, "POST", outputStr, cookie);
        if ("200".equals(response.get("code").getAsString())) {
            logger.info("用户id:{}，昵称:{}的动弹:{},点赞成功", ownerOfLog, username, logid);
        } else if ("201".equals(response.get("code").getAsString())) {
            logger.info("用户id:{}，昵称:{}的动弹:{},取消赞成功", ownerOfLog, username, logid);
        } else {
            logger.info("用户id:{}，昵称:{}的动弹:{},操作失败，原因:{}", ownerOfLog, username, logid, response.get("msg").getAsString());
        }
    }

    static Pattern userIdPatternA = Pattern.compile(".*/u/(\\d*)"); // a标签获取userId
    static Pattern userIdPatternImg = Pattern.compile(".*/user/\\d*/(\\d*).*");// img标签获取userId
    static Pattern logIdPattern = Pattern.compile(".*/tweet/(\\d*)");// a标签获取logId

    // 启动。获取动弹列表
    public static void start() throws Exception {
        login();
        Document document = Jsoup.connect("https://www.oschina.net/tweets").cookies(map).method(Connection.Method.GET).get();
        Elements select = document.select("div.box.tweetitem");
        Matcher matcher;
        String ownerOfLog = null;
        String logid = null;
        String currentLoveCount;
        for (Element element : select) {
            String username = element.select("a.ti-uname").get(0).text();
            Element span = element.select("span.box.vertical.tiicon").get(0); // 点赞的span
            if ("点赞".equals(span.attr("title"))) {
                Elements userA = element.select("a.portrait"); // 用户头像a标签，上面有用户ID
                if ((matcher = userIdPatternA.matcher(userA.attr("href"))).matches()) {
                    ownerOfLog = matcher.group(1);
                } else if ((matcher = userIdPatternImg.matcher(userA.select("img").get(0).attr("src"))).matches()) {
                    ownerOfLog = matcher.group(1);
                }
                Elements detailA = element.select("span.box.vertical.tiicon a"); // 查看详情a标签，上面有动弹ID
                if ((matcher = logIdPattern.matcher(detailA.attr("href"))).matches()) {
                    logid = matcher.group(1);
                }
                currentLoveCount = span.attr("count");
                userMarkLove(logid, currentLoveCount, ownerOfLog, username);
            }
        }

    }

}
