package project.weather;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class ApiExplorer {

    private final WeatherDTO weatherdto;

    LocalDateTime now = LocalDateTime.now();
    String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    // 열거형으로 정의 후 사용
//    enum WeatherValue {
//        PTY, REH, RN1, T1H, POP, SKY
//    }

    public List<WeatherDTO> getWeatherInfo(double nx, double ny) throws IOException, JSONException {

        LocalDateTime yesterday = now.minusDays(1);
        String nxString = String.format("%.0f", nx);
        String nyString = String.format("%.0f", ny);
        String baseTime = null;
        int hour = now.getHour();
        if(hour >= 23 || hour < 2) {
            if(hour != 23)
            {baseDate = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));}
            baseTime = "2300";
        } else if(hour >= 2 || hour < 5) {
            baseTime = "0200";
        } else if(hour >= 5 || hour < 8) {
            baseTime = "0500";
        } else if(hour >= 8 || hour < 11) {
            baseTime = "0800";
        } else if(hour >= 11 || hour < 14) {
            baseTime = "1100";
        } else if (hour >= 14 || hour < 17) {
            baseTime = "1400";
        } else if(hour >= 17 || hour < 20) {
            baseTime = "1700";
        } else if(hour >= 20 || hour < 23) {
            baseTime = "2000";
        }
        String type = "JSON";
        //         홈페이지에서 받은 키
        String serviceKey = "WCgfH5NwN2TOyTIE5t8tOUXP9R8hezYg7JLzXZLY%2FIGSW5L0i1bUffHA4MT3AtUcBQ5AFXRMLNXk5phxUw1YTA%3D%3D";

        //		참고문서에 있는 url주소
        String apiUrl = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8")); /* 조회하고싶은 날짜*/
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8")); /* 조회하고싶은 시간 AM 02시부터 3시간 단위 */
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nxString, "UTF-8")); //경도
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(nyString, "UTF-8")); //위도

        /*
         * GET방식으로 전송해서 파라미터 받아오기
         */
        URL url = new URL(urlBuilder.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }

        rd.close();
        conn.disconnect();
        String result = sb.toString();
        List<WeatherDTO> weatherDTOList= parseJSON(result);
        return weatherDTOList;
    }

    private List<WeatherDTO> parseJSON(String jsonString) throws JSONException {
        List<WeatherDTO> weatherList = null;
        try {
            if (jsonString == null || jsonString.isEmpty()) {
                System.out.println("JSON 문자열이 null이거나 비어 있습니다.");
            }
            JSONObject jsonObj = new JSONObject(jsonString);
            JSONObject response = jsonObj.getJSONObject("response");
            JSONObject body = response.getJSONObject("body");
            JSONObject items = body.getJSONObject("items");
            JSONArray itemArray = items.getJSONArray("item");
            String fcstValue;
            weatherList = new ArrayList<>();
            // item 배열에서 각 항목을 출력 (예시)
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject item = itemArray.getJSONObject(i);
                String fcstDate = item.getString("fcstDate");
                String fcstTime = item.getString("fcstTime");
                String category = item.getString("category");
                fcstValue = item.getString("fcstValue");

                // fcstDate와 fcstTime을 LocalDateTime 객체로 변환
                LocalDateTime forecastDateTime = LocalDateTime.parse(fcstDate + fcstTime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

                // fcstDate가 현재 날짜와 같고 fcstTime이 현재 시간보다 이전이지만 1시간 차이 이내이면 포함
                if (forecastDateTime.isBefore(now) && forecastDateTime.plusHours(1).isBefore(now)) {
                    continue;
                }

                WeatherDTO weatherdto = findOrCreateWeatherDTO(weatherList, fcstDate, fcstTime);

                switch (category) {
                    case "PTY":  // 강수형태
                        weatherdto.setPTY(fcstValue);
                        break;
                    case "REH":  // 습도
                        weatherdto.setREH(fcstValue);
                        break;
                    case "RN1":  // 1시간 강수량 범주(1mm)
                        weatherdto.setRN1(fcstValue);
                        break;
                    case "TMP":  // 1시간 기온
                        weatherdto.setTMP(fcstValue);
                        break;
                    case "POP":  // 강수확률
                        weatherdto.setPOP(fcstValue);
                        break;
                    case "SKY":  // 하늘상태
                        weatherdto.setSKY(fcstValue);
                        break;
                    case "SNO":  // 적설량 (cm)
                        weatherdto.setSNO(fcstValue);
                        break;
                    case "TMN":  // 일 최저기온(℃)
                        weatherdto.setTMN(fcstValue);
                        break;
                    case "TMX":  // 일 최고기온(℃)
                        weatherdto.setTMX(fcstValue);
                        break;
                    default:
                        break;
                }
                // DTO가 리스트에 없으면 새로 추가
                if (!weatherList.contains(weatherdto)) {
                    weatherList.add(weatherdto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weatherList;
    }

    private static WeatherDTO findOrCreateWeatherDTO(List<WeatherDTO> weatherList, String fcstDate, String fcstTime) {
        for (WeatherDTO dto : weatherList) {
            if (dto.getFcstDate().equals(fcstDate) && dto.getFcstTime().equals(fcstTime)) {
                return dto;
            }
        }
        WeatherDTO newDto = new WeatherDTO();
        newDto.setFcstDate(fcstDate);
        newDto.setFcstTime(fcstTime);
        return newDto;
    }
}

//    public void getWeatherInfo2(Double nx, Double ny) throws IOException, ParserConfigurationException, SAXException {
//
//        LocalDateTime t = LocalDateTime.now();
//        String nxString = String.format("%.0f", nx);
//        String nyString = String.format("%.0f", ny);
//        String 서비스키 = "WCgfH5NwN2TOyTIE5t8tOUXP9R8hezYg7JLzXZLY%2FIGSW5L0i1bUffHA4MT3AtUcBQ5AFXRMLNXk5phxUw1YTA%3D%3D";
//
//        StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst"); /*URL*/
//        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + 서비스키); /*Service Key*/
//        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
//        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8")); /*한 페이지 결과 수*/
//        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
//        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(t.format(DateTimeFormatter.ofPattern("yyyyMMdd")), "UTF-8")); /*금일*/
//        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(t.format(DateTimeFormatter.ofPattern("HHmm")), "UTF-8")); /*06시 발표(정시단위) */
//        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nxString, "UTF-8")); /*예보지점의 X 좌표값*/
//        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(nyString, "UTF-8")); /*예보지점의 Y 좌표값*/
//
//        URL url = new URL(urlBuilder.toString());
//
//        // HTTP연결
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("GET");
//        conn.setRequestProperty("Content-type", "application/json");
//
//        // 응답 확인
//        BufferedReader rd;
//        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
//            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//        } else {
//            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
//        }
//
//        // 응답 문자열로 읽기
//        StringBuilder sb = new StringBuilder();
//        String line;
//        while ((line = rd.readLine()) != null) {
//            sb.append(line);
//        }
//        rd.close();
//        conn.disconnect();
//
//        String result = sb.toString();
//        System.out.println(sb.toString());
//
//        // XML 파싱
//        String xmlString = sb.toString();
//        parseXML(xmlString);

//        // 문자열 Document 로 변경해서 List 형태로 가져와서 객체에 파싱함.
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        DocumentBuilder db = dbf.newDocumentBuilder();
//
//        InputSource is = new InputSource(new StringReader(result));
//        Document document = db.parse(is);
//        document.getDocumentElement().normalize();
//        Double value;
//
//        // item을 기준으로 document를 구분 지음
//        NodeList items = document.getElementsByTagName("item");
//        for (int i = 0; i < items.getLength(); i++) {
//            Element item = (Element) items.item(i);
////            String baseDate = item.getElementsByTagName("baseDate").item(0).getTextContent();
////            String baseTime = item.getElementsByTagName("baseTime").item(0).getTextContent();
//            String category = item.getElementsByTagName("category").item(0).getTextContent();
//            // Error 발생할수도 있으며 받아온 정보를 double이 아니라 문자열로 읽으면 오류
//            value = Double.valueOf(item.getElementsByTagName("obsrValue").item(0).getTextContent());
////            weatherdto.setDate();
//            WeatherValue weatherValue = WeatherValue.valueOf(category);
//
//            switch (weatherValue) {
//                case PTY:  // 강수형태
//                    weatherdto.setPTY(value);
//                    break;
//                case REH:  // 습도
//                    weatherdto.setREH(value);
//                    break;
//                case RN1:  // 1시간 강수량 범주(1mm)
//                    weatherdto.setRN1(value);
//                    break;
//                case T1H:  // 기온
//                    weatherdto.setT1H(value);
//                    break;
//                case POP:  // 강수확률
//                    weatherdto.setPOP(value);
//                    break;
//                case SKY:  // 하늘상태
//                    weatherdto.setSKY(value);
//                    break;
//                default:
//            }
//        }
//        return weatherdto;
//    }
//
//    // XML 문자열을 파싱하는 메서드
//    private static void parseXML(String xmlString) {
//        try {
//            if (xmlString == null || xmlString.isEmpty()) {
//                System.out.println("XML 문자열이 null이거나 비어 있습니다.");
//                return;
//            }
//
//            BufferedReader br = new BufferedReader(new StringReader(xmlString));
//            InputSource is = new InputSource(br);
//
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            DocumentBuilder db = dbf.newDocumentBuilder();
//            Document document = db.parse(is);
//            document.getDocumentElement().normalize();
//
//            // NodeList를 가져옴
//            NodeList nodeList = document.getElementsByTagName("fcstValue");
//
//            if (nodeList != null && nodeList.getLength() > 0) {
//                // 첫 번째 노드의 텍스트 내용을 가져옴
//                String fcstValue = nodeList.item(0).getTextContent();
//                System.out.println("Parsed fcstValue: " + fcstValue);
//            } else {
//                System.out.println("fcstValue 요소를 찾을 수 없습니다.");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}