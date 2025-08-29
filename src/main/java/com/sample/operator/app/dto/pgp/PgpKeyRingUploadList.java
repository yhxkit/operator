package com.sample.operator.app.dto.pgp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PgpKeyRingUploadList {
    List<PgpKeyRingUploadDto> list;
    String comment;
}
