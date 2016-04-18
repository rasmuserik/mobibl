#!/bin/bash -vx

# copy id and version from project.clj into config.xml
export APP_ID=`grep defproject project.clj | head -n 1 | sed -e 's/ *".*//' | sed -e 's/.* //' | sed -e 's/\\//./'`
export VERSION=`grep defproject project.clj | head -n 1 | sed -e 's/.* "//' | sed -e 's/".*//'`
perl -pi -e "s/widget id=\"[^\"]*\" version=\"[^\"]*/widget id=\"$APP_ID\" version=\"$VERSION/" config.xml 

# cleanup before building
killall java
lein clean 

# build
lein cljsbuild once dist 
cordova prepare
cordova build

# copy cordova-web
cp -a platforms/browser/www/cordova* .
cp -a platforms/browser/www/cordova* resources/public/


# Manifest file
#
#echo "CACHE MANIFEST" > index.appcache
#echo "# `date`" >> index.appcache
#find assets -type f >> index.appcache
#echo "index.js" >> index.appcache
#find */config.xml >> index.appcache

npm install clean-css
./node_modules/.bin/cleancss -o assets/style.css resources/css/*

cat doc/intro.md doc/roadmap.md CONTRIBUTING.md LICENSE.md > README.md
for SRC in main mock_data mobibl html5 bibapp_datasource
do
cat src/solsort/mobibl/${SRC}.cljs | 
  sed -e "s/^[^/]/    \0/" | sed -e s'/^ *[;][;] \?//' >> README.md
done
