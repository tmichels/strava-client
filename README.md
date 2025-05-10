# Strava client

## Description
Run `mvn package` to let OpenApi generate the Strava model as per contract published by Strava 
https://developers.strava.com/swagger/swagger.json.

Set STRAVA_CLIENT_ID and STRAVA_CLIENT_SECRET as environment variables.

Run application and call endpoint at http://localhost:8080/athlete. You will be redirected to Strava if not yet 
logged in. After logging in with Strava, your token is used to get Strava content.