package me.zxia.learn.chat.protocol;

public class Header {

    private int version = 1;
    private int length;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
