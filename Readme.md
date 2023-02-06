## General info

That project was created to simulate requests to generated API
by [internal_spring_api_requester](https://github.com/bodyangug/internal_spring_api_requester).

## Workflow

Prerequisite:

* check that folder ```files``` was created at the root of the project and here file with metadata exists.

1. Provide necessary path to param.
2. Configure app.properties file
3. Run using ``main`` method
4. Wait until app will have asked all urls

## Files folder

In that folder you should place file with metadata to all
created controllers and define that file while you request call method.
That file contains next json data structure:

- Array of:
    - type: type of HTTP method
    - url: url to that controller
    - numberOfParams: count of params

## Properties

``api.endpoint`` - endpoint of admin app. Here will be sent next payloads: START/STOP session, ADD_TESTS type of
payloads;

``api.authorization`` - using by header of authorization in admin API;

``api.batch.size`` - size of batch that will be sent using ADD_TESTS type of payload;

``api.external.url`` - URL of API with generated count of controller.
