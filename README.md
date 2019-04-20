# Full-Stack-Recommendation-Project

## Data
The data is actually what you can see in the front-end pages. 

That is, the Data from both external APIs and Database's tables, when they are sent to the front-end, will be converted into raw JSON file. Then the raw JSON file will be presented to users in the forms of images, fancy font, and links.

## SQL
All the queries, as well as inserts, delete, and update SQL statements, are embedded into the web service. 

Specifically, all the SQL statements are in the package 'db'. The MySQLConnection class includes all the inserts, queries, update, and delete SQL statements, which are corresponded with Java methods. For example, the 'getFavoriteItems' method include SQL query 'SELECT * FROM items WHERE item_id = ?', while the 'saveItem' method include SQL insert 'INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)'.

## Codes
All the codes, such as front-end codes (HTML/CSS/JavaScript), back-end codes(Java), and Database(SQL), are included.

The front-end codes are in the WebContent folder. The back-end codes are in the src folder. And the SQL codes are embedded in Java methods.

## How to execute
The entire web service has already been running in the AWS. And since the SQL statements are embedded in the service, as a user you just need to click the buttons or links, which will trigger the corresponding back-ned methods, inside which the SQL statments are.

For example, if you like an event and you want to save it, you can just click the 'love' icon. Then, the Eventlistener will trigger the REST API in the Servlet, and the REST API will call a method 'setFavoriteItems'. Inside this method, there is a SQL insert statement 'INSERT IGNORE INTO history (user_id, item_id) VALUES(?, ?)' and once this method is called, this SQL statement will be executed. At last, the event you want to save will be actually inserted into the Database table 'history'.

Also, if you want to see the events you save, you can just click the button 'Favorites'. Then, based on your userId, the method 'getFavoriteItemIds' and 'getFavoriteItems' will be called. And the SQL query statement 'SELECT item_id FROM history WHERE user_id = ?' and 'SELECT * FROM items WHERE item_id = ?' will also be executed. Thus, you can finally see the SQL query results, which are your favorite events, in the front-end pages.
