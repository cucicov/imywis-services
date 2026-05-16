package com.example.imywisservices.controller;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<String> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == 404) {
                String html = """
                        <!doctype html>
                        <html lang="en">
                          <head>
                            <meta charset="utf-8"/>
                            <meta name="viewport" content="width=device-width, initial-scale=1"/>
                            <title>Page Not Found</title>
                            <link rel="icon" type="image/svg+xml" href="/favicon.svg"/>
                            <style>
                              * {
                                margin: 0;
                                padding: 0;
                                box-sizing: border-box;
                              }
                              body {
                                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                                min-height: 100vh;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                color: #fff;
                              }
                              .container {
                                text-align: center;
                                padding: 2rem;
                              }
                              .error-code {
                                font-size: 8rem;
                                font-weight: 700;
                                line-height: 1;
                                margin-bottom: 1rem;
                                text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
                              }
                              .error-message {
                                font-size: 2rem;
                                font-weight: 300;
                                margin-bottom: 2rem;
                                text-shadow: 1px 1px 2px rgba(0,0,0,0.2);
                              }
                              .home-link {
                                display: inline-block;
                                padding: 1rem 2rem;
                                background: rgba(255, 255, 255, 0.2);
                                border: 2px solid #fff;
                                border-radius: 50px;
                                color: #fff;
                                text-decoration: none;
                                font-size: 1.1rem;
                                font-weight: 500;
                                transition: all 0.3s ease;
                                backdrop-filter: blur(10px);
                              }
                              .home-link:hover {
                                background: rgba(255, 255, 255, 0.3);
                                transform: translateY(-2px);
                                box-shadow: 0 4px 12px rgba(0,0,0,0.2);
                              }
                            </style>
                          </head>
                          <body>
                            <div class="container">
                              <div class="error-code">404</div>
                              <div class="error-message">Page Not Found</div>
                              <a href="/" class="home-link">Go to Home</a>
                            </div>
                          </body>
                        </html>
                        """;

                return ResponseEntity.status(404)
                        .contentType(MediaType.TEXT_HTML)
                        .body(html);
            }
        }

        // For other errors, return a generic error page
        String html = """
                <!doctype html>
                <html lang="en">
                  <head>
                    <meta charset="utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1"/>
                    <title>Error</title>
                    <link rel="icon" type="image/svg+xml" href="/favicon.svg"/>
                    <style>
                      * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                      }
                      body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: #fff;
                      }
                      .container {
                        text-align: center;
                        padding: 2rem;
                      }
                      .error-message {
                        font-size: 2rem;
                        font-weight: 300;
                        margin-bottom: 2rem;
                        text-shadow: 1px 1px 2px rgba(0,0,0,0.2);
                      }
                      .home-link {
                        display: inline-block;
                        padding: 1rem 2rem;
                        background: rgba(255, 255, 255, 0.2);
                        border: 2px solid #fff;
                        border-radius: 50px;
                        color: #fff;
                        text-decoration: none;
                        font-size: 1.1rem;
                        font-weight: 500;
                        transition: all 0.3s ease;
                        backdrop-filter: blur(10px);
                      }
                      .home-link:hover {
                        background: rgba(255, 255, 255, 0.3);
                        transform: translateY(-2px);
                        box-shadow: 0 4px 12px rgba(0,0,0,0.2);
                      }
                    </style>
                  </head>
                  <body>
                    <div class="container">
                      <div class="error-message">Something went wrong</div>
                      <a href="/" class="home-link">Go to Home</a>
                    </div>
                  </body>
                </html>
                """;

        return ResponseEntity.status(500)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
