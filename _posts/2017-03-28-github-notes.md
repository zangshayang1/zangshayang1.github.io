---
layout: post
title: Github Notes
date: 2017-03-28 13:30:00 -0700
categories: study-notes
tag: github
---
* content
{:toc}


Last Modified: 20180513


Recommended Tutorials:
1. https://www.atlassian.com/git/tutorials
2. https://git-scm.com/book/en/v2


## Basics

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
> git reset -- index.html
# or you can use the following
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

## Undo

```shell
# Regret making dubious commits on master branch:  
git add dubious.py
git commit -m "this is the commit that I am about to regret making."
git checkout -b keep_dubious_on_this_branch
git checkout master
git reset --hard <SHA1sum of the latest undubous commit>

# If you made some change to index.html and committed again, it caused some bug. Then you regretted those changes and you want to withdraw the commit.
# "~1" means 1 step backward.
# You could withdraw the last 2 commits if you like.
> git reset @~1
# Note: Now the commit is withdrawn and the file became "modified and unstaged".  

# If you want to discard all the changes you made since the last bug-free commit.  
> git reset @ --hard # reset the working dir to last commit
> git clean -fd # clean all untracked files and dirs

# If you want to checkout the last committed version of some file (current version will be discarded):  
> git checkout @~1 index.html

# Now you improved the code in index.html and you committed it again.
> git add index.html
> git commit -m "bug-free improvement in index.html"

# Then you realized you should include "README.md" in this commit.
> git add README.md
> git commit --amend
# it will replace the last commit with this new one
# a chance to re-write your commit msg is also provided.
```

## Branch and Merge

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

Delete local merged branch  
```shell
> git branch -d [branch]
```

Delete local unmerged branch
```shell
> git branch -D [branch]
```

Delete remote branch  
```shell
> git push origin --delete [branch]
```

---

__Now you know how to develop your code back and forth locally.__  
__The following shows you how to bounce between local directory and remote server.__  

---

## Git Remote

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

## Git Compare

``` shell
# diff local files between different branches
git diff master:somedir branch:somedir

# diff local branch and remote branch:  
git diff master origin/master

#diff local files with those on the remote:
git diff origin/branch -- [file paths]

# diff between different commits:  
git diff targetCommitHash^ targetCommitHash

# If you want to compare your local repo with someone else's repo:
> git remote add someone someRepo.git
> git fetch someone somebranch
> git diff mybranch someone/somebranch
> git remote rm someone
```

## Git Remove
```shell
# Untrack files from git (should never track runtime files from the beginning):
git rm --cached fileX

# Remove branch locally
> git branch -d <branch_name>

# Remove branch locally even if the branch is not merged into master yet
> git branch -D <branch_name>

# Remove branch remotely
> git push origin --delete <branch_name>
```

## Other Notes
``` shell
# diverge push terminal to some other remote
git remote set-url origin git@github.corp.ebay.com:apdrm/<project>.git
git remote set-url --push origin git@github.corp.ebay.com:APD/<project>.git
```

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

## Git Rename
```shell
# rename origin to destination
> git remote rename origin destination

# rename a branch
> git checkout [target_branch]
> git branch -m [new_branch_name]
```

# Git Workflow

[Centralized and distributed workflow on github](https://git-scm.com/book/en/v2/Distributed-Git-Distributed-Workflows)
