biblegateway-to-OSIS
====================

Tools for retrieving bibles from biblegateway.com for use in AndBible

The "scraper" tool is a Java app which will retrieve a bible translation for biblegateway.com and save it as an XML file
in OSIS format.

The "build" script will run a tool called osis2mod, which is part of the Sword project, to convert the OSIS file into 
zText format, which is what AndBible can use.

You will need to retrieve a copy of the Sword Utilities from 
http://crosswire.org/ftpmirror/pub/sword/utils/win32/sword-utilities-1.6.2.zip. These are the only binaries I've found 
that actually work on Windows. You may have an easier time on Linux.

The "push" script will helpfully copy a finished package into the correct location on your phone, if it is plugged in 
and AndBible is installed.

This could all be rewritten as a web application, which I might well do some time, but I hope it may be helpful to anyone 
who would desparately like to get translations like the NASB, NIV, NKJV, etc into AndBible.

You'll need to do a bit of fiddling to get this all working, but hopefully it's easy enough.

P.S. I highly recommend using a caching tool at first, to allow you to aggressively cache the app's requests to biblegateway.com so that you don't inundate them with requests over and over again while you try to get it working. Use the relaxTransparency = yes option to always return a hit from the cache when the data is present.
