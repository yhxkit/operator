package com.sample.operator.app.common.crypt;

import com.sample.operator.app.common.crypt.dto.PgpDto;
import com.sample.operator.app.common.crypt.spec.PgpKeySpec;
import com.sample.operator.app.common.exception.OperException;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PgpCryptor implements BaseCryptor {
    private final PgpKeySpec pgpKeySpec;

    private final int BUFFER_SIZE = 1 << 16;

    @Override
    public String encrypt(String data) {
        return "";
    }

    @Override
    public String decrypt(String data) {
        return "";
    }

    public String encrypt(PgpDto pgpDto, String data, ByteArrayOutputStream outputStream) {
        PGPPublicKeyRingCollection pubCol = pgpDto.getPublicKeyCollection();
        PGPSecretKeyRingCollection secCol = pgpDto.getSecretKeyCollection();

        String decryptedData = "";

        try (InputStream contentStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)))
        {
            // 암호화 키 추출 및 메서드 추가 ~
             List<JcePublicKeyKeyEncryptionMethodGenerator> pgpEncVoList = extractPgpEncGen(pubCol);
             PGPEncryptedDataGenerator edg = new PGPEncryptedDataGenerator(new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5).setSecureRandom(new SecureRandom()).setProvider(pgpKeySpec.getBcProvider()));

             for (JcePublicKeyKeyEncryptionMethodGenerator pgpEncVo : pgpEncVoList) {
                 edg.addMethod(pgpEncVo);
             }
            // ~암호화 키 추출 및 메서드 추가


            OutputStream encOut = edg.open(outputStream, new byte[BUFFER_SIZE]);

             PGPCompressedDataGenerator compress = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP);
             OutputStream comOut = compress.open(encOut);


             //서명용 키 추출 및 서명 생성~
            List<PGPSignatureGenerator> sigVoList = extractPgpSigGen(secCol);

            for(PGPSignatureGenerator sigVo : sigVoList)
            {
                sigVo.generateOnePassVersion(false).encode(comOut);
            }
            // ~서명용 키 추출 및 서명 생성


            // 암호화~
            PGPLiteralDataGenerator litGen = new PGPLiteralDataGenerator();
            OutputStream litOut = litGen.open(comOut, PGPLiteralData.BINARY, "", new Date(), new byte[BUFFER_SIZE]);

            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            while ((len = contentStream.read(buf)) > 0)
            {
                litOut.write(buf, 0, len);

                for(PGPSignatureGenerator sigVo : sigVoList){
                    sigVo.update(buf, 0, len); // 암호화
                }
            }

            // 종료해야 암호화 데이터 생성!
            litOut.close();
            // ~암호화


            // 서명 ~
            for(PGPSignatureGenerator sigVo : sigVoList){
                sigVo.generate().encode(comOut); // 데이터 인코딩
            }
            // ~서명

            // 리소스 종료
            litOut.close();
            compress.close();
            comOut.close();
            edg.close();
            encOut.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream.toString(); // ? 어케 리턴할까
    }


    public String decrypt(PgpDto pgpDto, String data, ByteArrayOutputStream outputStream) {
        PGPPublicKeyRingCollection pubCol = pgpDto.getPublicKeyCollection();
        PGPSecretKeyRingCollection secCol = pgpDto.getSecretKeyCollection();

        String decryptedData = "";

        try{
            long partnerKeyId = 0;
            long myKeyId = 0;

            PGPPrivateKey privateKey = null;
            PublicKeyDataDecryptorFactory decFac = null;
            PGPPublicKeyEncryptedData encData = null;

            InputStream encIn = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            PGPObjectFactory objFac = new PGPObjectFactory(PGPUtil.getDecoderStream(encIn), pgpKeySpec.getCalculator());

            Object firstObj = objFac.nextObject();

            PGPEncryptedDataList encList = (PGPEncryptedDataList) (firstObj instanceof PGPEncryptedDataList ? (PGPEncryptedDataList) firstObj : objFac.nextObject());
            Iterator<?> i = encList.getEncryptedDataObjects();

            // 복호화 키 추출 ~
             while (i.hasNext()) {
                 //my 개인키 추출 = 복호화용
                 encData = (PGPPublicKeyEncryptedData) i.next();
                 long keyId = encData.getKeyID();

                 PGPSecretKey secKey = secCol.getSecretKey(keyId);

                 System.out.println("enc 데이터에서 추출한 비밀키 아이디 " + keyId);

                 // 키 ID 에 해당하는 키가 키링에 있다면
                 if(secKey != null)
                 {
                     myKeyId = secKey.getKeyID();
                     System.out.println("mykey 개인키 추출. 데이터 복호화 용도. 키 ID " + myKeyId);

                     privateKey = secKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider(pgpKeySpec.getBcProvider()).build(pgpKeySpec.getPassPhrase()));
                     decFac = new JcePublicKeyDataDecryptorFactoryBuilder().setProvider(pgpKeySpec.getBcProvider()).setContentProvider(pgpKeySpec.getBcProvider()).build(privateKey);

                     break;
                 }
             }

             if(privateKey == null)
             {
                 throw new OperException("메시지에 대한 비밀키를 찾을 수 없음 " + myKeyId);
             }
            // ~ 복호화 키 추출

            // 압축 데이터 추출~
            InputStream comIs = encData.getDataStream(decFac);
             PGPObjectFactory comObjFac = new PGPObjectFactory(comIs, pgpKeySpec.getCalculator());



             Object msg = comObjFac.nextObject();

             if( msg instanceof PGPCompressedData comData)
             {
                 // 압축 데이터
                 objFac = new PGPObjectFactory(comData.getDataStream(), pgpKeySpec.getCalculator());
                 msg = objFac.nextObject();
             }
             else {
                 objFac = comObjFac;
             }
             // ~압축 데이터 추출

            // 서명 데이터 추출 ~
            PGPOnePassSignature ops = null;

             int encIdx =0;
             if(msg instanceof PGPOnePassSignatureList)
             {
                 for(int ei=0; ei<((PGPOnePassSignatureList) msg).size(); ei++)
                 {
                     ops = ((PGPOnePassSignatureList) msg).get(ei);
                     PGPPublicKey signPubKey = pubCol.getPublicKey(partnerKeyId = ops.getKeyID());

                     if(signPubKey != null)
                     {
                         System.out.println("서명에서 추출한 파트너 공개키 ID " + partnerKeyId);

                         encIdx = ei;
                         ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider(pgpKeySpec.getBcProvider()), signPubKey);

                         break;
                     }
                 }
                 msg = objFac.nextObject();
             }
            // ~ 서명 데이터 추출


            // 복호화 데이터 write ~
            if(msg instanceof PGPLiteralData litData)
            {
                InputStream litIs = litData.getInputStream();
                int nextBt;

                while((nextBt = litIs.read()) >= 0)
                {
                    ops.update((byte)nextBt);
                    outputStream.write((char)nextBt);
                }
            }
            else
            {
                throw new OperException("예기치 않은 메시지 유형");
            }
            // ~복호화 데이터 write

            //서명데이터 검증~
            if(ops != null){
                PGPSignatureList sigLIst = (PGPSignatureList) objFac.nextObject();
                PGPSignature msgSig = sigLIst.get(encIdx);

                if(!ops.verify(msgSig))
                {
                    throw new OperException("서명 검증 실패");
                }
            }
            //~서명데이터 검증

            if(encData.isIntegrityProtected())
            {
                // 무결성 검증
                if(!encData.verify())
                {
                    throw new OperException("메시지 무결성 검사 실패 ");
                }
            }
            decryptedData = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            
            System.out.println("복호화 성공 ");
        } catch (Exception e) {
            System.out.println("복호화 실패");
            e.printStackTrace();
        }
        
        return decryptedData;
    }


    // 암호화용 키 리스트 추출
    private List<JcePublicKeyKeyEncryptionMethodGenerator> extractPgpEncGen(PGPPublicKeyRingCollection pubCol)
    {
        List<JcePublicKeyKeyEncryptionMethodGenerator> result = new ArrayList<>();
        pubCol.forEach( pub -> {
            PGPPublicKey pubKey = pub.getPublicKey();
            System.out.println("암호화용 공개키 서브 ID" + pubKey.getKeyID());

            JcePublicKeyKeyEncryptionMethodGenerator jce = new JcePublicKeyKeyEncryptionMethodGenerator(pubKey).setProvider(pgpKeySpec.getBcProvider()).setSecureRandom(new SecureRandom());
            result.add(jce);
        });

        return result;
    }

    //서명용 키 리스트 추출
    private List<PGPSignatureGenerator> extractPgpSigGen(PGPSecretKeyRingCollection secCol)
    {
        List<PGPSignatureGenerator> result = new ArrayList<>();
        secCol.forEach( sec -> {
            try {
                PGPSecretKey seckey = sec.getSecretKey();
                System.out.println("PGP 서명 용도 개인키 / 마스터 키 아이디" + seckey.getKeyID());

                PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder(new JcaPGPDigestCalculatorProviderBuilder().setProvider(pgpKeySpec.getBcProvider()).build())
                        .setProvider(pgpKeySpec.getBcProvider()).build(pgpKeySpec.getPassPhrase());

                PGPPrivateKey priKey = seckey.extractPrivateKey(decryptor);
                PGPSignatureGenerator sigGen = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(seckey.getPublicKey().getAlgorithm(), PGPUtil.SHA1).setProvider(pgpKeySpec.getBcProvider()), seckey.getPublicKey());
                sigGen.init(PGPSignature.BINARY_DOCUMENT, priKey);

                Iterator<?> i = seckey.getPublicKey().getUserIDs();

                if (i.hasNext()) {
                    String userID = (String) i.next();
                    PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
                    spGen.addSignerUserID(false, userID);
                    sigGen.setHashedSubpackets(spGen.generate());
                }
                result.add(sigGen);
            } catch (Exception e) {

            }
        });

        return result;
    }


}

