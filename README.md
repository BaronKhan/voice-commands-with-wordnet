Voice Commands with WordNet (VCW)
===========

VCW is a Java 8 library for easily adding voice commands to Java projects.
It uses Princeton's WordNet database and natural language understanding to
reduce the amount of work required to add voice commands to your project.

Unlike services such as IBM's Watson Conversation, Houndify or DialogFlow, VCW is
free-to-use and works without an Internet connection, making it suitable for
embedded systems, remote robots, or offline video games.


See the [documentation](https://github.com/BaronKhan/voice-commands-with-wordnet/blob/master/doc/vcw-manual.pdf)
for more details on how to use the library.

VCW is a library created for a final year project at Imperial College London.
The project can be found [here](https://github.com/BaronKhan/VoiceRecognitionRPG).

Purpose of the Library
---------

Imagine trying to process the follwing voice commands that map to an attack
using a sword within a video game:

- "attack with a sword"
- "hit with something sharp"
- "use a blade to fight"
- "launch an assault using the sword"
- "obliterate the enemy with a pointy weapon"

Or commands that map to healing with a potion:

- "heal"
- "recover"
- "rest"
- "heal with a potion"
- "regenerate using an elixer"

A naive approach would be to use a `switch` statement and add every possible
variation of the command:
```java
switch(input) {
  case "attack with a sword":
  case "hit with something sharp":
  //...
  case "obliterate the enemy with a pointy weapon":
     attackWithWeapon();
     break;
  case "heal":
  case "recover":
  //...
  case "rest":
     heal();
     break;
  case "heal with a potion":
  case "recover with a potion":
  //...
  case "regenerate using an elixer":
     healWithPotion();
     break;
  //...
}
```
However, this misses out on a lot of other variations of the command (e.g.
"fight using a sword") unless they are also added. It is infeasible to add every
possible variation of a command in this way.


Another approach (used by Houndify) is to create a regex-style expression to
denote the structure of acceptable phrases. For example, a regex expression for
detecting commands for attacking with a sword would look like this:

```
("attack" | "hit") . "with" . ["a"] . ("sword" | "blade")
```

Here, the `|` symbol denotes an alternation, while words between `[]` are
optional. However, as more variations of the command are added, the expression
becomes much more complex:

```
("attack" | "hit" | "obliterate" | ("launch" . "an" . "assault")) . ("with" | "using") . ["a"] . ("sword" | "blade" | ("something" . ("pointy" | "sharp")))
```

Even that expression does not cover all of the possible varations of the command
in the list above, let alone any other variation that the user might say.

VCW aims to avoid the need of hard-coding any phrases in the application. The
developer simply has to create a table of context-action mappings in a CSV file,
and specify the actions in the application as single words, as below.

|         |  attack    |  heal       |  move | 
|---------|------------|-------------|-------| 
| default |  Attack    |  Heal       |  Move | 
| weapon  |  AtkWeapon |             |       | 
| potion  |            |  HealPotion |       | 

And that's pretty much it. All of the commands in the above list will be detected, as well
as almost any other type of variation of the command.

See the [documentation](https://github.com/BaronKhan/voice-commands-with-wordnet/blob/master/doc/vcw-manual.pdf)
for more details on how to use the library.