package project.weather;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class WeatherDTO {
    private String fcstDate;  // 예보일자
    private String fcstTime;  // 예보시간
    private String PTY;   // 강수형태  - (단기) 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
    private String REH;   // 습도(%)
    private String RN1;   // 1시간 강수량 범주(1mm)
    private String TMP;   // 1시간 기온
    private String POP;   // 강수확률(%)
    private String SKY;   // 하늘상태
    private String SNO;   // 적설량(cm)
    private String TMN;   // 일 최저기온(℃)
    private String TMX;   // 일 최고기온(℃)
    private String WSD;   // 풍속 m/s
}
