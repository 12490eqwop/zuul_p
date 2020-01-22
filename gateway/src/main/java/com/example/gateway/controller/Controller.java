package com.example.gateway.controller;


import com.netflix.zuul.http.ZuulServlet;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.annotation.WebServlet;

@RequestMapping("/*")
@WebServlet
public class Controller extends ZuulServlet {
}
