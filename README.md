TimkiemWeb is a versatile and user-friendly Java-based desktop application designed to facilitate the process of searching both news articles and e-commerce products efficiently. The application is built using Java Swing for the graphical user interface, Jsoup for web scraping, and the JSON library for processing structured data returned by APIs.

The application provides two main functionalities:

News Search:
Users can enter a keyword into the input field to search for relevant news articles across multiple popular Vietnamese news websites. Currently, the application supports searching on Dân Trí, VNExpress, and Vietnamnet. For each news article found, the program displays the title, the source of the news, and a direct link to the full article. Users can click on any link in the table to open the news article directly in their default web browser, allowing for quick access to updated information.

Product Search (Tiki E-commerce):
The product search feature allows users to find products from Tiki.vn efficiently. Users simply enter the product keyword, and the program fetches product information through Tiki’s public API. Each result shows the product name, original price (if any), current price, the source, and a direct link to the product page. The application also supports automatic handling of network restrictions by using proxy servers and retrying requests multiple times if the API blocks the connection or returns empty results. This ensures that users can access accurate pricing information even when facing network limitations or temporary blocks from the server.

Additional features include:

Tabbed Interface: The application organizes information into separate tabs: one for news articles and another for product search results. This design allows users to switch between news and product searches effortlessly.

Clickable Links: Both news articles and product results are displayed in a table format, and each link is clickable. Clicking a link will open the corresponding web page in the system’s default browser, providing a seamless user experience.

Responsive GUI: The graphical interface is designed with usability in mind, allowing users to perform searches quickly without dealing with complicated settings or commands.

Proxy Support and Retry Mechanism: To handle situations where requests might be blocked or fail due to network issues, the application automatically selects from a list of proxy servers and retries the request up to a predefined number of times.

In conclusion, TimkiemWeb is a practical and robust application aimed at saving time for users who frequently need to search for news and product information online. By integrating multiple sources, providing clear and clickable results, and handling network restrictions automatically, TimkiemWeb offers a complete solution for everyday information retrieval tasks.  

Link ytb: https://youtu.be/uRtFnDXhv_0?si=V8Nr6-4j3trNCL4W
