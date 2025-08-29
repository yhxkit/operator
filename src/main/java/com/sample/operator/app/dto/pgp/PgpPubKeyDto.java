package com.sample.operator.app.dto.pgp;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class PgpPubKeyDto {

    String keyId;
    String userId;

    boolean isMaster;

    Date creationTime;
    Date expirationTime;

    long validPeriod;
    boolean isValidNow;

    boolean hasPrivateKey; // = 있으면 mykey, 없으면 partnerkey
}
