package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;

public class CreatePDF {
    public static void main(String[] args){
        PDDocument document=new PDDocument();
        PDPage page=new PDPage();
        document.addPage(page);
        //設定字體
        PDType1Font font= PDType1Font.HELVETICA_BOLD;

        try{
            PDPageContentStream contentStream=new PDPageContentStream(document, page);
            contentStream.beginText();
            //字體大小為12
            contentStream.setFont(font,12);
            //將當前的筆跡位置移動到右邊100單位和上面700單位的位置
            contentStream.newLineAtOffset(100,700);
            contentStream.showText("Hello World");
            contentStream.endText();
            contentStream.close();
            //創建檔案
            document.save(new File("one-more.pdf"));
            document.close();
            System.out.println("PDF created successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
