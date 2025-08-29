package com.sample.operator.app.svc.pgp.biz;

import com.sample.operator.app.common.crypt.spec.PgpKeySpec;
import com.sample.operator.app.common.exception.OperException;
import com.sample.operator.app.dto.pgp.PgpPrivKeyDto;
import com.sample.operator.app.dto.pgp.PgpPubKeyDto;
import com.sample.operator.app.svc.fileBiz.ServerFileSvc;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.bcpg.*;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
public class PgpOperationBiz {

    private final PgpKeySpec pgpKeySpec;
    private final ServerFileSvc serverFileSvc;


    public byte[] convertPgpToBase64Str(MultipartFile pubFile, MultipartFile secFile)
    {
        try(ArmoredInputStream aisForPub = new ArmoredInputStream(pubFile.getInputStream());
            ArmoredInputStream aisForSec = new ArmoredInputStream(secFile.getInputStream()))
        {
            // PGP 키링
            PGPPublicKeyRingCollection pub = new PGPPublicKeyRingCollection(aisForPub, pgpKeySpec.getCalculator());
            PGPSecretKeyRingCollection sec = new PGPSecretKeyRingCollection(aisForSec, pgpKeySpec.getCalculator());

            //문자열 변환
            String strPub = Base64.getEncoder().encodeToString(getArmoredBtArr(pub.getEncoded()));
            String strSec = Base64.getEncoder().encodeToString(getArmoredBtArr(sec.getEncoded()));

            //zip 리턴
            return makeZipFile(strPub.getBytes(), strSec.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] convertBase64StrToPgp(MultipartFile pubFile, MultipartFile secFile)
    {
        try{
            // 원본 문자열
            String pubstr = new String(pubFile.getBytes());
            String secstr = new String(secFile.getBytes());

            // base64 bytearr 변환
            byte[] pubBt = Base64.getDecoder().decode(pubstr);
            byte[] secBt = Base64.getDecoder().decode(secstr);

            // PGP로 변환
            PGPPublicKeyRingCollection pubKeyR = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(pubBt)), pgpKeySpec.getCalculator());
            PGPSecretKeyRingCollection secKeyr = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(secBt)), pgpKeySpec.getCalculator());

            //armored 로 변환 후 zip 리턴
            return makeZipFile(getArmoredBtArr(pubKeyR.getEncoded()), getArmoredBtArr(secKeyr.getEncoded()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // 공개키 추가
    public byte[] addPubKeyRing(MultipartFile keycol, MultipartFile newKey)
    {
        try(ArmoredInputStream aisForCol = new ArmoredInputStream(keycol.getInputStream());
            ArmoredInputStream aisForKey = new ArmoredInputStream(newKey.getInputStream()))
        {
            PGPPublicKeyRingCollection pubCol = new PGPPublicKeyRingCollection(aisForCol.readAllBytes(), pgpKeySpec.getCalculator());
            PGPPublicKeyRing pubKey = new PGPPublicKeyRing(aisForKey.readAllBytes(), pgpKeySpec.getCalculator());

            pubCol = PGPPublicKeyRingCollection.addPublicKeyRing(pubCol, pubKey);

            return getArmoredBtArr(pubCol.getEncoded());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new OperException(e.getMessage());
        }
    }


    // 비밀키 추가
    public byte[] addSecKeyRing(MultipartFile keycol, MultipartFile newKey)
    {
        try(ArmoredInputStream aisForCol = new ArmoredInputStream(keycol.getInputStream());
        ArmoredInputStream aisForKey = new ArmoredInputStream(newKey.getInputStream()))
        {
            PGPSecretKeyRingCollection secCol = new PGPSecretKeyRingCollection(aisForCol.readAllBytes(), pgpKeySpec.getCalculator());
            PGPSecretKeyRing secKey = new PGPSecretKeyRing(aisForKey.readAllBytes(), pgpKeySpec.getCalculator());

            secCol = PGPSecretKeyRingCollection.addSecretKeyRing(secCol, secKey);

            return getArmoredBtArr(secCol.getEncoded());
        }catch (Exception e)
        {
            e.printStackTrace();
            throw new OperException(e.getMessage());
        }
    }


    // 각 키링에서 마스터서브 키페어삭제
    public byte[] removeKeyPair(PGPPublicKeyRingCollection pubCol, PGPSecretKeyRingCollection secCol, long masterId) throws IOException {
        PGPPublicKeyRingCollection removedPub = removeKeyPairFromPubKeyRing(pubCol, masterId);
        PGPSecretKeyRingCollection removedSec = removeKeyPairFromSecKeyRing(secCol, masterId);

        return makeZipFile(getArmoredBtArr(removedPub.getEncoded()), getArmoredBtArr(removedSec.getEncoded()));
    }

    // 공개키링에서 마스터 서브 키 삭제
    private PGPPublicKeyRingCollection removeKeyPairFromPubKeyRing(PGPPublicKeyRingCollection col, long masterId)
    {
        PGPPublicKeyRingCollection returnCol = col;

        if(returnCol.contains(masterId))
        {
            PGPPublicKeyRing masterKey = returnCol.getPublicKeyRing(masterId);
            returnCol = PGPPublicKeyRingCollection.removePublicKeyRing(returnCol, masterKey);
        }

        return returnCol;
    }


    // 비밀키링에서 마스터 서브 키 삭제
    private PGPSecretKeyRingCollection removeKeyPairFromSecKeyRing(PGPSecretKeyRingCollection col, long masterId)
    {
        PGPSecretKeyRingCollection returnCol = col;

        if(returnCol.contains(masterId))
        {
            PGPSecretKeyRing masterKey = returnCol.getSecretKeyRing(masterId);
            returnCol = PGPSecretKeyRingCollection.removeSecretKeyRing(returnCol, masterKey);
        }

        return returnCol;
    }

    //공개키 정보 확인
    public List<PgpPubKeyDto> showPubKeyRingInfo(PGPPublicKeyRingCollection col)
    {
        List<PgpPubKeyDto> pubKeys = new ArrayList<>();

        col.getKeyRings().forEachRemaining( ring ->{
            ring.getPublicKeys().forEachRemaining( key -> {
                StringBuffer sb = new StringBuffer();
                key.getUserIDs().forEachRemaining( uid -> {
                    sb.append(uid).append(" ");
                });

                if(key.isEncryptionKey() && key.isMasterKey()) // 마스터여야만 PGP KEY BLOCK 으로 나옴. Sub 라면 PGP KEY MESSAGE 로 나와서 사용불가
                {
//                    System.out.println("마스터키입니다");
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(key.getCreationTime());
                cal.add(Calendar.DAY_OF_MONTH, (int)(key.getValidSeconds() /(60*60*24)));
                Date expiration = cal.getTime();
                boolean isValidNow = expiration.after(new Date());

                PgpPubKeyDto dto = PgpPubKeyDto.builder()
                        .keyId(String.valueOf(key.getKeyID()))
                        .userId(sb.toString())
                        .creationTime(key.getCreationTime())
                        .expirationTime(expiration)
                        .validPeriod(key.getValidSeconds() /(60*60*24))
                        .isMaster(key.isEncryptionKey() && key.isMasterKey())
                        .isValidNow(isValidNow)
                        .build();

                pubKeys.add(dto);
            });
        });
        return pubKeys;
    }

    // 개인키 정보 확인
    public List<PgpPrivKeyDto> showPrivKeyRingInfo(PGPSecretKeyRingCollection col)
    {
        List<PgpPrivKeyDto> privKeyDtos = new ArrayList<>();

        col.getKeyRings().forEachRemaining( ring -> {
            ring.getSecretKeys().forEachRemaining( key -> {
                StringBuffer sb = new StringBuffer();
                key.getUserIDs().forEachRemaining( uid -> {
                    sb.append(uid).append(" ");
                });

                Calendar cal = Calendar.getInstance();
                cal.setTime(key.getPublicKey().getCreationTime());
                cal.add(Calendar.DAY_OF_MONTH, (int)(key.getPublicKey().getValidSeconds() / (60*60*24)));
                Date expireDate = cal.getTime();
                boolean isValidNow = expireDate.after(new Date());

                PgpPrivKeyDto dto = PgpPrivKeyDto.builder()
                        .keyId(String.valueOf(key.getKeyID()))
                        .userId(sb.toString())
                        .creationTime(key.getPublicKey().getCreationTime())
                        .expirationTime(expireDate)
                        .isMaster(key.getPublicKey().isMasterKey() && key.getPublicKey().isEncryptionKey())
                        .isValidNow(isValidNow)
                        .build();

                privKeyDtos.add(dto);
            });
        });

        return privKeyDtos;
    }

    // PGP 키 쌍 생성 pub , sec 생성하여 zip 리턴
    public byte[] createPgpKeySet() throws NoSuchAlgorithmException, PGPException, IOException {
        Date date = new Date();
        
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(pgpKeySpec.getGeneratorAlgorithm(),pgpKeySpec.getBcProvider());
        kpg.initialize(pgpKeySpec.getKeySize()); // 키길이

        // 마스터키 생성 = 암호화용
        KeyPair kpEnc = kpg.generateKeyPair();
        PGPKeyPair encKeyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, kpEnc, date);

        PGPSignatureSubpacketVector unhashedPcks = null;
        PGPSignatureSubpacketVector masterHashedPcks = genHashedPcks(true);

        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
        JcaPGPContentSignerBuilder signer = new JcaPGPContentSignerBuilder(encKeyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256);
        PBESecretKeyEncryptor encryptor = new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256).setProvider(pgpKeySpec.getBcProvider()).build(pgpKeySpec.getPassPhrase());

        PGPKeyRingGenerator keyGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, encKeyPair, pgpKeySpec.getIdentity(), sha1Calc, masterHashedPcks, unhashedPcks, signer, encryptor);

        // 서브키 생성 = 서명용
        KeyPair kpSgn = kpg.generateKeyPair();
        PGPKeyPair sgnKeyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, kpSgn, date);

        PGPSignatureSubpacketVector subhashedPcks = genHashedPcks(false);

        // 서브키를 add
        keyGen.addSubKey(sgnKeyPair, subhashedPcks, unhashedPcks);

        //zip으로 묶기
        PGPSecretKeyRing secretKeyRing = keyGen.generateSecretKeyRing();
        PGPPublicKeyRing pubKeyRing = keyGen.generatePublicKeyRing();

        return makeZipFile(pubKeyRing.getEncoded(), secretKeyRing.getEncoded());

    }
    
    public byte[] makeZipFile(byte[] pubKey, byte[] secKey)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(ZipOutputStream zos = new ZipOutputStream(baos))// zos.close를 먼저 닫아줘야 파일 완성
        {
            serverFileSvc.addFileToZip(zos, pubKey, "public");
            serverFileSvc.addFileToZip(zos, secKey, "secret");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new OperException(e.getMessage());
        }

        return baos.toByteArray();
    }


    // 해시 패킷 생성
    private PGPSignatureSubpacketVector genHashedPcks(boolean isMaster)
    {
        PGPSignatureSubpacketGenerator svgGen = new PGPSignatureSubpacketGenerator();

        svgGen.setKeyExpirationTime(true, pgpKeySpec.getExpiryPeriod());

        int[] encAlgs = {SymmetricKeyAlgorithmTags.AES_256};
        svgGen.setPreferredSymmetricAlgorithms(true, encAlgs);

        int[] hashAlgs ={HashAlgorithmTags.SHA512, HashAlgorithmTags.SHA384, HashAlgorithmTags.SHA256};
        svgGen.setPreferredHashAlgorithms(true, hashAlgs);

        int[] compAlgs = {CompressionAlgorithmTags.ZIP};
        svgGen.setPreferredCompressionAlgorithms(true, compAlgs);

        svgGen.setFeature(true, Features.FEATURE_MODIFICATION_DETECTION);

        if(isMaster)
        {
            // 마스터키
            svgGen.setPrimaryUserID(true, true);
            svgGen.setKeyFlags(true, KeyFlags.CERTIFY_OTHER + KeyFlags.SIGN_DATA);
        }
        else
        {
            // 서브키
            svgGen.setKeyFlags(true, KeyFlags.ENCRYPT_COMMS + KeyFlags.ENCRYPT_STORAGE);
        }

        return svgGen.generate();
    }


    private byte[] getArmoredBtArr(byte[] btArr)
    {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();

        try(ArmoredOutputStream armoredOut = new ArmoredOutputStream(bo))
        {
            armoredOut.write(btArr);
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return bo.toByteArray();
    }
}
