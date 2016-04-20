# Kismet
> **n**. Fate; fortune.

A refreshing crafting challenge to spice any modded experience. Don't know what to do next? Wishing to challenge yourself?

The RNG gods have smiled upon you and given rise to the **Kismet**. Try to fulfill it, at your own pace or against the clock, and see how well you do!

 *Warning: Batteries not included.*

 ## Summary
 The mod adds two blocks and one item.

 These blocks display random items from the game. These are your Kismet, your goals.

 Right-click the target into the displays to increase your score and generate another goal.

 One is under a timer, the other is not. If the timer runs out, the score is reset.

 The item allows for a reroll of the shown item, costing the key in the process.


 ## The concept
 So I get bored very easily when playing modded Minecraft. Why? *I don't know what to do.*

 There's either too many things to tackle and I get overwhelmed, or I can't think of anything worthy to try out. HQM packs, Simple achievements, Story Maps, all these kinda help by setting specific goals but those always require a lot of work, if not mine, from others.

 So what if the game chose some goals for you? Make this, craft that, use those. Random goals, that while simple to understand, could help clueless folk like me on what to start with that day.

 That's what this mod is all about: a constant challenge to try to get stuff, not being picky with what mod it is from (but it can, if you want to).

 ## Mechanics
 ### Kismetic Display Block
 One of the blocks added by the mod. When placed into the world, it'll display a randomly chosen item from the game above it.
 That'll be your goal.

 Your job will be to fetch an exact copy of whatever is displayed and **right-click** the display with it.

 If it's the correct goal the display will update itself with a new goal and increase a score counter. If it is not, nothing happens.

 ### Timed Kismetic Display Block
 Like the regular *Kismetic Display*, its goal is to show random items and/or blocks from the game, and yours to fetch it whatever it demands.

 The similarities end here: this display will not wait for you and will change goals on you every so often, whenever you achieved it already or not. While a goal isn't fulfilled it will display a helpful timer of the remaining time until it swaps goals.

 If you manage to fulfill several goals in a row, instead of a score, the display will show the unbroken streak so far. Try to aim high!

 ### Kismetic Key Item
 Right-clicking this item on any *Kismetic Display* will reroll the item shown, at the cost of consuming the key.

 The reroll won't count for any score and/or streak, and skipped items may show up again later.

 ### Commands
 All these can be done with `/kismet <command>` from within the game if the player is an OP or is presently in singleplayer.

 | Command | Description
 | ---- | ----
 | `reset` | Recalculates the target library for Kismet (the possible item pool from which targets are chosen)
 | `blacklist` | Adds the currently help item to the target blacklist

 ## Note for Server Admins
 Because of current limitations on the Minecraft engine, under dedicated servers the target library (see `/kismet reset`) won't be generated until:
   - An OP logins into the server;
   - A currently online OP runs the `/kismet reset` command.

 Failure to do so will result in any displays present in the world to not function until the above has been done.

 This is usually an one-off procedure, although if you have done any configuration changes relation to recipes or available items (such as adding a new server-side mod), I recommend *running the command again* to update the library.

 ## To-do
 - Client-side GUI that shows the current target as an ItemStack
    - Highlight squares in inventory?
 - Sync server configurations with client to prevent desyncs
 - Make the rendered item translucent (is it even possible?)
 - Tweak the crafting recipes

  |          |  example   |          |
  |:--------:|:----------:|:--------:|
  | emerald  | quartz     | diamond  |
  | redstone | slab       | redstone |
  | ?        | lapisBlock | ?        |

 - Fix the .lang files
   - I18n the remaining GUI strings
   - Outdated fields, such as the .config ones
   - Add pt-PT? :cat:
 - Test in 1.9 modpacks
   - Do internal mod testing for:
     1. Aliased block/items
   - Big-ass mods (lots of items/blocks)
 - Add the blacklist command
 - Downgrade the javacode so it can run in 1.6 (why :cry:)
 - Make this presentable
   - GIF of mod in action
   - Better description of the mod and its features
   - FAQ
 - Rename it to Gimme a Challenge/Goal ?
 - <3
