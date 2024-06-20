package project.weather;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Controller
public class weatherController {

    private final LatxLnY latxLnY;
    private final ApiExplorer apiExplorer;
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = today.plusDays(1);
    LocalDate dayAfterTomorrow = today.plusDays(2);

    @GetMapping("/")
    public String main(Model model) {
        return "layout";
    }

    @GetMapping("/weatherData")
    public String getWeatherData(@RequestParam("nx") String nx, @RequestParam("ny") String ny, @RequestParam("address") String address, Model model) throws JSONException, IOException {
        double nxDou = Double.parseDouble(nx);
        double nyDou = Double.parseDouble(ny);
        LatxLnY.LatXLngY info = this.latxLnY.convertGRID_GPS(0, nxDou, nyDou);   // daum 주소에 따른 적도,위도를 기상청 적도,위도로 변경
        List<WeatherDTO> weatherDTOList = this.apiExplorer.getWeatherInfo(info.x, info.y);

        List<List<WeatherDTO>> dtoList = new ArrayList<>();
        int startIndex = 0;
        int endIndex = 12;

        for (int i = 0; i < weatherDTOList.size() / 12; i++) {
            dtoList.add(weatherDTOList.subList(startIndex, Math.min(endIndex, weatherDTOList.size())));
            startIndex += 12;
            endIndex += 12;
        }

        WeatherDTO todayData = getFirstDataByDate(weatherDTOList, today);
        WeatherDTO tomorrowData = getFirstDataByDate(weatherDTOList, tomorrow);
        WeatherDTO dayAfterTomorrowData = getFirstDataByDate(weatherDTOList, dayAfterTomorrow);

        WeatherDTO nowWeather = weatherDTOList.get(2);

        model.addAttribute("today", todayData.getFcstDate());
        model.addAttribute("today2", Objects.equals(todayData.getTMN(), null) ? '0' : todayData.getTMN());
        model.addAttribute("today3", Objects.equals(todayData.getTMN(), null) ? '0' : todayData.getTMX());

        model.addAttribute("tomorrow", tomorrowData.getFcstDate());
        model.addAttribute("tomorrow2", tomorrowData.getTMN());
        model.addAttribute("tomorrow3", tomorrowData.getTMX());

        model.addAttribute("dayAfterTomorrow", dayAfterTomorrowData.getFcstDate());
        model.addAttribute("dayAfterTomorrow2", Objects.equals(dayAfterTomorrowData.getTMN(), "") ? '0' : dayAfterTomorrowData.getTMN());
        model.addAttribute("dayAfterTomorrow3", Objects.equals(dayAfterTomorrowData.getTMN(), "") ? '0' : dayAfterTomorrowData.getTMX());

        model.addAttribute("dtoList", dtoList);
        model.addAttribute("nowWeather", nowWeather);
        model.addAttribute("latitude", nx);
        model.addAttribute("longitude", ny);
        model.addAttribute("address", address);

        // 현재 시간의 정각시간부터 표현하기 위해 데이터 제공
        LocalDateTime now = LocalDateTime.now();
        now = now.withMinute(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentTime = now.format(DateTimeFormatter.ofPattern("HHmm"));
        String todayStr = today.format(formatter);
        String tomorrowStr = tomorrow.format(formatter);
        String dayAfterTomorrowStr = dayAfterTomorrow.format(formatter);

        model.addAttribute("currentTime", currentTime);
        model.addAttribute("todayStr", todayStr);
        model.addAttribute("tomorrowStr", tomorrowStr);
        model.addAttribute("dayAfterTomorrowStr", dayAfterTomorrowStr);

        return "page1";
    }

    @GetMapping("/coordinate")
    public String getCoordinate() {
        return "page2";
    }


    @GetMapping("/coordinate/detail")
    public String setCoordinateDetail(@RequestParam("nx") String nx, @RequestParam("ny") String ny, @RequestParam("address") String address
            , @RequestParam("str_date") String str_date, @RequestParam("end_date") String end_date, Model model) throws IOException {
        double nxDou = Double.parseDouble(nx);
        double nyDou = Double.parseDouble(ny);
        LatxLnY.LatXLngY info = this.latxLnY.convertGRID_GPS(0, nxDou, nyDou);
        Map<String, Object> mapDto = this.apiExplorer.getCoordiWeatheanrInfo(info.x, info.y,str_date,end_date);

        List<CoordiWeatherDTO> coordiWeatherDTOList = (List<CoordiWeatherDTO>) mapDto.get("coordiList");
        String totalTmp = mapDto.get("avgTmp").toString();
        int avgTmp = Integer.parseInt(totalTmp) / coordiWeatherDTOList.size();
        String maxTmp = mapDto.get("maxTmp").toString();  // 31/20240620/1600
        String minTmp = mapDto.get("minTmp").toString();  //20/20240621/0400
        String[] maxTmpInfo = maxTmp.split("/");  //20/20240621/0400
        String[] minTmpInfo = minTmp.split("/");


        List<List<CoordiWeatherDTO>> dtoList = new ArrayList<>();
        int startIndex = 0;
        int endIndex = 7;

        // coordiWeatherDTOList가 7 이하인 경우
        if (coordiWeatherDTOList.size() < 7) {
            dtoList.add(coordiWeatherDTOList);
        } else {
            // coordiWeatherDTOList가 7 초과인 경우
            for (int i = 0; i < coordiWeatherDTOList.size() / 7; i++) {
                dtoList.add(coordiWeatherDTOList.subList(startIndex, Math.min(endIndex, coordiWeatherDTOList.size())));
                startIndex += 7;
                endIndex += 7;
            }
        }


        model.addAttribute("coordiWeatherList",dtoList);

        // 현재 시간의 정각시간부터 표현하기 위해 데이터 제공
        LocalDateTime now = LocalDateTime.now();
        now = now.withMinute(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentTime = now.format(DateTimeFormatter.ofPattern("HHmm"));
        String todayStr = today.format(formatter);
        String tomorrowStr = tomorrow.format(formatter);
        String dayAfterTomorrowStr = dayAfterTomorrow.format(formatter);
        String maxDay = maxTmpInfo[1].equals(todayStr) ? "오늘" : (maxTmpInfo[1].equals(tomorrowStr) ? "내일" : "모레");
        String minDay = minTmpInfo[1].equals(todayStr) ? "오늘" : (maxTmpInfo[1].equals(tomorrowStr) ? "내일" : "모레");

        model.addAttribute("maxTmp",maxTmpInfo[0]);
        model.addAttribute("maxDay",maxDay);
        model.addAttribute("maxTime",maxTmpInfo[2].substring(0,2));
        model.addAttribute("minTmp",minTmpInfo[0]);
        model.addAttribute("minDay",minDay);
        model.addAttribute("minTime",minTmpInfo[2].substring(0,2));

        model.addAttribute("currentTime", currentTime);
        model.addAttribute("todayStr", todayStr);
        model.addAttribute("tomorrowStr", tomorrowStr);
        model.addAttribute("dayAfterTomorrowStr", dayAfterTomorrowStr);
        model.addAttribute("avgTmp",avgTmp);
        model.addAttribute("address",address);
        return "page2-2";
    }

    private WeatherDTO getFirstDataByDate(List<WeatherDTO> dtoList, LocalDate date) {
        String TMX = null;
        String TMN = null;
        String Date = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (WeatherDTO dto : dtoList) {
            if (dto.getFcstDate().equals(Date)) {
                if (dto.getTMN() != null) {
                    TMN = dto.getTMN();
                } else if (dto.getTMX() != null) {
                    TMX = dto.getTMX();
                }
            }
        }
        WeatherDTO weatherDTO = getTemper(TMX, TMN, Date);
        return weatherDTO;
    }

    public WeatherDTO getTemper(String TMX, String TMN, String date) {
        WeatherDTO newDto = new WeatherDTO();
        newDto.setFcstDate(date);
        newDto.setTMX(TMX);
        newDto.setTMN(TMN);
        return newDto;
    }

}
