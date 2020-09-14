---
layout: post
title:  Heroku Learning Notes
date:   2018-05-14 19:18:00 +0800
categories: study-notes
tag: heroku
---

* content
{:toc}


## Heroku CLI
```shell
# Grant app AWS service access
> heroku config:set AWS_ACCESS_KEY_ID=xxxxxxxxxxx AWS_SECRET_ACCESS_KEY=xxxxxxxxxxx

# Specify S3 bucket name
> heroku config:set S3_BUCKET_NAME=[bucket]
```