# iot-heroku-server
Playing around with Internet of Things on Heroku

## Installing on heroku

Fork and clone. Then:

```
heroku create
heroku addons:create m2x
heroku addons:create heroku-redis:hobby-dev
heroku config -s > .env # to work with M2X using heroku local
git push heroku master
```

Now enjoy the app.

Below are commands to help you get started. `WEB_URL` env can be set by executing this command:

```
export WEB_URL=$(heroku apps:info | grep 'Web URL' | awk -F 'Web URL:' '{print $2}' | xargs)
```

Assuming device id is:

```
export DEVICE_ID=123
```

1. Create device:

```
curl -X POST \
-d deviceId=$DEVICE_ID \
${WEB_URL}v1/registry/devices
```

2. Create metric:

```
curl -X POST \
-d streams=steps -d types=numeric -d units=count \
${WEB_URL}v1/registry/devices/$DEVICE_ID/streams
```

3. Push some telemetry data:


```
t1=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
t2=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
t3=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
curl -X POST \
-d v=1 -d t=$t1 -d v=2 -d t=$t2 -d v=3 -d t=$t3 \
${WEB_URL}v1/telemetry/$DEVICE_ID/steps
```

4. Get device info:

```
curl -X GET \
${WEB_URL}v1/registry/devices/$DEVICE_ID
```

I wrote a simple android app that publishes steps and a mock metric to this app.
Just need to find some more time to clean it up and push to github.
