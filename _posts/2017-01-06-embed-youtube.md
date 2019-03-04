---
layout: post
title:  Embed youtube
date:   2017-01-06 00:48:00 -0800
categories: HOW-TO
youtubeID: iX5V1WpxxkY
tag: youtube-embedding
---


* content
{:toc}


---
{% include youtubePlayer.html id=page.youtubeID %}
---

Under you _includes_ dir, create a "youtubePlayer.html" that puts the embeded video in a video container with a specific style, so that the video size can be __responsive__ to different types of devices.

{% highlight bash %}
echo '<div class="video-container"><iframe width="560" height="315" src="https://www.youtube.com/embed/{{ include.id }}" frameborder="0" allowfullscreen></iframe></div>' > youtubePlayer.html
{% endhighlight bash %}


Create the style for "video-container" class in your css file:

```
.video-container {
    position: relative;
    padding-bottom: 56.25%;
    padding-top: 30px;
    height: 0;
    overflow: hidden;
}

.video-container iframe,  
.video-container object,  
.video-container embed {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
}
```

Under you _posts_ dir, create a post with propriate jekyll front matter and include "youtubePlayer.html" where you want it to be.

```
---
layout: post
title:  Embed youtube
date:   2017-01-06 00:48:00 -0800
categories: HOW-TO
youtubeID: iX5V1WpxxkY --> put up the ID you wanted instead
tag: youtube
---


* content
{:toc}



{% raw %}{% include youtubePlayer.html id=page.youtubeId %}{% endraw %}

```

__Make sure__: you push ALL 3 updates up to your Github Repo.


* my thanks to the author: Adam Garrett-Harris and his [original post](https://adam.garrett-harris.com/how-to-easily-embed-youtube-videos-in-jekyll-sites-without-a-plugin) on how to embed the video.
* my thanks to the author: Avexdesigns and his [original post](https://avexdesigns.com/responsive-youtube-embed/) on how to make the video responsive.
