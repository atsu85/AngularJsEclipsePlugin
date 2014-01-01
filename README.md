AngularJS Eclipse Plugin
================


### Quick Overview

The project was created to assist developer with writing [AngularJS](http://AngularJS.org/) applications.
It extends the default HTML file editor of Eclipse WDT plugin (instead of creating new editor just for AngularJS)...
![Screenshot of opening WDT HTML Editor](https://dl.dropboxusercontent.com/sh/5xpbkluybciflfl/Uo6Nvhuqb_/OpenUsingHtmlEditor.png?token_hash=AAF3yOKMHz8siMft4MxxXtZb-jz6BccDWrLQcXNfSXaK8A)
... and it doesn't require configuring project anyhow (no need to add some custom project nature to the Eclipse project).

![Screenshot of the plugin in action](https://dl.dropboxusercontent.com/sh/5xpbkluybciflfl/rbGePYqe7g/Screenshot-HtmlEditor1.png?token_hash=AAF3yOKMHz8siMft4MxxXtZb-jz6BccDWrLQcXNfSXaK8A)

0. Propose AngularJS built-in directives that can be used as html element attributes
  * Show short description about each proposal (and link to AngularJS documentation of the proposal)
  * don't show proposal if...
    * ... given attribute is already added.
    * ... given attribute can't be used with current html tag (for example ng-value can be used with input tag).
    * ... it depends on presence of another attribute, for example don't propose "count" unless "ng-pluralize" is already added.
  * filter proposals based on the attribute name user started typing
0. Convert regions of text to hyperlink in html file if it contains smth like "javascript-file-name" and project contains file "**/javascriptFileName.js" - if you have all angular directives in separate files based on directive name, then you can jump from usage to directive javascript file (this works great with my project conventions, but may be useless for you if you don use the same convention)


### Prerequisites

Eclipse 4.3 installation with "Eclipse Web Development Tools" plugin.
It is included in many eclipse distributions related to web development, but it can be installed through Help -> Install New Software:
![Screenshot of installing plugin](https://dl.dropboxusercontent.com/sh/5xpbkluybciflfl/Pu-L9EZ_x3/dependency-WDT-1.GIF?token_hash=AAF3yOKMHz8siMft4MxxXtZb-jz6BccDWrLQcXNfSXaK8A)


### Installation

0. See "Prerequisites" and install WDT if needed

0. Download latest ee.uiboupin.ats.angular.eclipse_*.jar version from [here](http://goo.gl/iHKfex)

0. Place it into dropins folder of the Eclipse installation folder

0. Restart Eclipse


### What if it doesn't work

0. See if plugin is installed:
  0.  Go to Eclipse -> Help -> About Eclipse -> Installation Details -> Plug-ins 
  0. Do you see a row with Plug-in Id "ee.uiboupin.ats.angular.eclipse"? If you can't see it, plugin is not installed.
0. If pluing is not installed, it is most probably because you haven't installed "Eclipse Web Development Tools" plugin - see "Prerequisites" and install WDT if needed


### TODO
* Create hyperlinks in HTML editor for directive attributes (link to correct directive JS file).
* Create hyperlinks in JS editor for template (controller view and directive partial view) files.
* Can we convert urls to links in proposal description? Would be easier to read full documentation.
* Propose custom angular directives created in project.
* Propose element type directives.
