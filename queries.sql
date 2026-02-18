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

/*Realistic Sales History*/
SELECT
   EXTRACT(HOUR FROM order_time) AS hour_of_day,
   COUNT(order_id) AS num_orders,
   SUM(total_price) AS hourly_revenue
FROM Orders
GROUP BY hour_of_day
ORDER BY hour_of_day;

