package com.sample.operator.app.dto.pgp;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class PgpPrivKeyDto {

    String keyId;
    String userId;

    boolean isMaster;

    Date creationTime;
    Date expirationTime;

    long validPeriod;
    boolean isValidNow;
}
