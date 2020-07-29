# inventory-task
## Build
mvn clean install


## Run
mvn spring-boot:run

## Endpoints
`@GetMapping("/inventory")`


`@GetMapping("/inventory/{item-id}")`


`@PostMapping("/buy")`

`/buy` endpoint is protected by basic authentication with base64 encrypted password. Id is `tariq` and password is `gilded-roses`.

Can also add API key for additional security. Have some sample code in a separate branch `api-key-addition` but that hasn't been tested yet.


Sample body
contains items to buy with quantities requested and total money provided


`{
    "requestedItems": [
        {
            "id": 1,
            "quantity": 4
        },
        {
            "id": 2,
            "quantity": 3
        }
    ],
    "money": "40"
}`


Sample response
contains items' information with quantities secured and money left.


`{
    "requestedItems": [
        {
            "id": 1,
            "quantity": 4,
            "name": "Chips",
            "price": "3.40"
        },
        {
            "id": 2,
            "quantity": 3,
            "name": "Fish",
            "price": "7.80"
        }
    ],
    "money": "20"
}`

Config used for testing purpose
application.properties


`app.surge.pricing.duration.ms=60000 // time period in milliseconds within which number of requests exceed the surge threshold.`

`app.calls.surge.call.limit=3 // number of calls to decide a surge.`

Surge logic:
1. First call for a product is made through `/inventory/{id}` is made at 12 pm with price $10.00.
2. If 10th call is made at 12:50 and 14th call is made at 13:05 just after 13:00 for same id, then surge price is enforced for this item id. Price becomes $11.00.
3. Now till 14:05 there will be no increase.
4. After 14:05 if the total number of calls within last hour is more than 10, one more surge is applied and price becomes $11.10.
5. Now for next hour till 15:05, number of calls is less than threshold, so item price goes back 2 levels at $10.
