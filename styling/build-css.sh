#!/bin/sh

# Download semantic ui
if ! [ -d Semantic-UI-042c1c35c144ef147eb01665d959de51d5095517 ]; then
    wget https://github.com/Semantic-Org/Semantic-UI/archive/042c1c3.zip
    unzip 042c1c3.zip
fi

mkdir -p semantic
(cd Semantic-UI-042c1c35c144ef147eb01665d959de51d5095517/ > /dev/null && \
    tar -cf - *) | (cd semantic && tar -xBf -)
cp -u semantic-theme.config semantic/src/theme.config
(cd semantic && npm install --ignore-scripts && gulp build)
(cd semantic/dist/assets/ > /dev/null 2>&1 && tar -cf - themes) | \
    (cd assets/ && tar -xBf -)

npm install purify-css
./purifycss.js
