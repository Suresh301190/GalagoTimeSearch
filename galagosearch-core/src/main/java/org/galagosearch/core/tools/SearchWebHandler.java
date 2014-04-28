// BSD License (http://www.galagosearch.org/license)

package org.galagosearch.core.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.galagosearch.core.parse.Document;
import org.galagosearch.core.tools.Search.SearchResult;
import org.galagosearch.core.tools.Search.SearchResultItem;
import org.galagosearch.tupleflow.Utility;
import org.mortbay.jetty.handler.AbstractHandler;
import org.znerd.xmlenc.XMLOutputter;

/**
 * <p>Handles web search requests against a Galago index.  Also handles XML requests for
 * documents, snippets and search results.</p>
 *
 * <p>This class is set up to work with an embedded Jetty instance, but it should be
 * fairly easy to wrap into a Servlet for use with something else (Tomcat, Glassfish, etc.)</p>
 *
 * <p>URLs supported:</p>
 *
 * <table>
 *   <tr>
 *     <td>/</td>
 *     <td>Main Page</td>
 *   </tr>
 *   <tr>
 *     <td>/search</td>
 *     <td>HTML Search Results (q, start, n)</td>
 *   </tr>
 *   <tr>
 *     <td>/xmlsearch</td>
 *     <td>XML Search Results (q, start, n)</td>
 *   </tr>
 *   <tr>
 *     <td>/snippet</td>
 *     <td>XML Snippet Result (identifier, term+)</td>
 *   </tr>
 *   <tr>
 *     <td>/document</td>
 *     <td>Document Result (identifier)</td>
 *   </tr>
 * </table>
 *
 * @author trevor
 */
public class SearchWebHandler extends AbstractHandler {
    Search search;

    public SearchWebHandler(Search search) {
        this.search = search;
    }

    public String getEscapedString(String text) {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c >= 128) {
                builder.append("&#" + (int)c + ";");
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    public void handleDocument(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getParameterMap();
        String identifier = request.getParameter("identifier");
        Document document = search.getDocument(identifier);
        response.setContentType("text/html; charset=UTF-8");

        PrintWriter writer = response.getWriter();
        writer.write(getEscapedString(document.text));
        writer.close();
    }

    public void handleSnippet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String identifier = request.getParameter("identifier");
        String[] terms = request.getParameterValues("term");
        Set<String> queryTerms = new HashSet<String>(Arrays.asList(terms));

        Document document = search.getDocument(identifier);

        if (document == null) {
            response.setStatus(response.SC_NOT_FOUND);
        } else {
        	
            response.setContentType("text/xml");
            PrintWriter writer = response.getWriter();
            String snippet = search.getSummary(document, queryTerms);
            String title = document.metadata.get("title");
            String url = document.metadata.get("url");

            if (snippet == null) snippet = "";

            response.setContentType("text/xml");
            writer.append("<response>\n");
            writer.append(String.format("<snippet>%s</snippet>\n", snippet));
            writer.append(String.format("<identifier>%s</identifier>\n", identifier));
            writer.append(String.format("<title>%s</title>\n", scrub(title)));
            writer.append(String.format("<url>%s</url>\n", scrub(url)));
            writer.append("</response>");
            writer.close();
        }
    }

    private String scrub(String s) throws UnsupportedEncodingException {
        if (s == null) return s;
        return s.replace("<", "&gt;")
                .replace(">", "&lt;")
                .replace("&", "&amp;");
    }

    public void retrieveImage(OutputStream output,HttpServletRequest request) throws IOException {
        InputStream image = getClass().getResourceAsStream(request.getPathInfo());
        Utility.copyStream(image, output);
        output.close();
    }

    public void handleImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OutputStream output = response.getOutputStream();
        response.setContentType("image/png");
        retrieveImage(output,request);
    }
    
    public void handleJs(HttpServletRequest request,HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
    	OutputStream output = response.getOutputStream();
        response.setContentType("text/javascript");
        retrieveJs(output,request);
		
	}

    public void retrieveJs(OutputStream output, HttpServletRequest request) throws IOException {
		// TODO Auto-generated method stub
    	InputStream css = getClass().getResourceAsStream(request.getPathInfo());
        Utility.copyStream(css, output);
        output.close();
		
	}
    public void handleCs(HttpServletRequest request,HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
    	OutputStream output = response.getOutputStream();
        response.setContentType("text/javascript");
        retrieveJs(output,request);
		
	}

    public void retrieveCs(OutputStream output, HttpServletRequest request) throws IOException {
		// TODO Auto-generated method stub
    	InputStream css = getClass().getResourceAsStream(request.getPathInfo());
        Utility.copyStream(css, output);
        output.close();
		
	}
	public void handleSearch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SearchResult result = performSearch(request);
        response.setContentType("text/html");
        String displayQuery = scrub(request.getParameter("q"));
        String encodedQuery = URLEncoder.encode(request.getParameter("q"), "UTF-8");

        PrintWriter writer = response.getWriter();
        writer.append("<html>\n");
        writer.append("<head>\n");
        writeStyle(writer);
        //writer.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"/css1/jquery.tooltip.css\"  />");
        //writer.append("<script type=\"text/javascript\" src=\"http://code.jquery.com/jquery.min.js\"></script>\n");
        //writer.append("<script type=\"text/javascript\" src=\"/js1/jquery.tooltip.js\"></script>\n");
        //writer.append("<script type=\"text/javascript\">$(document).ready(function(){ $j = jQuery.noConflict(); $j(document).ready(function(){$j(\"div#result\").tooltip();});});</script>");
        writer.append(String.format("<title>%s - Galago Search</title>\n", displayQuery));
        writer.append("<script type=\"text/javascript\">\n");
        writer.append("function toggleDebug() {\n");
        writer.append("   var object = document.getElementById('debug');\n");
        writer.append("   if (object.style.display != 'block') {\n");
        writer.append("     object.style.display = 'block';\n");
        writer.append("  } else {\n");
        writer.append("     object.style.display = 'none';\n");
        writer.append("  }\n");
        writer.append("}\n");
        writer.append("</script>\n");
        writer.append("</head>\n<body>\n");
        writer.append("<script type=\"text/javascript\" language=\"JavaScript1.2\">if (document.all||document.getElementById)document.body.style.background=\"url('/images/MainLogo1.png') white center no-repeat fixed\"</script>");
        writer.append("<div id=\"dhtmltooltip\"></div>\n"+

"<script type=\"text/javascript\">\n"+

"var offsetxpoint=-60\n"+
"var offsetypoint=20\n"+
"var ie=document.all \n"+
"var ns6=document.getElementById && !document.all \n"+
"var enabletip=false \n"+
"if (ie||ns6) \n"+
"var tipobj=document.all? document.all[\"dhtmltooltip\"] : document.getElementById? document.getElementById(\"dhtmltooltip\") : \"\" \n"+

"function ietruebody(){ \n"+
"return (document.compatMode && document.compatMode!=\"BackCompat\")? document.documentElement : document.body \n"+
"} \n"+

"function ddrivetip(thetext, thecolor, thewidth){ \n"+
"if (ns6||ie){ \n"+
"if (typeof thewidth!=\"undefined\") tipobj.style.width=thewidth+\"px\" \n"+
"if (typeof thecolor!=\"undefined\" && thecolor!=\"\") tipobj.style.backgroundColor=thecolor \n"+
"tipobj.innerHTML=thetext \n"+
"enabletip=true \n"+
"return false \n"+
"} \n"+
"} \n"+

"function positiontip(e){ \n"+
"if (enabletip){ \n"+
"var curX=(ns6)?e.pageX : event.clientX+ietruebody().scrollLeft; \n"+
"var curY=(ns6)?e.pageY : event.clientY+ietruebody().scrollTop; \n"+
"var rightedge=ie&&!window.opera? ietruebody().clientWidth-event.clientX-offsetxpoint : window.innerWidth-e.clientX-offsetxpoint-20 \n"+
"var bottomedge=ie&&!window.opera? ietruebody().clientHeight-event.clientY-offsetypoint : window.innerHeight-e.clientY-offsetypoint-20 \n"+

"var leftedge=(offsetxpoint<0)? offsetxpoint*(-1) : -1000 \n"+
"if (rightedge<tipobj.offsetWidth) \n"+
"tipobj.style.left=ie? ietruebody().scrollLeft+event.clientX-tipobj.offsetWidth+\"px\" : window.pageXOffset+e.clientX-tipobj.offsetWidth+\"px\" \n"+
"else if (curX<leftedge) \n"+
"tipobj.style.left=\"5px\" \n"+
"else \n"+
"tipobj.style.left=curX+offsetxpoint+\"px\" \n"+
"if (bottomedge<tipobj.offsetHeight) \n"+
"tipobj.style.top=ie? ietruebody().scrollTop+event.clientY-tipobj.offsetHeight-offsetypoint+\"px\" : window.pageYOffset+e.clientY-tipobj.offsetHeight-offsetypoint+\"px\" \n"+
"else \n"+
"tipobj.style.top=curY+offsetypoint+\"px\" \n"+
"tipobj.style.visibility=\"visible\" \n"+
"} \n"+
"} \n"+

"function hideddrivetip(){ \n"+
"if (ns6||ie){ \n"+
"enabletip=false \n"+
"tipobj.style.visibility=\"hidden\" \n"+
"tipobj.style.left=\"-1000px\" \n"+
"tipobj.style.backgroundColor='' \n"+
"tipobj.style.width='' \n"+
"} \n"+
"} \n"+

"document.onmousemove=positiontip \n"+

"</script> \n");
        writer.append("<div id=\"header\">\n");
        writer.append("<table ><tr >");
        writer.append("<td width=\"15%\"><a href=\"http://www.galagosearch.org\">" +"<img src=\"/images/MainLogo4.png\" width=\"100%\"></a></td>");
        writer.append("<td width=\"85%\"><br/><form action=\"search\"><input type=\"text\" name=\"q\" class=\"search rounded\" size=\"70\" style=\"height: 30px\"><input value=\"Search\" class=\"search rounded\" type=\"submit\" style=\"height: 27px; width:80px\" ></form></td>");
        writer.append("</tr>");
        writer.append("</table>\n");
        writer.append("</div>\n");

        /*writer.append("<center>[<a href=\"#\" onClick=\"toggleDebug(); return false;\">debug</a>]</center>");
        writer.append("<div id=\"debug\">");
        writer.append("<table>");
        writer.append(String.format("<tr><td>%s</td><td>%s</td></tr>",
                      "Parsed Query", result.query.toString()));
        writer.append(String.format("<tr><td>%s</td><td>%s</td></tr>",
                      "Transformed Query", result.transformedQuery.toString()));
        writer.append("</table>");
        writer.append("</div>");*/

        for (SearchResultItem item : result.items) {
        	writer.append("<table style=\"width:100%\">");
        	writer.append("<tr>\n");
        	writer.append("<td width=\"15%\"></td>\n");
        	writer.append("<td width=\"55%\" >\n");
        	//String s=item.summary;
            writer.append(String.format("<div onMouseover=\"ddrivetip('%s','#990033')\"; onMouseout=\"hideddrivetip()\" id=\"result\">\n","Publication Date : "+item.publication.toString()));
            writer.append(String.format("<a href=\"document?identifier=%s\">%s</a><br/>" +
                                        "<div id=\"meta\">%s - %s</div>\n",
                                        item.identifier,
                                        item.displayTitle,
                                        item.summary,
                                        item.displayTitle,
                                        scrub(item.identifier),
                                        item.publication.toString(),
                                        scrub(item.url)));
            writer.append("</div>\n");
           // writer.append(" <div class=\"tooltip_description\" style=\"display:none\" title=\"Item 1 Description\">th desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.</div>");
            writer.append("</td>\n");
            writer.append("<td width=\"15%\"></td>\n");
            writer.append("</tr>");
            writer.append("</table>");
        }

        String startAtString = request.getParameter("start");
        String countString = request.getParameter("n");
        int startAt = 0;
        int count = 10;

        if (startAtString != null) {
            startAt = Integer.parseInt(startAtString);
        }
        if (countString != null) {
            count = Integer.parseInt(countString);
        }

        writer.append("<center>\n");
        if (startAt != 0) {
            writer.append(String.format("<a href=\"search?q=%s&start=%d&n=%d\">Previous</a>",
                                        encodedQuery, Math.max(startAt-count,0), count));
            if (result.items.size() >= count) {
                writer.append(" | ");
            }
        }

        if (result.items.size() >= count) {
            writer.append(String.format("<a href=\"search?q=%s&start=%d&n=%d\">Next</a>",
                                        encodedQuery, startAt+count, count));
        }
        writer.append("</center>");
        writer.append("</body>");
        writer.append("</html>");
        writer.close();
    }

    public void handleSearchXML(HttpServletRequest request, HttpServletResponse response) throws IllegalStateException, IllegalArgumentException, IOException, Exception {
        SearchResult result = performSearch(request);
        PrintWriter writer = response.getWriter();
        XMLOutputter outputter = new XMLOutputter(writer, "UTF-8");
        response.setContentType("text/xml");

        outputter.startTag("response");

        writer.append("<response>\n");
        for (SearchResultItem item : result.items) {
            outputter.startTag("result");
            
            outputter.startTag("identifier");
            outputter.pcdata(item.identifier);
            outputter.endTag();
            
            outputter.startTag("title");
            outputter.pcdata(item.displayTitle);
            outputter.endTag();

            outputter.startTag("url");
            outputter.pcdata(item.url);
            outputter.endTag();

            outputter.startTag("snippet");
            outputter.pcdata(item.summary);
            outputter.endTag();

            outputter.startTag("rank");
            outputter.pcdata("" + item.rank);
            outputter.endTag();
            
            outputter.startTag("metadata");
            for (Entry<String, String> entry : item.metadata.entrySet()) {
                outputter.startTag("item");
                outputter.startTag("key");
                outputter.pcdata(entry.getKey());
                outputter.endTag();
                outputter.startTag("value");
                outputter.pcdata(entry.getValue());
                outputter.endTag();
            }
            
            outputter.endTag();
        }
    }

    public void writeStyle(PrintWriter writer) {
    	writer.write("<style type=\"text/css\">\n");
    	writer.write(".highlight { background-color: #C0C0C0 }");
    	writer.write("body  { font-family: Helvetica, sans-serif;}\n");
    	writer.write(".rounded {border-radius:15px;	-moz-border-radius:15px; background: -webkit-linear-gradient(top, #dcdcdc 0%,#808080 100%);  -webkit-border-radius:15px;}");
    	writer.write(".lighter {width:95%;	height:50px;padding:40px 25px; }");
    	writer.write("input[type=submit], input[type=submit]:hover{position:relative; left:-85px;border:1px solid #adc5cf; background: -webkit-linear-gradient(top, #39C63A 0%,#708F70 100%); color:#72aa;cursor: pointer;}");
        writer.write("img { border-style: none; }\n");
        writer.write("#box { solid #ccc; margin: 10px auto; width: 500px;\" }\n");
        writer.write("#box a { font-size: small; text-decoration: none; }\n");
        writer.write("#box a:link { color: rgb(0, 93, 40); }\n");
        writer.write("#box a:visited { color: rgb(90, 93, 90); }\n");
        writer.write("#header { background: rgb(0, 0, 0); border: 1px solid #ccc; }\n");
        writer.write("#result { padding: 10px 5px; max-width: 550px; solid #ccc;}\n");
        writer.write("#meta { font-size: small; color: rgb(60, 100, 60); }\n");
        writer.write("#summary { font-size: small; }\n");
        writer.write("#dhtmltooltip{border-radius:15px;	-moz-border-radius:15px;-webkit-border-radius:15px; position: absolute;width: 150px;border: 2px solid black;padding: 2px;background: -webkit-linear-gradient(top, #dcdcdc 0%,#808080 100%);visibility: hidden;z-index: 100;");
        writer.write("#dhtmlpointer{position:absolute;left: -300px;z-index: 101;visibility: hidden;}");
        writer.write("#debug { display: none; }\n");
        writer.write("</style>");
    }

    public void handleMainPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        
        response.setContentType("text/html");

        writer.append("<html background-size: cover; >\n");
        writer.append("<head>\n");
        //writeStyle(writer);
        writer.write("<style type=\"text/css\">\n");
    	writer.write(".rounded {border-radius:15px;	-moz-border-radius:15px; background: -webkit-linear-gradient(top, #dcdcdc 0%,#808080 100%);-webkit-border-radius:15px;}");
    	writer.write(".lighter {width:95%;	margin:25px; height:50px;padding:80px 55px;}");
    	writer.write("input[type=submit], input[type=submit]:hover{position:relative; left:-85px;border:1px solid #adc5cf; background: -webkit-linear-gradient(top, #39C63A 0%,#708F70 100%); color:#72aa;cursor: pointer;}");
    	writer.write("html {background: url(/images/NYT1.png) no-repeat center center fixed;-webkit-background-size: cover;-moz-background-size: cover;-o-background-size: cover;background-size: cover;}");
    	writer.write("body  { font-family: Helvetica, sans-serif; background-size:cover;background-repeat: no-repeat; fixed; }\n");
        writer.write("img { border-style: none; }\n");
        writer.write("#box { solid #ccc; margin: 0; display:inline-block; position:relative; }\n");
        writer.write("#header { background: rgb(210, 233, 217); border: 1px solid #ccc; }\n");
        writer.write("</style>");
        writer.append("<title><FONT COLOR=\"00FF00\">Chronons</FONT></title></head>");
        writer.append("<body></a><br/>\n");
        
        writer.append("<center><br/><br/><div id=\"lighter\" style = \"margin-left: 0px;\" >\n");
        writer.append("<form action=\"search\"><input type=\"text\" name=\"q\" class=\"search rounded\" size=\"80\" style=\"height: 40px\" placeholder=\"Enter your query and start the time machine..\"><input value=\"Search\" class=\"search rounded\" type=\"submit\" style=\"height: 37px; width:80px\" ></span></form><br/><br/>");
        writer.append("</div></center></body></html>\n");
        
        writer.close();
    }

    public void handle(String target,HttpServletRequest request, HttpServletResponse response,int dispatch) throws IOException, ServletException {
        if (request.getPathInfo().equals("/search")) {
            try {
                handleSearch(request, response);
            } catch(Exception e) {
                throw new ServletException("Caught exception from handleSearch", e);
            }
        } else if (request.getPathInfo().equals("/document")) {
            handleDocument(request, response);
        } else if (request.getPathInfo().equals("/searchxml")) {
            try {
                handleSearchXML(request, response);
            } catch(Exception e) {
                throw new ServletException("Caught exception from handleSearchXML", e);
            }
        } else if (request.getPathInfo().equals("/snippet")) {
            handleSnippet(request, response);
        } else if (request.getPathInfo().startsWith("/images")) {
            handleImage(request, response);
        } else if(request.getPathInfo().equals("/js1")){
        	handleJs(request,response);
        } else if(request.getPathInfo().equals("/css1")){
        	handleCs(request,response);
        }else {
            handleMainPage(request, response);
        }
    }

    

	private SearchResult performSearch(HttpServletRequest request) throws Exception {
        String query = request.getParameter("q");
        String startAtString = request.getParameter("start");
        String countString = request.getParameter("n");
        int startAt = (startAtString == null) ? 0 : Integer.parseInt(startAtString);
        int resultCount = (countString == null) ? 10 : Integer.parseInt(countString);
        SearchResult result = search.runQuery(query, startAt, resultCount, true);
        return result;
    }
}
