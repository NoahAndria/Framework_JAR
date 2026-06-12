package controllers;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class FrontControllerServlet extends HttpServlet{

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        handleRequest(req, res);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        handleRequest(req, res);
    }

    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
          System.out.println("FrontControllerServlet: forwarding to /pageAcceuil.jsp");
          RequestDispatcher dispat = req.getRequestDispatcher("/WEB-INF/views/pageAcceuil.jsp");
          dispat.forward(req, res);
    }
}