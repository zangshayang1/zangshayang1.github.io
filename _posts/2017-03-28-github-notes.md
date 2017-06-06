---
layout: post
title: Github Notes
date: 2017-03-28 13:30:00 -0700
categories: Study-Notes
tag: github
---
* content
{:toc}



Last Modified: 20170517


Recommended Tutorials:
1. https://www.atlassian.com/git/tutorials
2. https://git-scm.com/book/en/v2

# Git Local

### Basics

Initialize git repo locally  
```shell
> git init
```

Connect local git with remote repo and name it "origin"  
```shell
> git remote add origin repo-url
```

Create __untracked__ files, such as "README.md" and "index.html"  
Check their status in current directory  
```shell
> git status
```

Put "index.html" on __stage__ to keep track of it
```shell
> git add index.html
```

If you realized there's something else you'd like to add to "index.html", you can __unstage__ it by:  
```shell
> git reset @ index.html
# "@" denotes "HEAD" pointer in old git versions.
```

_Note:_  
* _Also, you can directly modify already-staged files. The file itself became_ __"modified and unstaged"__ _but you can see there's still a copy of previously-staged version of the file if you check the status. If you stage the modified file again, it will cover the old one._

After you staged all the files you'd like to commit this time, you can create a __pointer__(also a time stamp in your commit history) to the current version of code/files in your working directory by committing as follows:  
```shell
> git commit -m "init commit of index.html"
```

_Note:_  
* _After the commit is made, the file became_ __"unmodified and unstaged"__.

Check commit history
```shell
> git log --graph --all --decorate
```

### Undo

If you made some change to index.html and committed again, it caused some bug. Then you regretted those changes and you want to withdraw the commit.
```shell
> git reset @~1
# "~1" means 1 step backward.
# You could withdraw the last 2 commits if you like.
```

_Note:_  
* _Now the commit is withdrawn and the file became_ __"modified and unstaged"__.  

If you want to discard all the changes you made since the last bug-free commit.  
```shell
> git reset @ --hard # reset the working dir to last commit
> git clean -fd # clean all untracked files and dirs
```

If you want to checkout the last committed version of some file:  
* If you add/commit this old version of the file. The current version will be lost. This line is subject to test.  

```shell
> git checkout @~1 index.html
```


Now you improved the code in index.html and you committed it again.
```shell
> git add index.html
> git commit -m "bug-free improvement in index.html"
```

Then you realized you should include "README.md" in this commit.
```shell
> git add README.md
> git commit --amend
# it will replace the last commit with this new one
# a chance to re-write your commit msg is also provided.
```

### Branch and Merge

After you finalized a working version, some genius idea hit on you and you'd like to add some features and test them without corrupting the working version of code. You can create a "ideaTest" branch.
```shell
> git branch ideaTest
```

See what branches you have locally and which one you are at right now.
```shell
> git branch
```
_Note:_  
* _master branch is the initial branch you had._

Switch between branches
```shell
> git checkout ideaTest
```

If you don't like the changes you made on "ideaTest" branch. Just switch back to master branch and delete this "ideaTest" branch.
```shell
> git branch -d ideaTest
```

If you like the changes you made "ideaTest" branch, you can:

```shell
> git add & commit changes on ideaTest branch
> git checkout master
> git merge ideaTest
```

_Note: make sure you switched back to master branch prior to merging._


Another way to "merge" changes from both branches is "rebase". The difference is that the latter makes the commitment history cleaner.
```shell
> git checkout ideaTest # switch to the test branch
> git rebase master # apply changes on top of master branch
> git checkout master # switch back to master branch
> git merge ideaTest # just fast forward master branch to the latest version
```

_NOTE:_  
_You DO NOT want to rebase, in other words,_ __change commitment history__ _when:_  
* _Someone might be using one of the commits you published as their "base"._  

_You DO want to rebase when:_
* _You want to hide some "commit history" or "branch merge history" before pushing them to remote server._  
* _You are one of the developers for some large project and integration manager pushed someone's code to remote server. (This is when you rebase your work on top of the latest code on remote server.)_

---

__Now you know how to develop your code back and forth locally.__  
__The following shows you how to bounce between local directory and remote server.__  

---

# Git Remote

__When your code is ready to go to the server end - origin/master branch.__  
_Note:_  
* _You can of course push to/pull from other branches on remote server._  

Scenarios:  
1. If you are working on your own and your remote repo doesn't contain anything you don't have locally, you can push to remote directly.
```shell
> git push origin master
# assume we are at local master branch and it contains the latest version
```

2. If you and your collaborators are working on different modules respectively, and the remote server contains update from his/her work that you don't have locally, you need to pull their updates down to your local before pushing your updates up to the server.  
```shell
> git pull origin master
> git push origin master
```

3. If you and your collaborators are working on different lines of the same module, prior to doing the same as "Scenario 2", you should commit your updates locally, for example:  
```shell
> git add index.html
> git commit -m "local change to index.html before merging with somebody's code."
> git pull origin master
> git push origin master
```

4. If you and your collaborators are working on overlapped lines of the same module, you need to  
commit your changes locally.  
pull down their work - this is where "conflicts" occur.  
manually go through the diff, resolve the conflict and locally commit the resolved code.  
then push to server.  
```shell
> git add index.html # line 7 changed
> git commit -m "local change to index.html before merging with somebody's code."
> git pull origin master # line 7 conflicted
> vim index.html # resolve conflict at line 7
> git add index.html # stage again
> git commit -m "resolve conflict at line 7, index.html when merging with somebody's code."
> git push origin master
```
---

### Other Notes

Untrack files from git (should never track runtime files from the beginning):
``` shell
git rm --cached fileX
```

If you want to compare your local repo with someone else's repo:
``` shell
> git remote add someone someRepo.git
> git fetch someone somebranch
> git diff mybranch someone/somebranch
> git remote rm someone
```

Delete branch locally and remotely:
> git push origin --delete <branch_name>
> git branch -d <branch_name>

When you pull from a repo with all kinds of runtime binaries and IDE-specific files. It won't go through because chances are that you have a lot of similar files, either untracked or modified but not committed in your local. But you really don't care about those auto-generated files and you just want to pull the update.  
The following does:  
1. fetch all from the target
2. set local HEAD to FETCH_HEAD, which is a pointer to what has been fetched.
3. clean all the untracked files.
``` shell
> git fetch target_repo target_branch
> git reset --hard FETCH_HEAD
> git clean -df
```

Rename local branch  
> git checkout [target_branch]
> git branch -m [new_branch_name]


# Git Workflow

[Centralized and distributed workflow on github](https://git-scm.com/book/en/v2/Distributed-Git-Distributed-Workflows)
