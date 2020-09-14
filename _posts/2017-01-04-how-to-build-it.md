---
layout: post
title: How it is built
date: 2017-01-04 09:20:00 -0800
categories: other-how-to
tag: jekyll-on-github
---

* content
{:toc}



### Quick start 

create github account as well as a repository for your site.    

clone source code and push to your own repository.    
  
{% highlight bash %}

$ git clone https://github.com/luoyan35714/LessOrMore local-project-dir
$ cd local-project-dir   
$ git config remote.origin.url your-reposiytory.git  
$ git push origin master

{% endhighlight bash %}

Go to your repository settings and allow Github Page to source from the master branch.

---

### About jekyll

Jekyll is written in ruby supported by Github Page.  

If you have ruby installed, the following will get you jekyll.
{% highlight bash %} $ gem install jekyll {% endhighlight bash %}

This will host your site built based on jekyll template on local machine. 
{% highlight bash %} $ jekyll serve {% endhighlight bash %}

If you want to specify other host.
{% highlight bash %} [option] --host {% endhighlight bash %}  

---

### About ruby eco behind jekyll

1. nice way to [install ruby](http://rvm.io)

2. [gem](https://rubygems.org/) is a ruby library manager. It helps to install ruby packages such as jekyll and bundler.

3. [bundler](http://bundler.io/) is a ruby project dependencies manager. It initialize a ruby project wit a Gemfile, where you list all the dependencies you want to use as well as their versions. So anyone with the same Gemfile can create the same dev environment via one command: {% highlight bash %} $ bundler install {% endhighlight bash %}

---

* Please reference the [author](https://github.com/luoyan35714/LessOrMore) of this jekyll template for more specific instructions.
