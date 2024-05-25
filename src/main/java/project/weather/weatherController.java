package project.weather;

import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@RequiredArgsConstructor
@Controller
public class weatherController {

    private final LatxLnY latxLnY;
    private final ApiExplorer apiExplorer;
    @GetMapping("/")
    public String test(Model model){
        model.addAttribute("dto","test");
        return "main";
    }

    @GetMapping("/weatherData")
    @ResponseBody
    public weatherDTO getWeatherData(@RequestParam("nx") double nx, @RequestParam("ny") double ny, Model model) throws IOException, JSONException {
        LatxLnY.LatXLngY info = this.latxLnY.convertGRID_GPS(0, nx, ny);   // daum 주소에 따른 적도,위도를 기상청 적도,위도로 변경
        weatherDTO weatherDTO1 = this.apiExplorer.getWeatherInfo(info.x,info.y); // 기상청 api에 적도,위도 적용하여 초단기예보 가져오기
        return weatherDTO1;
    }
}
