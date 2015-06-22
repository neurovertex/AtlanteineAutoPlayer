Atlanteine Auto-Player
======================

This is an old program I wrote around 2011 when a friend dared me to code something that could play *Atlanteine*,
a flash game from the website [KadoKado](http://kadokado.com). I recently found the sources and decided to clean them
up a bit and upload them here, for reference.

How it works
------------

Well I don't recall the details, and I'm sure as hell not gonna re-read the whole of it. But from what I remember,
here's the general process:
Note: Screen capture and mouse control are accomplished through the java.awt.Robot class.

 - Find the "game area" - area of the screen where the game is. This is accomplished by taking a screenshot and scanning
 it for a color that is known to be the upper left-hand corner.
 - "Parse" the game area : Take a picture of the game area, divide it by the game grid, and compare the colours of each
 square to a list of known ones. The result is a model of the grid with the player, boxes, obstacles, empty spaces, portals...
 - Do some simple pathfinding to find the best path available (well it should but I wouln't vouch for the optimality of the
 found path. It's some simple pure recursive tree exploration)
 - Use the keyboard emulation to guide the pumpkin. Try and track it to ensure it goes where it should (reset otherwise).