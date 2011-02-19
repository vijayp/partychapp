package com.imjasonh.partychapp.urlinfo;

import junit.framework.TestCase;

import java.net.URI;

public class SimpleUrlInfoServiceTest extends TestCase {
  public void testBasicTitle() {
    assertEquals(new UrlInfo("title", ""), getUrlInfo("foo<title>title</title>bar"));
  }
  
  public void testNoTitle() {
    assertEquals(new UrlInfo("", ""), getUrlInfo("adasdasd"));
  }
  
  public void testNoContents() {
    assertEquals(new UrlInfo("", ""), getUrlInfo(null));    
  }
  
  public void testTitleWhitespace() {
    assertEquals(
        new UrlInfo("title foo bar", ""),
        getUrlInfo("foo<title>title   foo\n\n  bar   </title>bar"));    
  }
  
  public void testTitleWithAttributes() {
    assertEquals(
        new UrlInfo("title", ""),
        getUrlInfo("foo<title id=\"the-title\">title</title>bar"));    
  }
  
  public void testTitleWithEscapedHtml() {
    assertEquals(
        new UrlInfo("Python quiz \u00AB Vijay Pandurangan's blog", ""),
        getUrlInfo("<title>Python quiz &laquo; Vijay Pandurangan&#039;s blog</title>"));    
  }  

  private static UrlInfo getUrlInfo(final String html) {
    return new SimpleUrlInfoService() {
      @Override protected String getUriContents(URI uri) {
        return html;
      }
    }.getUrlInfo(URI.create("http://www.example.com/"));
  }
}
