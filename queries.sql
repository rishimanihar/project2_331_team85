SELECT
   EXTRACT(WEEK FROM order_time) AS week_number,
   COUNT(order_id) AS total_orders
FROM Orders
GROUP BY week_number
ORDER BY week_number;
