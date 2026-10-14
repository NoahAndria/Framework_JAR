package myframework.utils;

public class MapFile{
    String fileName;
    String fileExtension;
    byte[] fileContent;

    public MapFile(){
        this.fileName = "";
        this.fileExtension = "";
        this.fileContent = new byte[0];
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

}