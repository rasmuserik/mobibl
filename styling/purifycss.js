#!/usr/bin/env node

var purifyCss = require('purify-css')

var content = ['index.js']
var css = ['resources/css/*.css', 'semantic/dist/packaged/semantic.css']

var options = {
  output: 'assets/style.css',
  rejected: true,
  minify: true
}

purifyCss(content, css, options, (result) => {
  console.log('wrote', options.output)
})
