package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class ReadPDFExample {
    public static void main(String[] args){
        //創建文件對象
        File file=new File("one-more.pdf");
        try {
            //創建PDF文黨對象
            PDDocument document=PDDocument.load(file);
            //創建PDF文本玻璃器
            PDFTextStripper stripper=new PDFTextStripper();
            //獲取PDF文件全部內容
            String text=stripper.getText(document);
            //輸出PDF文黨對象
            System.out.println(text);
            //關閉PDF文黨對象
            document.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
