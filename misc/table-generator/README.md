`ContextActionMap` Table Generator
===========

This directory contains a Python program from automatically generating
a `ContextActionMap` Java source file from a CSV file containing the table.


The CSV file should have the action strings along the top, and the context types
along the left-side. The fields should contain the class names of the `Action`
methods to be called.


You can run an example for a game `ContextActionMap` as follows:

`python generateTable.py table-examples/game-table.csv GameMap`