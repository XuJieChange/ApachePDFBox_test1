package org.example;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ReadPDFImagesExample {
    public static void main(String[] arg){
        try{
            //加載PDF文件
            PDDocument document=PDDocument.load(new File("one-more-jpa.pdf"));

            PDPageTree pageTree=document.getPages();

            //遍歷每個頁面
            for(PDPage page : pageTree){
                int pagNum=pageTree.indexOf(page)+1;
                int count=1;
                System.out.println("Page"+pagNum+":");
                for(COSName xObjectName : page.getResources().getXObjectNames()){
                    PDXObject pdxObject=page.getResources().getXObject(xObjectName);
                    if(pdxObject instanceof PDImageXObject){
                        PDImageXObject image=(PDImageXObject) pdxObject;
                        System.out.println("Found image with width "
                        +image.getWidth()
                        +"px and height "
                        +image.getHeight()
                        +"px.");
                        String filename="one-more-"+pagNum+"-"+count+".jpg";
                        //使用ImageIO把圖片保存到本地文件中
                        ImageIO.write(image.getImage(), "jpg", new File(filename));
                        count++;
                    }
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
