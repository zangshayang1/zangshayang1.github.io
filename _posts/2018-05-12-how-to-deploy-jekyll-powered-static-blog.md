---
layout: post
title:  How To Deploy Jekyll Powered Static Blog
date:   2016-08-27 01:08:00 +0800
categories: How-To
tag: Jekyll
---

* content
{:toc}

## What is Jekyll, Gem, Bundler?
Jekyll itself is a ruby library. Gem is a ruby library manager. Bundler is a ruby project manager (It ensures the consistency of the project's dependencies across different working environment). Rvm is ruby version manager.
```shell
# Use rvm to install/manage ruby -> http://rvm.io/
> gpg --keyserver hkp://keys.gnupg.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3
> \curl -sSL https://get.rvm.io | bash -s stable --ruby
# Install Bundler
> gem install bundler
# Initialize a vanilla ruby project starting point under the currrent dir, which comes with a vanilla Gemfile where all the dependecies are listed such as Jekyll
> bundler init 
# Install all the dependecies through Bundler, such as Jekyll
> bundler install
# Initialize a Jekyll project template
> jekyll _3.3.0_ new <name>
# Push to git and serve it
> git init .
> git remote add origin <project-repo-url>
> git add .
> git commmit -m "initialize my project"
> git push -u origin master
> bundle exec jekyll serve --host 0.0.0.0  # it will be served locally without specifying the host

# Don't forget to open tcp port 4000 on secruity group if you are using ec2

``` 
