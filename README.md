This is the master branch for the kyoto repo
=====

This is the master branch - code in this branch should be final and working.

There is also a development branch - please merge your group's progress
into this branch on a regular basis. Code in this branch does not need to
be final.

Work in progress should be managed within group branches and merged into
master when it working.

When merging into master please use this process to avoid committing 
conflicts:

    git checkout master
    git merge --no-commit groupbranch
    FIX ANY CONFLICTS AND TEST
    git add -u # Adds all updated (tracked) files
    git status # Check what files you are changing
    git commit
    git push

Please be descriptive in your commit messages! If you are unsure what to
include, take a look at a few of the big GitHub projects.

Recommended reading:
 - http://nvie.com/posts/a-successful-git-branching-model/
 - The GIT man pages

This README will be updated later on with proper instructions to make it
look amazing with proper wiki page setup.

We are gonna go big guys!!!!!
