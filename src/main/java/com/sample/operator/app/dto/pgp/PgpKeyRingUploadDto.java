package com.sample.operator.app.dto.pgp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PgpKeyRingUploadDto {
    MultipartFile keyRingFile;
    String keyRingType; // PUBLIC /PRIVATE
    String svcType; // PGP
}
