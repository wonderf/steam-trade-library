package io.github.wonderf.models.responses;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class AcceptedOffer {
    private final String tradeid;

    @JSONField(name = "needs_mobile_confirmation")
    private Boolean mobileConfirmation;

    @JSONField(name = "needs_email_confirmation")
    private Boolean emailConfirmation;
    @JSONField(name = "email_domain")
    private String emailDomain;
}
