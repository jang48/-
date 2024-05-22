package project.weather;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class weatherController {
    private final weatherAPI WeatherAPI;
    @GetMapping("/")
    public String test(){
//        this.WeatherAPI.weatherInfo();
        return "main";
    }
}
