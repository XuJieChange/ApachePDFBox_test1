package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;

public class InsertImageInPDF {
    public static void main(String[] args) {
        try {
            //加載PDF文件
            PDDocument document = PDDocument.load(new File("one-more.pdf"));

            //獲取第一頁
            PDPage page = document.getPage(0);

            //加仔圖片
            PDImageXObject image = PDImageXObject.createFromFile("src/main/resources/one-more.jpg", document);

            //在指定位置加入圖像
            PDPageContentStream contentStream=new PDPageContentStream(document,page, PDPageContentStream.AppendMode.APPEND,true,true);
            contentStream.drawImage(image, 200, 500, image.getWidth(), image.getHeight());

            contentStream.close();
            document.save("one-more-jpa.pdf");
            document.close();
            System.out.println("PDF created successfully");

        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
