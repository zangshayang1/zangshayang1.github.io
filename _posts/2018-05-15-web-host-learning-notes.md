---
layout: post
title:  Web Hosting Learning Notes
date:   2018-05-14 22:08:00 +0800
categories: study-notes
tag: octocat
---

* content
{:toc}


## Map purchased domain to Heroku webApp

__How to configure domain purchased from Google and map it to webApp hosted on Heroku__
1. Purchase domain name [octocat.me](www.octocat.me) from Google, which is your DNS provider.
2. Deploy your app on Heroku and customize its domain name as www.octocat.me, which is a subdomain of octocat.me.
3. Map www.octocat.me to as CNAME of data at: www.octocat.me.herokuapp.com in Google Domain DNS settings. 

