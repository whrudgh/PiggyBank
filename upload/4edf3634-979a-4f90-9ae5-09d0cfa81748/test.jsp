<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta charset="UTF-8">
        <title>JSP 테스트 페이지</title>
    </head>

    <body>
        <h1>JSP 테스트 페이지</h1>
        <p>현재 시간: <%= new java.util.Date() %>
        </p>
        <p>서버 정보: <%= application.getServerInfo() %>
        </p>
        <p>JSP 버전: <%= JspFactory.getDefaultFactory().getEngineInfo().getSpecificationVersion() %>
        </p>
    </body>

    </html>