package project.weather;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class weatherAPI {

//    @GetMapping("/weather")
    public String weatherInfo(@RequestParam("nx") String nx, @RequestParam("ny") String ny)
    {
        LocalDateTime t = LocalDateTime.now().minusMinutes(30); // 현재 시각 30분전
        Map rData = null;
        HashMap<String, Object> result = new HashMap<String, Object>();
        String key = "WCgfH5NwN2TOyTIE5t8tOUXP9R8hezYg7JLzXZLY%2FIGSW5L0i1bUffHA4MT3AtUcBQ5AFXRMLNXk5phxUw1YTA%3D%3D";  // 서비스 키값
        List<Map> finalFailedMovieList = new ArrayList<>();
        try {

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getFcstVersion";

            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url + "?" + "serviceKey=" + key + "&dataType=JSON"
                                + "&base_date=" + t.format(DateTimeFormatter.ofPattern("yyyyMMdd"))  // 발표 날짜
                                + "&base_time=" + t.format(DateTimeFormatter.ofPattern("HHmm"))
                                + "&nx=" + nx + "&ny=" + ny // 발표 시각
                                ).build();

            System.out.println("url값 : " + uri);

            HashMap<String, Object> resultMap;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            result.put("statusCode", e.getRawStatusCode());
            result.put("body", e.getStatusText());
            System.out.println(e.toString());

        } catch (Exception e) {
            result.put("statusCode", "999");
            result.put("body", "excpetion오류");
            System.out.println(e.toString());
        }
        return "Test";
    }
}
