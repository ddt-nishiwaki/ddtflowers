<%@page import="ddtflowers.DbConnect, java.sql.Connection"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%
    DbConnect dbConnect = new DbConnect();
    dbConnect.connect();
    System.out.println(dbConnect.getConnect());
    dbConnect.disconnect();
%>




