/* Weekly Sales History
SELECT
   EXTRACT(WEEK FROM order_time) AS week_number,
   COUNT(order_id) AS total_orders
FROM Orders
GROUP BY week_number
ORDER BY week_number;
*/


/*Peak Sales Day
SELECT
   DATE(order_time) AS sale_day,
   SUM(total_price) AS daily_revenue
FROM Orders
GROUP BY sale_day
ORDER BY daily_revenue DESC
LIMIT 10;
*/

/*Realistic Sales History
   EXTRACT(HOUR FROM order_time) AS hour_of_day,
   COUNT(order_id) AS num_orders,
   SUM(total_price) AS hourly_revenue
FROM Orders
GROUP BY hour_of_day
ORDER BY hour_of_day;
*/


/*  Top 5 orders that spent the most money in a single transaction. 

SELECT order_id, total_price
FROM Orders
ORDER BY total_price DESC
LIMIT 5;

*/


/* Question: List the average order value

SELECT ROUND(AVG(total_price), 2) AS avg_ticket_price FROM Orders;

*/


/* Question: What is the total count of items in the inventory currently?
SELECT SUM(quantity) as total_stock_count
FROM Inventory;
*/

/* testing branch */


