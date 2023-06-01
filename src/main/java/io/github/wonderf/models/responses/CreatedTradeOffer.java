package io.github.wonderf.models.responses;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreatedTradeOffer {
    @JSONField(name = "tradeofferid")
    private String id;

    @JSONField(name = "needs_mobile_confirmation")
    private Boolean mobileConfirmation;

    @JSONField(name = "needs_email_confirmation")
    private Boolean emailConfirmation;
    @JSONField(name = "email_domain")
    private String emailDomain;

    @JSONField(name = "strError")
    private String error;
}
