# BFF
BFF Project for the relationship between Storage and ZooStore

  Custom Operation - Discount for newly registered user.
The client has a 5% discount for the first order. The discount is valid 1 week after registration and is applied automatically.
BFF business logic - We get the user, then check if he is valid for the discount
(valid means that he is registered at least 1 week before the sale of the cart and has no old orders)
Then we get all records from the database and map them to object with info about itemId, quantity, priceWithDiscount, price.
Also we calculate the total price with the applied discount.
Then we send request to storage to perform export item operation and save the item to sold_items_history table.
At the end we delete all records from cart table by userId.
The method returns all sold items and the total price with applied discount.
