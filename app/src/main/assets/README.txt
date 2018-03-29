Instructions to create a taxi Guide:
Create a word document.
Save word document as Web Page, Filtered (.htm file)

Take the png image for the vehicle, resize to a reasonably small resolution (less than 1MB in size)

use https://www.base64-image.de/
to generate a base64 string. click </> show code, select all on the top result, and copy this into somewhere.
open the .htm file in a notepad
just after <style> paste this:
div.fill-screen {
    position: relative;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    text-align: center;
}

img.make-it-fit {
    max-width: 99%;
    max-height: 99%;
}

and just after <body lang=EN-US> tag, paste this
<div class='fill-screen'>
<img class="make-it-fit" src="" />
</div>

finally, use the long string you copied earlier into between the "" in the src property of the <img> tag.




