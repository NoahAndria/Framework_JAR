package myframework.utils;

public class UrlMethod{
    String url;
    String method;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object o){
        UrlMethod u = (UrlMethod) o;
        return ((u.getUrl().toUpperCase().equals(this.getUrl().toUpperCase())) && (u.getMethod().toUpperCase().equals(this.getMethod().toUpperCase())));
    }


}