package org.example.pdf2;

public class Extension {

    //它包含三個主要字段，代表X.509擴充部分的基本組成部分，
    // 包括oid（對象標識符）、critical（表明此擴充是否是關鍵的）
    // 和value（擴充的值）。
    private String oid;//對象標識符
    private boolean critical;//表明此擴充是否是關鍵的
    private byte[] value;//擴充的值
    public String getOid(){
        return oid;
    }
    public byte[] getValue(){
        return value;
    }
    public boolean isCritical(){
        return critical;
    }
}
