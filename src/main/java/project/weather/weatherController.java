package project.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class weatherController {

    private final LatxLnY latxLnY;
    private final ApiExplorer apiExplorer;

    @GetMapping("/")
    public String test() {
        return "main";
    }

//    @GetMapping("/weatherData")
//    @ResponseBody
//    public List<WeatherDTO> getWeatherData(@RequestParam("nx") double nx, @RequestParam("ny") double ny, Model model) throws IOException, JSONException {
//        LatxLnY.LatXLngY info = this.latxLnY.convertGRID_GPS(0, nx, ny);   // daum 주소에 따른 적도,위도를 기상청 적도,위도로 변경
//        List<WeatherDTO> weatherDTOList = this.apiExplorer.getWeatherInfo(info.x,info.y); // 기상청 api에 적도,위도 적용하여 초단기예보 가져오기
//        return weatherDTOList;
//    }

    @GetMapping("/weatherData")
    public String getWeatherData(@RequestParam("nx") String nx, @RequestParam("ny") String ny, RedirectAttributes redirectAttributes) {
        double nxDou = Double.parseDouble(nx);
        double nyDou = Double.parseDouble(ny);
        LatxLnY.LatXLngY info = this.latxLnY.convertGRID_GPS(0, nxDou, nyDou);   // daum 주소에 따른 적도,위도를 기상청 적도,위도로 변경
        redirectAttributes.addAttribute("x", info.x);
        redirectAttributes.addAttribute("y", info.y);
        return "redirect:/weatherData/test/";
    }


    @RequestMapping("/weatherData/test/")
    public String handleWeatherData(@RequestParam("x") double x, @RequestParam("y") double y, Model model) throws JSONException, IOException {
        List<WeatherDTO> weatherDTOList = this.apiExplorer.getWeatherInfo(x, y); // 기상청 api에 적도,위도 적용하여 초단기예보 가져오기

        List<List<WeatherDTO>> dtoList = new ArrayList<>();
        int startIndex = 0;
        int endIndex = 12;

        for (int i = 0; i < weatherDTOList.size() / 12; i++) {
            dtoList.add(weatherDTOList.subList(startIndex, Math.min(endIndex, weatherDTOList.size())));
            startIndex += 12;
            endIndex += 12;
        }

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        WeatherDTO todayData = getFirstDataByDate(weatherDTOList, today);
        WeatherDTO tomorrowData = getFirstDataByDate(weatherDTOList, tomorrow);
        WeatherDTO dayAfterTomorrowData = getFirstDataByDate(weatherDTOList, dayAfterTomorrow);

        WeatherDTO nowWeather = weatherDTOList.get(0);

        model.addAttribute("today",todayData.getFcstDate());
        model.addAttribute("today2",todayData.getTMN());
        model.addAttribute("today3",todayData.getTMX());

        model.addAttribute("tomorrow",tomorrowData.getFcstDate());
        model.addAttribute("tomorrow2",tomorrowData.getTMN());
        model.addAttribute("tomorrow3",tomorrowData.getTMX());

        model.addAttribute("dayAfterTomorrow",dayAfterTomorrowData.getFcstDate());
        model.addAttribute("dayAfterTomorrow2",dayAfterTomorrowData.getTMN());
        model.addAttribute("dayAfterTomorrow3",dayAfterTomorrowData.getTMX());

        model.addAttribute("dtoList", dtoList);
        model.addAttribute("nowWeather", nowWeather);
        return "main2";
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
