biblegateway-to-OSIS
====================

**Warning: Currently broken due to layout changes to BibleGateway. I plan to fix this tool at some point but
am not sure when that will be.**

Tools for retrieving bibles from biblegateway.com for use in <a href="https://github.com/mjdenham/and-bible">AndBible</a>

The "scraper" tool is a Java app which will retrieve a bible translation for biblegateway.com and save it as an XML file
in OSIS format. Start by running Fetcher to grab and store the raw HTML, then run Main to process it into an OSIS XML file.

The "build" script will run a tool called osis2mod, which is part of the <a href="http://www.crosswire.org/sword/">Sword</a> project, to convert the OSIS file into 
zText format, which is what AndBible can use.

You will need to retrieve a copy of the Sword Utilities from 
http://crosswire.org/ftpmirror/pub/sword/utils/win32/sword-utilities-1.6.2.zip. These are the only binaries I've found 
that actually work on Windows. You may have an easier time on Linux.

The "push" script will helpfully copy a finished package into the correct location on your phone, if it is plugged in 
and AndBible is installed. It requires a <translation name>.conf file for AndBible's use, an example of which (NASB.conf) is included.

This could all be rewritten as a web application, which I might well do some time, but I hope it may be helpful to anyone 
who would desparately like to get translations like the NASB, NIV, NKJV, etc into AndBible.

You'll need to do a bit of fiddling to get this all working, but hopefully it's easy enough.

<b>Disclaimer:</b> I have no affiliation with biblegateway.com, Sword, or AndBible. I am not aware of any terms of service that biblegateway.com have which forbids scraping, but use this software at your own risk. Please consider the relevant copyright issues. I strongly recommend that you only use the resulting Bible texts for personal use. It is worth noting, however, that this is the same mechanism that other Bible apps on the Play store, such as LiveBible, use to retrieve Bible text for offline use.
