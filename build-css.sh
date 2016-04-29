#!/bin/sh

(cd semantic && gulp build)
(cd semantic/dist/assets/ > /dev/null 2>&1 && tar -cf - themes) | \
    (cd assets/ && tar -xBf -)

./purifycss.js
