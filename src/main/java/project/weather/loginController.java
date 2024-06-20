package project.weather;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Controller

public class loginController {

    @GetMapping("/login")
    public String test2(Model model) {
        model.addAttribute("kakaoApiKey", "ca910a38c4c701f2ded1026e3f8af74f");
//        model.addAttribute("redirectUri","https://getpostman.com/oauth2/callback");  // postman 작업시 변경
        model.addAttribute("redirectUri", "http://localhost:7700/login/oauth2/kakao/callback");
        return "login";
    }
    @GetMapping("/login/oauth2/kakao/callback")
    public HashMap<String, Object> kakaoLogin(@RequestParam String code) {
        String accessToken = getAccessToken(code);
        HashMap<String, Object> userInfo = getUserInfo(accessToken);
        return userInfo;
    }

    @GetMapping("/login/oauth2/github/callback")
    public HashMap<String, Object> githubLogin(@RequestParam String code) {
        String accessToken = getAccessToken(code);
        HashMap<String, Object> userInfo = getUserInfo(accessToken);
        return userInfo;
    }


    public String getAccessToken(String code){
        String accessToken = "";
        String refreshToken = "";
        String reqUrl = "https://kauth.kakao.com/oauth/token";
        String kakaoApiKey ="ca910a38c4c701f2ded1026e3f8af74f";
        String kakaoRedirectUri ="http://localhost:7700/login/oauth2/kakao/callback";

        try{
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //필수 헤더 세팅
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setDoOutput(true); //OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();

            //필수 쿼리 파라미터 세팅
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=").append(kakaoApiKey);
            sb.append("&redirect_uri=").append(kakaoRedirectUri);
            sb.append("&code=").append(code);

            bw.write(sb.toString());
            bw.flush();

            int responseCode = conn.getResponseCode();

            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String line = "";
            StringBuilder responseSb = new StringBuilder();
            while((line = br.readLine()) != null){
                responseSb.append(line);
            }
            String result = responseSb.toString();
            // jackson objectmapper 객체 생성
            ObjectMapper objectMapper = new ObjectMapper();
            // JSON String -> Map
            Map<String, Object> jsonMap = objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {
            });

            accessToken = jsonMap.get("access_token").toString();
            refreshToken = jsonMap.get("refresh_token").toString();

            System.out.println("access_token : " + accessToken);
            System.out.println("refresh_token : " + refreshToken);

            br.close();
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return accessToken;
    }

    public HashMap<String, Object> getUserInfo(String accessToken){
        HashMap<String, Object> userInfo = new HashMap<String, Object>();
        String reqURL = "https://kapi.kakao.com/v2/user/me";
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 요청에 필요한 Header에 포함될 내용
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);
            System.out.println("result type" + result.getClass().getName()); // java.lang.String

            try {
                // jackson objectmapper 객체 생성
                ObjectMapper objectMapper = new ObjectMapper();
                // JSON String -> Map
                Map<String, Object> jsonMap = objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {
                });

                System.out.println(jsonMap.get("properties"));

                Map<String, Object> properties = (Map<String, Object>) jsonMap.get("properties");
//                Map<String, Object> kakao_account = (Map<String, Object>) jsonMap.get("kakao_account");

                // System.out.println(properties.get("nickname"));
                // System.out.println(kakao_account.get("email"));

                String nickname = properties.get("nickname").toString();
                String profile_image = properties.get("profile_image").toString();

                userInfo.put("nickname", nickname);
                userInfo.put("profile_image", profile_image);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return userInfo;
    }
}
