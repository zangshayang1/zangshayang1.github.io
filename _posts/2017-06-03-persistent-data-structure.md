---
layout: post
title: DataStructure - Persistence
date: 2017-06-03 13:00:00 -0700
categories: study-notes
tag: dataStructure
---
* content
{:toc}




### Persistence - Version control for Data Structure

__Persistence:__ (think of git)
1. partial persistence - old versions are read-only
2. full persistence - old versions can be updated.
3. confluent persistence - combine different parts from multiple versions

__Classic example:__ [stack](https://zangshayang1.github.io/study-notes/2017/04/03/stack/)  
1. Persistent if implemented in an object-oriented way.
2. Non-persistent if implemented with Array.  

#### Convert Non-persistent to Persistent

__Fat-node technique converts everything to partial persistent:__
[wiki ref](https://zangshayang1.github.io/study-notes/2017/04/03/stack/)  
1. Versions are represented by a global counter
2. Each variable is represented by a collection of tuples like (version_stamp, value)
3. Each collection is represented as a BST ordered by version stamp so that accessing takes O(logn) with n being the number of versions.
4. Space: O(number of changes) Time: O(Non-persistent version query time * logn)
5. Using flat tree to replace BST can improve query performance.

__Path Copy Technique:__
[wiki ref](https://en.wikipedia.org/wiki/Persistent_data_structure#Complexity_of_path_copying)  
1. Works only for trees with parent to children pointers, such as WAVL.  
2. Create only new nodes along the paths from root node to target nodes where updates occur, and pointers to old unchanged parts.
3. The number of nodes will be the same as the number of versions that have been created. They are stored in an array, with version stamp.
4. Space O(number of versions)

<img src="{{ '/styles/images/pathCopy/pathCopy.jpg' }}" width="100%" />
