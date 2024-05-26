package project.weather;

import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class weatherController {

    private final LatxLnY latxLnY;
    private final ApiExplorer apiExplorer;
    @GetMapping("/")
    public String test(){
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
        redirectAttributes.addAttribute("x",info.x);
        redirectAttributes.addAttribute("y",info.y);
        return "redirect:/weatherData/test/";
    }


    @RequestMapping("/weatherData/test/")
    public String handleWeatherData(@RequestParam("x")double x, @RequestParam("y")double y,  Model model) throws JSONException, IOException {
        List<WeatherDTO> weatherDTOList = this.apiExplorer.getWeatherInfo(x,y); // 기상청 api에 적도,위도 적용하여 초단기예보 가져오기
        List<List<WeatherDTO>> dtoList =  new ArrayList<>();
        int startIndex = 0;
        int endIndex = 5;

        for (int i = 0; i < weatherDTOList.size() / 5; i++) {
            dtoList.add(weatherDTOList.subList(startIndex, Math.min(endIndex, weatherDTOList.size())));
            startIndex += 5;
            endIndex += 5;
        }
        model.addAttribute("dtoList", dtoList);
        return "main2";
    }



}
