[![Stories in Ready](https://badge.waffle.io/solsort/mobibl.png?label=ready&title=Ready)](https://waffle.io/solsort/mobibl)
# MoBibl

The purpose of this project is to make an app for the public danish libraries.

Feel free to use the issue tracker (https://github.com/solsort/mobibl/issues), for discussions, comments, suggest features etc.

## Status

The project starts medio February 2016. Nothing is up and running yet.

## Roadmap
### Version 0.1

- Functionality
    - Search for library materials, order books
    - News / calendar for library
    - Locate library
    - Patron status / renew books, etc.
- Backend
    - search etc. must build on real services
    - actual patron status, order books etc. might be mocked if we do not have access to those APIs yet
- Platforms
    - HTML5 browser app
    - Packagede Cordova app

Wireframes:

<img width=20% align=top src=doc/wireframes/search.jpg>
<img width=20% align=top src=doc/wireframes/work.jpg>
<img width=20% align=top src=doc/wireframes/library.jpg>
<img width=20% align=top src=doc/wireframes/patron-status.jpg>
### Later versions

- Functionality
    - Exploration of related materials
- Platforms
    - react-native apps (reusing all logic, but rewritten UI using native widgets on Android/iOS)

# Openness

The project will be released under a **proper MIT open source license** when/if the Danish Public Library Sector decides, that they want to use the app, and pays for the development cost. Until then it is only released as Creative Commons BY-NC-ND, and copyrighted by solsort.com ApS. The cost calculated from actual number hours used on the project, plus expenses such as: transport to meetings with stakeholders, buying of graphics assets for the projects, etc. Contributors should keep track of how many hours we use on the project.

The development happens in full openness on the project github repository, and release status, features etc. can be seen/discussed here.

# Literate source code

    (ns solsort.mobibl.mobibl
      (:require-macros  [cljs.core.async.macros :refer  [go go-loop alt!]])
      (:require
       [solsort.util
        :refer 
        [<ajax <seq<! js-seq normalize-css load-style! put!close! parse-json-or-nil log page-ready render
         dom->clj]]
       [reagent.core :as reagent :refer  []]
       [clojure.string :as string :refer [replace split blank?]]
       [cljs.core.async :refer  [>! <! chan put! take! timeout close! pipe]]))

    (render [:h1 "hello"])

