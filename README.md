# Kismet
> **n**. Fate; fortune.

Some aspire to construct great builds. Others, to automate the world. Yet others, to beat the savagest beasts. 

And then there's *us*, the clueless. Ones craving on some inspiration on what to do next for some reason or another.

Well, the RNG gods have smiled upon you and given rise to the **Kismet**! Try to fulfill it, at your own pace or against the clock, and see how well you do! *Warning: Batteries not included.*

**Important**: Server owners, please [read this](https://github.com/dustine/kismet/wiki/Note-for-servers) if you want to use this mod.

## Summary
Minecraft mod that does the following:

![The two blocks and item from the mod](notdone)

There are two blocks and one item. Let's focus on the blocks for now.

![A display cycling thought items rapidly](stillnotdone)

These blocks will display random items from the game. These items are your Kismet, your goals.

![Player fulfilling a display goal increasing score](unfinished)

Right-click the target into the display to increase your score and get the next goal.

![Display timer runs out with score poofing](gimmeamoment)

One is under a timer, the other is not. If the timer runs out, your score is reset.

![Consuming key to reroll display goal](sorryfortheinconvenience)

The item allows to reroll the goal, costing the key in the process.

![Several keys failing to reroll goal](thiswillgetmelynchedoneday)

The more goals you skip with a key, the less likely it'll be to work.

## The concept
So I get bored very easily when playing modded Minecraft. Why? *I don't know what to do.*

There's either too many things to tackle and I get overwhelmed from all the possibilities, or I can't think of anything worthy to try out. Or I'm just not filling inspired enough. HQM packs, Simple achievements, Story Maps, all these kinda work but they always require a lot of work, if not mine, from others.

So what if the game chose some objectives for you? Make this, craft that, use those. Random goals, that while simple to understand, could help clueless folk like me on what to start with that day.

That's what this mod is all about: a constant challenge to try to get stuff, not being picky with what mod it is from (but it can, if you want to).

## FAQ
*Q*. Are these even frequently asked?
No, but I can foresee parts of the mod that fail to explain themselves. And this is my solution to that fact for now.

*Q*. Crafting recipes please.
Not a question but I aim to serve. And please use JEI? The mod profits from it.

![Crafting recipe for the Kismet Display](nope)
![Crafting recipe for the Timed Kismet Display](notyet)
![Crafting recipe for the Kismetic Key](justwait)

*Q*. Does Kismet have any useful commands?
Yeah! All these can be done with `/kismet <command>`. Here's a select few most likely to be used.

| Command   | Description 
| --------- | ----        
| `block`   | Adds the currently help item to the target blacklist                                
| `pardon`  | Removes the currently help item from the target blacklist                          
| `force`   | Adds the currently help item as a forced target (whitelist)                         
| `unforce` | Removes the currently help item from the forced target list         
| `reset`   | Resets the target database (see [note for servers](https://github.com/dustine/kismet/wiki/Note-for-servers))

*Q*. Uh, these commands just gave me an error message saying they can only be run ingame, wtf?
Heh. This is not the case for ALL commands but it happens that the most useful ones (see above) end up being the ones that, as the error message says, need to be called in-game. No server consoles or automatic tools. Also OP permission required.

*Q*. Any more commands I should know?
Uh, yeah, but I don't see anyone using them to be honest. They're over [here](https://github.com/dustine/kismet/wiki/Commands).

*Q*. A Timed Display is taking too long to get a new goal, can I speed it up somehow?
Yup. Use a Kismetic Key on it, it'll unlock the next goal with no penalties. The key still gets consumed in the process.

*Q*. The Key failure thing seems unfair. Can't we disable it?
Still debating this one (keys are cheap-ish because of this). But know that fulfilling goals after using a key sucessfully makes it more likely it'll work in the future, and that unsuccessful key usages don't count into decreasing future odds. 

And you can always just break and replace the display, all you lose is score bragging rights :wink:

*Q*. Is there a way to move a display and not have it lose its score?
Try breaking the block with Silk Touch.

Yeah, I didn't expect that to work either :pensive: I'm thinking on a solution for now, suggestions would be welcome.

*Q*. The display looks all weird and no item is showing up. Have I done something wrong?
Probably not, but if your display looks like this:

![Kismetic Display in a broken state](poorthing)

Then it means there's something wrong with the mod. You can get more info by right-clicking the display, but the safest bet is that the target database is corrupted. Simplest fix is to run `/kismet reset`. If you're running a server and this happened, you haven't read [the thing I asked you to above](https://github.com/dustine/kismet/wiki/Note-for-servers) :anger:

*Q*. Why the name Kismet?
Because I suck at naming stuff.
