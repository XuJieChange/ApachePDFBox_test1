package org.example.pdf2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.example.pdf2.Extension;

public class PKcs {

    private static KeyPair getKey() throws NoSuchAlgorithmException {
        //getKey() ->這個方法使用RSA演算法生成一對公鑰和私鑰
        //創建一個新的KeyPairGenerator物件，
        // 該物件能生成RSA密鑰對。
        // 它明確地使用BouncyCastle提供者來實例化，
        // 這意味著密鑰生成是根據BouncyCastle的實現。
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA",
                new BouncyCastleProvider());
        generator.initialize(1024);//初始化密鑰生成器，1024是密鑰的位大小
        // 证书中的密钥 公钥和私钥
        KeyPair keyPair = generator.generateKeyPair();
        return keyPair;
    }

    /**
     * @param password
     *            密码
     * @param issuerStr 颁发机构信息
     *
     * @param subjectStr 使用者信息
     *
     * @param certificateCRL 颁发地址
     *
     * @return
     */

    //主要目的是創建一個PKCS#12格式的KeyStore，
    // 其中包含生成的數字證書和相應的私鑰。
    public static Map<String, byte[]> createCert(String password,
                                                 String issuerStr, String subjectStr, String certificateCRL) {
        //返回值是一個映射(Map)，鍵是字符串類型，值是byte數組。
        //方法接受四個參數：password (密碼), issuerStr (發行者信息),
        // subjectStr (使用者信息), 和 certificateCRL (證書的CRL分發點地址)。
        Map<String, byte[]> result = new HashMap<String, byte[]>();
        //初始化一個用來保存和返回數據的映射。
        ByteArrayOutputStream out = null;
        try {
            //初始化一個名為PKCS12的KeyStore類型，並使用Bouncy Castle作為安全提供者。
            KeyStore keyStore = KeyStore.getInstance("PKCS12",
                    new BouncyCastleProvider());
            //使用keyStore.load(null, null);載入KeyStore。
            // 在這裡，因為輸入參數是null，所以它將會載入一個空的KeyStore。
            keyStore.load(null, null);
            //調用了getKey()方法來生成一個KeyPair。
            // 包含公鑰和私鑰。
            KeyPair keyPair = getKey();
            // issuer与 subject相同的证书就是CA证书
            //使用generateCertificateV3方法生成一個X.509版本3的證書。
            Certificate cert = generateCertificateV3(issuerStr, subjectStr,
                    keyPair, result, certificateCRL, null);
            // 將私鑰和相關的證書保存到KeyStore中，使用"cretkey"作為別名。
            keyStore.setKeyEntry("cretkey", keyPair.getPrivate(),
                    password.toCharArray(), new Certificate[] { cert });
            // 創建一個ByteArrayOutputStream來保存KeyStore的二進制數據
            out = new ByteArrayOutputStream();
            //確保該證書可以使用相應的公鑰進行驗證。
            cert.verify(keyPair.getPublic());
            //將KeyStore的內容保存到ByteArrayOutputStream中
            keyStore.store(out, password.toCharArray());
            //從ByteArrayOutputStream中獲取二進制數據
            byte[] keyStoreData = out.toByteArray();
            result.put("keyStoreData", keyStoreData);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    /**
     * @param issuerStr
     * @param subjectStr
     * @param keyPair
     * @param result
     * @param certificateCRL
     * @param extensions
     * @return
     */
    public static Certificate generateCertificateV3(String issuerStr,
                                                    String subjectStr, KeyPair keyPair, Map<String, byte[]> result,
                                                    String certificateCRL, List<Extension> extensions) {
        //读取X.509证书数据，从而生成一个X509Certificate对象
        ByteArrayInputStream bout = null;
        //是Java中表示X.509证书的类。它包含了证书的所有信息，例如发行者、主题、有效期等
        //初始化
        X509Certificate cert = null;
        try {
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            //證書開始的時間
            Date notBefore = new Date();
            Calendar rightNow = Calendar.getInstance();
//            rightNow.setTime(notBefore);
            // 日期加1年
            rightNow.add(Calendar.YEAR, 1);
            Date notAfter = rightNow.getTime();
            // 证书序列号
            BigInteger serial = BigInteger.probablePrime(256, new Random());
            X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                    new X500Name(issuerStr), serial, notBefore, notAfter,
                    new X500Name(subjectStr), publicKey);
            JcaContentSignerBuilder jBuilder = new JcaContentSignerBuilder(
                    "SHA1withRSA");
            SecureRandom secureRandom = new SecureRandom();
            jBuilder.setSecureRandom(secureRandom);
            ContentSigner singer = jBuilder.setProvider(
                    new BouncyCastleProvider()).build(privateKey);
            // 分发点
            ASN1ObjectIdentifier cRLDistributionPoints = new ASN1ObjectIdentifier(
                    "2.5.29.31");
            GeneralName generalName = new GeneralName(
                    GeneralName.uniformResourceIdentifier, certificateCRL);
            GeneralNames seneralNames = new GeneralNames(generalName);
            DistributionPointName distributionPoint = new DistributionPointName(
                    seneralNames);
            DistributionPoint[] points = new DistributionPoint[1];
            points[0] = new DistributionPoint(distributionPoint, null, null);
            CRLDistPoint cRLDistPoint = new CRLDistPoint(points);
            builder.addExtension(cRLDistributionPoints, true, cRLDistPoint);
            // 用途
            ASN1ObjectIdentifier keyUsage = new ASN1ObjectIdentifier(
                    "2.5.29.15");
            // | KeyUsage.nonRepudiation | KeyUsage.keyCertSign
            builder.addExtension(keyUsage, true, new KeyUsage(
                    KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
            // 基本限制 X509Extension.java
            ASN1ObjectIdentifier basicConstraints = new ASN1ObjectIdentifier(
                    "2.5.29.19");
            builder.addExtension(basicConstraints, true, new BasicConstraints(
                    true));
            // privKey:使用自己的私钥进行签名，CA证书
            if (extensions != null)
                for (Extension ext : extensions) {
                    builder.addExtension(
                            new ASN1ObjectIdentifier(ext.getOid()),
                            ext.isCritical(),
                            ASN1Primitive.fromByteArray(ext.getValue()));
                }
            X509CertificateHolder holder = builder.build(singer);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            bout = new ByteArrayInputStream(holder.toASN1Structure()
                    .getEncoded());
            cert = (X509Certificate) cf.generateCertificate(bout);
            byte[] certBuf = holder.getEncoded();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            // 证书数据
            result.put("certificateData", certBuf);
            //公钥
            result.put("publicKey", publicKey.getEncoded());
            //私钥
            result.put("privateKey", privateKey.getEncoded());
            //证书有效开始时间
            result.put("notBefore", format.format(notBefore).getBytes("utf-8"));
            //证书有效结束时间
            result.put("notAfter", format.format(notAfter).getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bout != null) {
                try {
                    bout.close();
                } catch (IOException e) {
                }
            }
        }
        return cert;
    }

    public static void main(String[] args) throws Exception{
        // CN: 名字与姓氏    OU : 组织单位名称
        // O ：组织名称  L : 城市或区域名称  E : 电子邮件
        // ST: 州或省份名称  C: 单位的两字母国家代码
        String issuerStr = "CN=黃偉浩 Huang Weihao,OU=榮總,O=台中榮總,E=weihao@gmail.com,L=台中,ST=台灣";
        String subjectStr = "CN=黃偉浩 Huang Weihao,OU=榮總,O=台中榮總,E=weihao@gmail.com,L=台中,ST=台灣";
        String certificateCRL  = "https://www.vghtc.gov.tw/";
        Map<String, byte[]> result = createCert("123456", issuerStr, subjectStr, certificateCRL);

        FileOutputStream outPutStream = new FileOutputStream("D:/test/ApachePDFBox_test1/keystore.p12"); // ca.jks
        outPutStream.write(result.get("keyStoreData"));
        outPutStream.close();
        FileOutputStream fos = new FileOutputStream(new File("D:/test/ApachePDFBox_test1/keystore.cer"));
        fos.write(result.get("certificateData"));
        fos.flush();
        fos.close();
    }
}
