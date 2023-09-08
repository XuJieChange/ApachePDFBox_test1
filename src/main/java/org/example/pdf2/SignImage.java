package org.example.pdf2;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class SignImage {//製作印章
    public static boolean createSignTextImg(//回傳true or false
            String doctorName,//醫生名
            String hospitalName,//醫院名
            String date,//簽章日期
            String jpgName){//輸出的JPEG名稱
        int width=255;//印章的長寬
        int height=100;
        FileOutputStream out=null;//初始化为 null
        Color bgcolor=Color.WHITE;//背景色
        Color fontcolor=Color.RED;//字的顏色
        //字體
        Font doctorNameFont=new Font(null, Font.BOLD,20);
        Font otherTextFont=new Font(null,Font.BOLD,18);

        try{
            BufferedImage bimage=new BufferedImage(width,height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g=bimage.createGraphics();
            g.setColor(bgcolor);
            g.fillRect(0,0,width,height);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);//抗鋸齒狀

            //创建了一个 8 像素宽的红色边框
            g.setColor(Color.RED);
            g.fillRect(0,0,8,height);
            g.fillRect(0,0,width,8);
            g.fillRect(0,height-8,width,height);
            g.fillRect(width-8,0,width,height);

            g.setColor(fontcolor);
            g.setFont(doctorNameFont);
            //是 Java 2D API 提供的一個類，用於獲得特定字體的度量信息
            // （例如字符的寬度、高度等）。這行代碼用於獲取 doctorNameFont
            // 字體的度量資訊。
            FontMetrics fm=g.getFontMetrics(doctorNameFont);
            int font1_Hight=fm.getHeight();
            int strWidth=fm.stringWidth(doctorName);
            int y=35;
            int x=(width-strWidth)/2;
            g.drawString(doctorName,x,y);

            g.setFont(otherTextFont);
            fm=g.getFontMetrics(otherTextFont);
            int font2_Hight = fm.getHeight();
            strWidth = fm.stringWidth(hospitalName);
            x = (width - strWidth) / 2;
            g.drawString(hospitalName, x, y + font1_Hight); // 在指定坐标除添加文字

            strWidth = fm.stringWidth(date);
            x = (width - strWidth) / 2;
            g.drawString(date, x, y + font1_Hight + font2_Hight); // 在指定坐标除添加文字
            g.dispose();

            out = new FileOutputStream(jpgName);//打開 jpgName 文件以供寫入的方式
            ImageWriter writer = ImageIO.getImageWritersByFormatName("JPEG").next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(out);
            writer.setOutput(ios);

            //影像壓縮
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.5f); // 這裡的0.5f對應於原來的50f

            writer.write(null, new IIOImage(bimage, null, null), param);

            ios.flush();
            out.flush();
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        createSignTextImg("黃偉浩", "台中榮總", "2023.09.04",   "sign.jpg");
    }
}
