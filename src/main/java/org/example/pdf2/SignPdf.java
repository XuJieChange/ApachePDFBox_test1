package org.example.pdf2;

import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.UUID;

public class SignPdf {
    /**
     * @param password
     *            秘钥密码
     * @param keyStorePath
     *            秘钥文件路径
     * @param signPdfSrc
     *            签名的PDF文件
     * @param signImage
     *            签名图片文件
     * @param x
     *            x坐标
     * @param y
     *            y坐标
     * @return
     */

    public static byte[] sign(String password, String keyStorePath, String signPdfSrc, String signImage,
                              float x, float y){
        File signPdfSrcFile=new File(signPdfSrc);
        PdfReader reader=null;
        ByteArrayOutputStream signPDFData=null;
        PdfStamper stp=null;
        FileInputStream fos=null;
        try{
            BouncyCastleProvider provider=new BouncyCastleProvider();
            Security.addProvider(provider);
            KeyStore ks=KeyStore.getInstance("PKCS12", new BouncyCastleProvider());
            fos=new FileInputStream(keyStorePath);
            //私鑰密碼 為Pkcs生成證書的私鑰密碼123456
            ks.load(fos, password.toCharArray());
            String alias=(String) ks.aliases().nextElement();
            PrivateKey key=(PrivateKey) ks.getKey(alias, password.toCharArray());
            Certificate[] chain=ks.getCertificateChain(alias);
            reader=new PdfReader(signPdfSrc);
            signPDFData=new ByteArrayOutputStream();

            //臨時的PDF格式透明圖片
            File temp=new File(signPdfSrcFile.getParent(),System.currentTimeMillis()+".pdf");
            stp=PdfStamper.createSignature(reader,signPDFData,'\0', temp, true);
            stp.setFullCompression();
            PdfSignatureAppearance sap=stp.getSignatureAppearance();
            sap.setReason("數字簽名，不可改變");

            //使用png格式透明圖片
            Image image=Image.getInstance(signImage);
            sap.setImageScale(0);
            sap.setSignatureGraphic(image);
            sap.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

            //是對應x,y軸座標
            sap.setVisibleSignature(new Rectangle(x,y,x+185,y+68),1,
                    UUID.randomUUID().toString().replace("-",""));
            stp.getWriter().setCompressionLevel(5);
            ExternalDigest digest=new BouncyCastleDigest();
            ExternalSignature signature = new PrivateKeySignature(key, DigestAlgorithms.SHA512, provider.getName());
            MakeSignature.signDetached(sap, digest, signature, chain, null, null, null, 0, MakeSignature.CryptoStandard.CADES);
            stp.close();
            reader.close();
            return signPDFData.toByteArray();

        }catch (Exception e) {
            e.printStackTrace();
        }finally {

            if (signPDFData != null) {
                try {
                    signPDFData.close();
                } catch (IOException e) {
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        byte[] fileData = sign("123456", "D:\\test\\ApachePDFBox_test1\\keystore.p12",
                "D:\\test\\ApachePDFBox_test1\\filled.pdf",
                "D:\\test\\ApachePDFBox_test1\\sign.jpg", 100, 190);
        FileOutputStream f = new FileOutputStream(new File("D:\\test\\ApachePDFBox_test1\\signed.pdf"));
        f.write(fileData);
        f.close();
    }
}
