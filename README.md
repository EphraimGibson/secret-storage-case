# Case: Secret Storage

## Background

You've just joined a small team that has built a simple internal service. The application is a Spring Boot web app that requires authentication to access. It's packaged as a Docker container and run locally.

Your tech lead has asked you to take a look at the repository and address any security concerns you find with how the application is configured — particularly around how secrets are handled.

## Your Task

1. **Identify the security problem** in this repository.
2. **Fix it.** Implement a solution that resolves the issue.
3. **Describe your reasoning.** Briefly explain why the current approach is problematic and how your solution addresses it. If your ideal solution involves infrastructure or tooling beyond this repo, feel free to describe what that would look like — you don't need to build it, but show us your thinking.

## Running the Application

The application reads its credentials at runtime else it would not start:

- `APP_ADMIN_USERNAME`: the Basic Auth username
- `APP_ADMIN_PASSWORD_HASH`: a bcrypt hash of the password

Generate a bcrypt hash once and store it in your secret manager or local environment. Do not commit the raw password or hash to the repository.

Build the image:

```sh
docker build -t secret-case .
```

Run the container:

```sh
docker run \
  -p 8080:8080 \
  -e APP_ADMIN_USERNAME='<username-set-me>' \
  -e APP_ADMIN_PASSWORD_HASH='<bcrypt-hash-set-me>' \
  secret-case
```

Then visit [http://localhost:8080](http://localhost:8080). You'll be prompted for the configured credentials.

You can also test with curl:

```sh
curl -u '<username-set-me>:<password-set-me>' http://localhost:8080
```

`APP_ADMIN_PASSWORD_HASH` must contain a bcrypt hash, not the plaintext password. The application uses `BCryptPasswordEncoder` to compare the supplied password with the stored hash.

## What We're Looking For

- Can you spot the problem?
- Is your solution practical and well-reasoned?
- How do you think about this in a broader context (local development, CI/CD, production)?
- No more than 15 minutes presentation on your solution and thoughts

There is no single "right" answer. We want to see how you think.

## Time Expectation

This should not take long. Don't over-engineer it. A clean, simple fix with clear reasoning is preferred over a complex one. You can hand-wave external services that might be part of your ideal design.

## Security Notes

* The original application stored the password in the source code and checked the Basic Auth header in the controller.
* The application now reads the username and bcrypt password hash from the runtime environment.
* Spring Security handles Basic Auth, checks the password hash, and protects every endpoint.
* The application is stateless and does not need to store login sessions.
* Repeated failed attempts for a username are limited in memory for this exercise.
* In production, rate limiting should also run at a gateway, WAF, load balancer, or reverse proxy.
* Production rate limiting should include the client IP address and use shared storage when multiple application instances are running.
* Basic Auth should only be used over HTTPS outside local development.
* Runtime secrets and password hashes should be stored in a secret manager and never committed to the repository.