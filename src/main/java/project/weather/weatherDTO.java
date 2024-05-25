package project.weather;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class weatherDTO {
    private String fcstDate;  // 예보일자
    private String fcstTime;  // 예보시간
    private Double PTY;   // 강수형태  - (단기) 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
    private Double REH;   // 습도(%)
    private Double RN1;   // 1시간 강수량 범주(1mm)
    private Double TMP;   // 1시간 기온
    private Double POP;   // 강수확률(%)
    private Double SKY;   // 하늘상태
    private Double SNO;   // 적설량(cm)

}
