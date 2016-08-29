# iot-heroku-server
Playing around with Internet of Things on Heroku

## Installing on heroku

Fork, clone and:

```
heroku create
heroku addons:create m2x
heroku config -s | grep M2X >> .env # to work with M2X using heroku local
git push heroku master
```

Navigate to Heroku dashboard, go into the newly created app. 

1. Create new virtual device (there is M2X API of course, but I'm not creating devices dynamically).
2. Create metric stream.
3. Create chart for the above metric stream.

Now enjoy the app. I wrote a simple android app that publishes steps and a mock metric to this app.
Just need to find some more time to clean it up and push to github. 
