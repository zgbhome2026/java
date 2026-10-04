# Shopping Java App

Spring Boot 2.7 / Java 17 backend for Tomcat 9 + PostgreSQL.

## Separated areas

- `auth/` — register, login, logout, login history, sessions
- `product/` — product browsing
- `cart/` — add/update/view shopping cart
- `checkout/` — create order, reduce stock, clear cart

## Build image

```bash
docker build -t java-shopping-app:latest .
```

## Kubernetes environment variables

```yaml
env:
  - name: SPRING_DATASOURCE_URL
    value: "jdbc:postgresql://postgres-svc:5432/shoppingdb"
  - name: SPRING_DATASOURCE_USERNAME
    value: "admin"
  - name: SPRING_DATASOURCE_PASSWORD
    value: "secretpassword"
```

## APIs

Register:
```bash
curl -X POST http://NODE_IP:30080/api/auth/register   -H 'Content-Type: application/json'   -d '{"username":"john","email":"john@example.com","password":"Password123!"}'
```

Login:
```bash
curl -X POST http://NODE_IP:30080/api/auth/login   -H 'Content-Type: application/json'   -d '{"username":"john","password":"Password123!"}'
```

The login response returns a token. Use it like:

```bash
-H 'X-Session-Token: TOKEN_HERE'
```

Products:
```bash
curl http://NODE_IP:30080/api/products
```

Add item:
```bash
curl -X POST http://NODE_IP:30080/api/cart/items   -H 'Content-Type: application/json'   -H 'X-Session-Token: TOKEN_HERE'   -d '{"productId":1,"quantity":2}'
```

View cart:
```bash
curl http://NODE_IP:30080/api/cart   -H 'X-Session-Token: TOKEN_HERE'
```

Checkout:
```bash
curl -X POST http://NODE_IP:30080/api/checkout   -H 'X-Session-Token: TOKEN_HERE'
```

Orders:
```bash
curl http://NODE_IP:30080/api/orders   -H 'X-Session-Token: TOKEN_HERE'
```

Passwords are stored as BCrypt hashes, not plaintext.
