package org.example.pdf2;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PDFUtils {
    //fillData用于填充PDF中的表单字段（通常是用Adobe Acrobat创建的交互式PDFs）。
    // 它使用了iText库的AcroFields类来完成此操作。
    private static void fillData(AcroFields fields, Map<String, String> data) throws IOException, DocumentException, DocumentException {
        //创建一个空的ArrayList，用于存储已经设置值的字段名。
        List<String> keys = new ArrayList<String>();
        //檢查每個數據中的鍵是否存在於PDF表單字段中，如果存在，則用該鍵的值填充該字段。
        Map<String, AcroFields.Item> formFields = fields.getFields();
        for (String key : data.keySet()) {
            if (formFields.containsKey(key)) {
                String value = data.get(key);
                fields.setField(key, value); // 为字段赋值,注意字段名称是区分大小写的
                keys.add(key);
            }
        }
        //获取所有PDF表单字段名的迭代器
        Iterator<String> itemsKey = formFields.keySet().iterator();
        while (itemsKey.hasNext()) {
            String itemKey = itemsKey.next();
            if (!keys.contains(itemKey)) {
                fields.setField(itemKey, " ");
            }
        }
    }


    //功能是生成一個新的PDF，基於一個模板PDF並填充特定數據。
    public static String generatePDF(String templatePdfPath, String generatePdfPath, Map<String, String> data) {
        //初始化FileOutputStream和ByteArrayOutputStream。
        OutputStream fos = null;
        ByteArrayOutputStream bos = null;
        try {
            //從指定路徑讀取模板PDF。
            PdfReader reader = new PdfReader(templatePdfPath);
            //創建一個新的ByteArrayOutputStream以將PDF內容寫入內存。
            bos = new ByteArrayOutputStream();
            /* 将要生成的目标PDF文件名称 */
            PdfStamper ps = new PdfStamper(reader, bos);
            /* 使用中文字体 */
            String fontPath = "C:\\Windows\\Fonts\\msjhl.ttc,1"; // 微軟正黑體的路徑，這裡的 ",1" 指的是集合中的第二個字體（索引從 0 開始）
            BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            ArrayList<BaseFont> fontList = new ArrayList<BaseFont>();


            fontList.add(bf);
            /* 取出报表模板中的所有字段 */
            AcroFields fields = ps.getAcroFields();
            //對於那些可能不在模板中的字符，例如中文字符，設定替代字體。
            fields.setSubstitutionFonts(fontList);
            //使用先前給出的fillData方法填充PDF中的字段
            fillData(fields, data);
            /* 必须要调用这个，否则文档不会生成的  如果为false那么生成的PDF文件还能编辑，一定要设为true*/
            ps.setFormFlattening(true);
            ps.close();
            //FileOutputStream->文件輸出
            fos = new FileOutputStream(generatePdfPath);
            fos.write(bos.toByteArray());
            fos.flush();
            return generatePdfPath;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }
    public static void main(String[] args) {
        Map<String, String> data = new HashMap<String, String>();
        //key为pdf模板的form表单的名字，value为需要填充的值

        data.put("title", "台中榮總");
        data.put("case", "123456789");
        data.put("date", "2018.12.07");
        data.put("name", "julia");
        data.put("sex", "女");
        data.put("age", "18");
        data.put("phone", "13711645814");
        data.put("office", "耳鼻喉科");
        data.put("cert", "咳嗽");
        data.put("drug", "1、奥美拉唑肠溶胶囊 0.25g10粒×2板 ");
        data.put("dose", "×2盒");
        data.put("cons", "用法用量：口服 一日两次 一次2粒");
        data.put("tips", "温馨提示");
        data.put("desc", "尽量呆在通风较好的地方，保持空气流通，有利于病情康复。尽量呆在通风较好的地方");
        generatePDF("D:/test/ApachePDFBox_test1/tpl.pdf",
                "D:/test/ApachePDFBox_test1/filled.pdf", data );
    }
}
