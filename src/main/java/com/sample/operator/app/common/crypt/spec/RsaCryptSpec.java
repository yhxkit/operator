package com.sample.operator.app.common.crypt.spec;

import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;

@Component
@Getter
public class RsaCryptSpec {
    final AlgorithmParameterSpec paramSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
    String cipherTransformation = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";
}
